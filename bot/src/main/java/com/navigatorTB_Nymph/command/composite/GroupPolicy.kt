package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.data.*
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MyPluginData
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.LocalDateTime

object GroupPolicy : CompositeCommand(
    PluginMain, "GroupPolicy", "群策略", description = "群功能个性化配置"
) {
    override val usage: String = """
        ${CommandManager.commandPrefix}群策略 [目标ID] [设定值]
        目标ID列表：
        *1* 报时模式
        *2* 订阅模式
        *3* 教学许可
        *4* 色图许可
        *5* 对话概率
        *6* 授权续费
        *7* 免打扰模式
        *8* 新成员通报
        *9* 每日提醒模式
        *X*群策略设定状态汇报
        """.trimIndent()

    @SubCommand("群策略设定状态汇报", "汇报")
    suspend fun MemberCommandSenderOnMessage.report() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val policy = UserPolicy(
            dbObject.selectOne(
                "Policy", Triple("groupID", "=", "${group.id}"), "群策略设定状态汇报\nFile:GroupPolicy,kt\tLine:49"
            )
        )
        val subscribeInfo = UserSubscribeInfo(
            dbObject.selectOne(
                "SubscribeInfo", Triple("groupID", "=", "${group.id}"), "群策略设定状态汇报\nFile:GroupPolicy,kt\tLine:56"
            )
        )
        val responsible = UserResponsible(
            dbObject.selectOne(
                "Responsible", Triple("groupID", "=", "${group.id}"), "群策略设定状态汇报\nFile:GroupPolicy,kt\tLine:62"
            )
        )
        val acgImg = UserACGImg(
            dbObject.selectOne(
                "ACGImg", Triple("groupID", "=", "${group.id}"), "群策略设定状态汇报\nFile:GroupPolicy,kt\tLine:68"
            )
        )
        dbObject.closeDB()

        val state = listOf("停用", "启用")
        sendMessage(buildMessageChain {
            +"群策略设定状态报告\n"
            +"===============\n"
            +"免打扰状态：   \t${state[policy.undisturbed]}\n"
            +"新成员通报状态：   \t${state[policy.groupNotification]}\n"
            +"教学许可状态：\t${state[policy.teaching]}\n"
            +"色图许可状态：\t${state[policy.acgImgAllowed]}\n"
            +"订阅状态：\n"
            +"\t>碧蓝航线\t${state[subscribeInfo.azurLane]}\n"
            +"\t>明日方舟\t${state[subscribeInfo.arKnights]}\n"
            +"\t>FGO\t\t${state[subscribeInfo.fateGrandOrder]}\n"
            +"\t>原神\t\t${state[subscribeInfo.genShin]}\n"
            +"每日提醒状态：\n"
            +"\t>碧蓝航线\t${state[policy.dailyReminderMode % 2]}\n"
            +"\t>FGO\t\t${state[policy.dailyReminderMode / 2]}\n"
            +"报时模式：\t${MyPluginData.tellTimeMode[policy.tellTimeMode]}\n"
            +"对话概率:\t\t${policy.triggerProbability}%\n"
            +"群色图配给：\t${acgImg.score}/200\n"
            +"-=-=-=-=-=-=-=-\n"
            +"距授权到期还有${responsible.active}天\n"
            +"群责任人：\t"
            +(responsible.principalID).let {
                if (it != 0L) At(it) else PlainText("本群暂无责任人")
            }
        })
    }

    @SubCommand("免打扰模式")
    suspend fun MemberCommandSenderOnMessage.tellUndisturbed(value: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        if (value > 0) {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("undisturbed"), arrayOf("1")),
                "免打扰模式\nFile:GroupPolicy,kt\tLine:117"
            )
            sendMessage("夜间免打扰已启用")
        } else {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("undisturbed"), arrayOf("0")),
                "免打扰模式\nFile:GroupPolicy,kt\tLine:125"
            )
            sendMessage("夜间免打扰已停用")
        }
        dbObject.closeDB()
    }

    @SubCommand("免打扰模式")
    suspend fun MemberCommandSenderOnMessage.tellUndisturbed() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 免打扰模式 [模式值]
            ——————————
            模式值 | 说明
            > 0	    开启免打扰
            ≯ 0     关闭免打扰
            """.trimIndent()
        )
    }

    @SubCommand("新成员通报")
    suspend fun MemberCommandSenderOnMessage.tellNotification(value: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        if (value > 0) {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("groupNotification"), arrayOf("1")),
                "新成员通报\nFile:GroupPolicy,kt\tLine:168"
            )
            sendMessage("成员通报已启用")
        } else {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("groupNotification"), arrayOf("1")),
                "新成员通报\nFile:GroupPolicy,kt\tLine:176"
            )
            sendMessage("成员通报已停用")
        }
        dbObject.closeDB()
    }

    @SubCommand("新成员通报")
    suspend fun MemberCommandSenderOnMessage.tellNotification() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 新成员通报 [模式值]
            ——————————
            模式值 | 说明
            > 0	    开启通报
            ≯ 0     关闭通报
            """.trimIndent()
        )
    }

    @SubCommand("报时模式")
    suspend fun MemberCommandSenderOnMessage.tellTime(mode: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        if (MyPluginData.tellTimeMode.containsKey(mode)) {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("tellTimeMode"), arrayOf("$mode")),
                "报时模式\nFile:GroupPolicy,kt\tLine:219"
            )
            sendMessage("报时设定到模式 ${MyPluginData.tellTimeMode[mode]}")
        } else {
            if (mode == 0) {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("tellTimeMode"), arrayOf("0")),
                    "报时模式\nFile:GroupPolicy,kt\tLine:228"
                )
                sendMessage("已关闭本群报时")
            } else {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("tellTimeMode"), arrayOf("-1")),
                    "报时模式\nFile:GroupPolicy,kt\tLine:236"
                )
                sendMessage("未知的模式，报时设定到标准模式")
            }
        }
        dbObject.closeDB()
    }

    @SubCommand("报时模式")
    suspend fun MemberCommandSenderOnMessage.tellTime() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val info: MutableList<String> = mutableListOf()
        MyPluginData.tellTimeMode.forEach {
            info.add("${it.key}\t    ${it.value}")
        }
        sendMessage("无效模式参数，设定失败,请参考以下示范命令\n群策略 报时模式 [模式值]\n——————————\n模式值 | 说明\n${info.joinToString("\n")}\n-\t    标准报时")
    }

    @SubCommand("订阅模式")
    suspend fun MemberCommandSenderOnMessage.subscription(mode: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        if (mode >= 0) {
            val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
            dbObject.update(
                "SubscribeInfo",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("azurLane"), arrayOf(if (mode and 1 == 1) "1" else "0")),
                "报时模式\nFile:GroupPolicy,kt\tLine:269"
            )
            dbObject.update(
                "SubscribeInfo",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("arKnights"), arrayOf(if (mode and 2 == 2) "1" else "0")),
                "报时模式\nFile:GroupPolicy,kt\tLine:275"
            )
            dbObject.update(
                "SubscribeInfo",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("fateGrandOrder"), arrayOf(if (mode and 4 == 4) "1" else "0")),
                "报时模式\nFile:GroupPolicy,kt\tLine:281"
            )
            dbObject.update(
                "SubscribeInfo",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("genShin"), arrayOf(if (mode and 8 == 8) "1" else "0")),
                "报时模式\nFile:GroupPolicy,kt\tLine:287"
            )
            sendMessage("订阅设定到模式$mode")
            dbObject.closeDB()
        } else {
            subscription()
        }
    }

    @SubCommand("订阅模式")
    suspend fun MemberCommandSenderOnMessage.subscription() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 订阅模式 [模式值]
            ——————————
            模式值计算方法及示例(1:开启,0:关闭)
            Mode = AzLn*1 + Akns*2 + FGO*4 + Gesn*8
            6 = 0 + 2 + 4 + 0(仅开启Akns及FGO)
            7 = 1 + 2 + 4 + 0(除Gesn外全部开启)
            9 = 1 + 0 + 0 + 8(仅开启AzLn及Gesn)
            15 = 1 + 2 + 4 + 8(全部开启)
            """.trimIndent()
        )
    }

    @SubCommand("每日提醒模式")
    suspend fun MemberCommandSenderOnMessage.dailyReminder(mode: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        when (mode) {
            0 -> {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("dailyReminderMode"), arrayOf("0")),
                    "每日提醒模式\nFile:GroupPolicy,kt\tLine:336"
                )
                sendMessage("已关闭本群每日提醒")
            }
            1, 2, 3 -> {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("dailyReminderMode"), arrayOf("$mode")),
                    "每日提醒模式\nFile:GroupPolicy,kt\tLine:345"
                )
                sendMessage("每日提醒设定到模式$mode")
            }
            else -> {
                dailyReminder()
            }
        }
        dbObject.closeDB()
    }

    @SubCommand("每日提醒模式")
    suspend fun MemberCommandSenderOnMessage.dailyReminder() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 每日提醒模式 [模式值]
            ——————————
            模式值 | 说明
            0	    关闭每日提醒
            1	    仅开启 AzurLane 每日提醒
            2	    仅开启 FateGrandOrder 每日提醒
            3	    同时开启 AzurLane 与 FateGrandOrder 每日提醒
            """.trimIndent()
        )
    }

    @SubCommand("教学许可")
    suspend fun MemberCommandSenderOnMessage.teaching(switch: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        if (switch > 0) {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("teaching"), arrayOf("1")),
                "教学许可\nFile:GroupPolicy,kt\tLine:394"
            )
            sendMessage("已开启本群教学模式")
        } else {
            dbObject.update(
                "Policy",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("teaching"), arrayOf("0")),
                "教学许可\nFile:GroupPolicy,kt\tLine:402"
            )
            sendMessage("已关闭本群教学模式")
        }
        dbObject.closeDB()
    }

    @SubCommand("教学许可")
    suspend fun MemberCommandSenderOnMessage.teaching() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 教学许可 [模式值]
            ——————————
            模式值 | 说明
            > 0	    开启教学功能
            ≯ 0     关闭教学功能
            """.trimIndent()
        )
    }

    @SubCommand("对话概率")
    suspend fun MemberCommandSenderOnMessage.triggerProbability(value: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        dbObject.update(
            "Policy",
            Pair("groupID", "${group.id}"),
            Pair(arrayOf("triggerProbability"), arrayOf("$value")),
            "对话概率\nFile:GroupPolicy,kt\tLine:444"
        )
        dbObject.closeDB()
        sendMessage("本群对话概率调整到$value%")
    }

    @SubCommand("对话概率")
    suspend fun MemberCommandSenderOnMessage.triggerProbability() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 对话概率 [概率值]
            ——————————
            概率值最高为100(必定触发),最低为0(绝不触发)
            #若无可回答内容，任何情况下都不会触发
            """.trimIndent()
        )
    }

    @SubCommand("色图许可")
    suspend fun MemberCommandSenderOnMessage.acgImage(token: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        when (token) {
            "无内鬼" -> {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("acgImgAllowed"), arrayOf("1")),
                    "色图许可\nFile:GroupPolicy,kt\tLine:486"
                )
                sendMessage("无内鬼,可以交易")
            }
            "有内鬼" -> {
                dbObject.update(
                    "Policy",
                    Pair("groupID", "${group.id}"),
                    Pair(arrayOf("acgImgAllowed"), arrayOf("0")),
                    "色图许可\nFile:GroupPolicy,kt\tLine:495"
                )
                sendMessage("有内鬼!终止交易!")
            }
            else -> sendMessage("口令错误!你就是内鬼?!")
        }
        dbObject.closeDB()
    }

    @SubCommand("色图许可")
    suspend fun MemberCommandSenderOnMessage.acgImage() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(
            """
            无效模式参数，设定失败,请参考以下示范命令
            群策略 色图许可 [模式值]
            ——————————
            模式值 | 说明
            无内鬼      开启随机图片功能
            有内鬼      关闭随机图片功能
            """.trimIndent()
        )
    }

    private fun permissionCheck(user: Member): Boolean {
        UsageStatistics.record(primaryName)
        return user.permission.isOperator().not()
    }

    @SubCommand("授权续费")
    suspend fun MemberCommandSenderOnMessage.renewal(cdKey: String, key: String) {
        if (group.botMuteRemaining > 0) return
        val cdKeyDbObject = SQLiteJDBC(PluginMain.resolveDataPath("CD-KEY.db"))
        val cdk = RegCode(cdKeyDbObject.selectOne(
            "'CD-KEY'",
            Triple("code", "=", "'$cdKey'"),
            "授权续费\nFile:GroupPolicy,kt\tLine:534"
        ).run {
            if (this.isEmpty()) {
                val t = LocalDateTime.now()
                mutableMapOf(
                    "code" to "for-test-use-only",
                    "key" to "${t.dayOfYear}${t.dayOfMonth}${t.dayOfWeek.value}",
                    "value" to 1
                )
            } else this
        })
        if (cdk.key != key) {
            cdKeyDbObject.closeDB()
            sendMessage("该序列号或密码无效,请检查输入(若确定输入无误请联系客服)")
            return
        }

        cdKeyDbObject.delete("'CD-KEY'", Pair("code", "'$cdKey'"), "授权续费\nFile:GroupPolicy.kt\tLine:545")
        cdKeyDbObject.closeDB()

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val groupObject = UserResponsible(
            dbObject.selectOne(
                "Responsible", Triple("groupID", "=", "${group.id}"),
                "授权续费\nFile:GroupPolicy,kt\tLine:549"
            )
        )
        val onlyTime = groupObject.active + cdk.value + if (LocalDateTime.now().hour <= 14) 0 else 1

        dbObject.update(
            "Responsible",
            Pair("groupID", "${group.id}"),
            Pair(arrayOf("principalID", "active"), arrayOf("${user.id}", "$onlyTime")),
            "授权续费\nFile:GroupPolicy,kt\tLine:557"
        )
        dbObject.closeDB()
        ActiveGroupList.activationStatusUpdate(false)
        sendMessage("续费完成,可通过群策略设定状态汇报命令查看当前群状态")
    }

    @SubCommand("授权续费")
    suspend fun MemberCommandSenderOnMessage.renewal() {
        if (group.botMuteRemaining > 0) return
        sendMessage(
            """
            无效参数，设定失败,请参考以下示范命令
            授权续费 [序列号] [密码]
            ——————————
            将认定发起续费的账号为当前群的Bot归属人
            根据输入的序列号对使用期限进行续费
            """.trimIndent()
        )
    }
}