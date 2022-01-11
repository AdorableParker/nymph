/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:43
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.MyPluginData.tellTimeMode
import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.LocalDateTime


object GroupPolicy : CompositeCommand(
    PluginMain, "GroupPolicy", "群策略",
    description = "群功能个性化配置"
) {
    override val usage: String = """
        ${CommandManager.commandPrefix}群策略 [目标ID] [设定值]
        目标ID列表：
        *1* 报时模式
        *2* 订阅模式
        *3* 每日提醒模式
        *4* 教学许可
        *5* 色图许可
        *6* 对话概率
        *7* 免打扰模式
        *8* 责任人绑定
        *9* 继承到群
        *X* 撤销继承协议
        *EX*群策略设定状态汇报
        """.trimIndent()

    @SubCommand("群策略设定状态汇报", "汇报")
    suspend fun MemberCommandSenderOnMessage.report() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))

        val policy = dbObject.selectOne("Policy", "group_id", group.id, 1)
        val subscribeInfo = dbObject.selectOne("SubscribeInfo", "group_id", group.id, 1)
        val responsible = dbObject.selectOne("Responsible", "group_id", group.id, 1)
        val acgImg = dbObject.selectOne("ACGImg", "group_id", group.id, 1)
        dbObject.closeDB()

        val state = listOf("停用", "启用")
        val d = policy["DailyReminderMode"] as Int
        sendMessage(buildMessageChain {
            +"群策略设定状态报告\n"
            +"===============\n"
            +"免打扰状态：   \t${state[policy["undisturbed"] as Int]}\n"
            +"教学许可状态：\t${state[policy["Teaching"] as Int]}\n"
            +"色图许可状态：\t${state[policy["ACGImgAllowed"] as Int]}\n"
            +"订阅状态：\n"
            +"\t>碧蓝航线\t${state[subscribeInfo["AzurLane"] as Int]}\n"
            +"\t>明日方舟\t${state[subscribeInfo["ArKnights"] as Int]}\n"
            +"\t>FGO\t\t${state[subscribeInfo["FateGrandOrder"] as Int]}\n"
            +"\t>原神\t\t${state[subscribeInfo["GenShin"] as Int]}\n"
            +"每日提醒状态：\n"
            +"\t>碧蓝航线\t${state[d % 2]}\n"
            +"\t>FGO\t\t${state[d / 2]}\n"
            +"报时模式：\t${tellTimeMode[policy["TellTimeMode"] as Int]}\n"
            +"对话概率:\t\t${policy["TriggerProbability"]}%\n"
            +"群色图配给：\t${acgImg["score"]}/200\n"
            +"群责任人：\t"
            +At((responsible["principal_ID"] as Int).toLong())
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
            dbObject.update("Policy", "group_id", group.id, "undisturbed", 1)
            sendMessage("夜间免打扰已启用")
        } else {
            dbObject.update("Policy", "group_id", group.id, "undisturbed", 0)
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
        if (tellTimeMode.containsKey(mode)) {
            dbObject.update("Policy", "group_id", group.id, "TellTimeMode", mode)
            sendMessage("报时设定到模式 ${tellTimeMode[mode]}")
        } else {
            if (mode == 0) {
                dbObject.update("Policy", "group_id", group.id, "TellTimeMode", 0)
                sendMessage("已关闭本群报时")
            } else {
                dbObject.update("Policy", "group_id", group.id, "TellTimeMode", -1)
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
        tellTimeMode.forEach {
            info.add("${it.key}\t    ${it.value}")
        }
        sendMessage(
            "无效模式参数，设定失败,请参考以下示范命令\n群策略 报时模式 [模式值]\n——————————\n模式值 | 说明\n${info.joinToString("\n")}\n-\t    标准报时"
        )
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
            dbObject.update("SubscribeInfo", "group_id", group.id, "AzurLane", if (mode and 1 == 1) 1 else 0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "ArKnights", if (mode and 2 == 2) 1 else 0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "FateGrandOrder", if (mode and 4 == 4) 1 else 0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "GenShin", if (mode and 8 == 8) 1 else 0)
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
            模式值使用16进制值保存，下面是计算方法及示例(1:开启,0:关闭)
            Mode | AzLn | Akns | FGO | Gesn
            3[3]        1           1       0       0
            3 = 1*1 + 1*2 + 0*4 + 0*8
            
            7[7]        1           1       1       0
            7 = 1*1 + 1*2 + 1*4 + 0*8
            
            A[10]       0           1       0       1
            A = 0*1 + 1*2 + 0*4 + 1*8
            
            F[15]       1           1       1       1
            F = 1*1 + 1*2 + 1*4 + 1*8
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
                dbObject.update("Policy", "group_id", group.id, "DailyReminderMode", "0")
                sendMessage("已关闭本群每日提醒")
            }
            1, 2, 3 -> {
                dbObject.update("Policy", "group_id", group.id, "DailyReminderMode", mode)
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
            dbObject.update("Policy", "group_id", group.id, "Teaching", 1)
            sendMessage("已开启本群教学模式")
        } else {
            dbObject.update("Policy", "group_id", group.id, "Teaching", 0)
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
        dbObject.update("Policy", "group_id", group.id, "TriggerProbability", value)
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
                dbObject.update("Policy", "group_id", group.id, "ACGImgAllowed", 1)
                sendMessage("无内鬼,可以交易")
            }
            "有内鬼" -> {
                dbObject.update("Policy", "group_id", group.id, "ACGImgAllowed", 0)
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
        record(primaryName)
        return user.permission.isOperator().not()
    }

    @SubCommand("续费")
    suspend fun MemberCommandSenderOnMessage.renewal(cdKey: String, key: String) {
        if (group.botMuteRemaining > 0) return
        val cdKeyDbObject = SQLiteJDBC(PluginMain.resolveDataPath("CD-KEY.db"))
        val cdk = cdKeyDbObject.selectOne("CD-KEY", "code", cdKey, 1)
        if (cdk.isEmpty() || cdk["key"] != key){
            sendMessage("该序列号或密码无效,请检查输入(若确定输入无误请联系客服)")
            return
        }

        cdKeyDbObject.delete("CD-KEY", "code", cdKey)
        cdKeyDbObject.closeDB()

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val groupObject = dbObject.selectOne("Responsible", "group_id", group.id, 1)
        val onlyTime = (groupObject["active"] as Int) + (cdk["value"] as Int) + if (LocalDateTime.now().hour < 12) 0 else 1

        dbObject.update("Responsible", "group_id", "${group.id}",
            arrayOf("principal_ID","active"),
            arrayOf("${user.id}","$onlyTime" ))
        dbObject.closeDB()
        sendMessage("续费完成,可以通过群策略设定状态汇报命令查看当前群状态")
    }

    @SubCommand("续费")
    suspend fun MemberCommandSenderOnMessage.renewal() {
        if (group.botMuteRemaining > 0) return
        sendMessage(
            """
            无效参数，设定失败,请参考以下示范命令
            续费 [序列号] [密码]
            ——————————
            将认定发起续费的账号为当前群的Bot归属人
            根据输入的序列号对使用期限进行续费
            """.trimIndent()
        )
    }
}
