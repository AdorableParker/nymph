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
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
@ConsoleExperimentalApi
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
        *7* 责任人绑定
        *8* 继承到群
        *9* 撤销继承协议
        """.trimIndent()

    @SubCommand("报时模式")
    suspend fun MemberCommandSenderOnMessage.tellTime(mode: Int) {
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
        val info: MutableList<String> = mutableListOf()
        tellTimeMode.forEach {
            info.add("${it.key}\t    ${it.value}")
        }
        sendMessage(
            "无效模式参数，设定失败,请参考以下示范命令\n群策略 报时模式 [模式值]\n——————————\n模式值 | 说明\n${info.joinToString("\n")}\n-\t    标准报时"
        )
    }

    @SubCommand("订阅模式")
    suspend fun MemberCommandSenderOnMessage.subscription(mode: String) {
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val i = mode.toIntOrNull(16)
        if (i != null && i >= 0) {
            val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
            dbObject.update("SubscribeInfo", "group_id", group.id, "AzurLane", if (i and 1 == 1) 1.0 else 0.0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "ArKnights", if (i and 2 == 2) 1.0 else 0.0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "FateGrandOrder", if (i and 4 == 4) 1.0 else 0.0)
            dbObject.update("SubscribeInfo", "group_id", group.id, "GenShin", if (i and 8 == 8) 1.0 else 0.0)
            sendMessage("订阅设定到模式$mode")
            dbObject.closeDB()
        } else {
            subscription()
        }
    }

    @SubCommand("订阅模式")
    suspend fun MemberCommandSenderOnMessage.subscription() {
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
        if (permissionCheck(user)) {
            sendMessage("权限不足")
            return
        }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        if (switch > 0) {
            dbObject.update("Policy", "group_id", group.id, "Teaching", 1.0)
            sendMessage("已开启本群教学模式")
        } else {
            dbObject.update("Policy", "group_id", group.id, "Teaching", 0.0)
            sendMessage("已关闭本群教学模式")
        }
        dbObject.closeDB()
    }

    @SubCommand("教学许可")
    suspend fun MemberCommandSenderOnMessage.teaching() {
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

    @SubCommand("责任人绑定")
    suspend fun MemberCommandSenderOnMessage.bindingOwnership(string: String) {
        if (group.botMuteRemaining > 0) return
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val rpl = dbObject.selectOne("Responsible", "group_id", group.id, 1)
        val nowPR = rpl["principal_ID"].toString().toLong()
        when (string) {
            "解绑" -> {
                if (nowPR == user.id) {
                    dbObject.update("Responsible", "group_id", "${group.id}", "principal_ID", 0)
                    sendMessage("本群责任人解绑完成,请尽快绑定相关责任人,防止出现使用问题")
                } else {
                    sendMessage("你不是本群责任人,无法解绑")
                }
            }
            "绑定" -> {
                if (nowPR == 0L) {
                    dbObject.update("Responsible", "group_id", "${group.id}", "principal_ID", user.id)
                    sendMessage("本群责任人绑定完成\nGroup ID:${group.id}\tPrincipal ID: ${user.id}")
                } else {
                    sendMessage(PlainText("本群已有责任人:") + At(nowPR) + PlainText("\n原责任人解绑后方可绑定"))
                }
            }
            else -> bindingOwnership()
        }
        dbObject.closeDB()
    }

    @SubCommand("责任人绑定")
    suspend fun MemberCommandSenderOnMessage.bindingOwnership() {
        sendMessage(
            """
            无效参数，设定失败,请参考以下示范命令
            群责任人绑定 [绑定|解绑]
            ——————————
            绑定责任人用于认定当前群的Bot使用权限最高归属人
            !!若因未绑定而导致后期BOt使用管控问题,后果自负...(相关自助功能使用将会受限,也将维权繁琐)
            """.trimIndent()
        )
    }

    @SubCommand("继承到群")
    suspend fun MemberCommandSenderOnMessage.succeed(ancestor: Long, inheritor: Long) {
        if (group.botMuteRemaining > 0) return

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val rpl = dbObject.selectOne("Responsible", "group_id", ancestor, 1)
        dbObject.closeDB()

        val nowPR = rpl["principal_ID"].toString().toLong()
        if (nowPR == user.id) {
            if (MyPluginData.pactList.contains(ancestor)) {
                sendMessage("群${ancestor}已有继承协议,请撤销后重新签署")
                return
            }
            MyPluginData.groupIdList[inheritor] = GroupCertificate(nowPR, true, ancestor)
            MyPluginData.pactList.add(ancestor)
            sendMessage(PlainText("继承协议已建立\n被继承群：$ancestor\n受继承群：$inheritor\n继承协议签署人：") + At(nowPR))
        } else {
            sendMessage("你不是该群责任人,无法继承")
        }
    }

    @SubCommand("继承到群")
    suspend fun MemberCommandSenderOnMessage.succeed(inheritor: Long) {
        succeed(group.id, inheritor)
    }

    @SubCommand("继承到群")
    suspend fun MemberCommandSenderOnMessage.succeed() {
        sendMessage(
            """
            无效参数，设定失败,请参考以下示范命令
            继承到群 [被继承群] [受继承群]
            ——————————
            参数[被继承群] 若是当前群可省略
            本功能仅绑定[被继承群]的责任人有权执行
            执行本命令后，直接邀请至目标群，同意申请后[被继承群]将自动退群
            """.trimIndent()
        )
    }

    @SubCommand("撤销继承协议")
    suspend fun MemberCommandSenderOnMessage.revokePact(ancestor: Long, inheritor: Long) {
        if (group.botMuteRemaining > 0) return

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val rpl = dbObject.selectOne("Responsible", "group_id", ancestor, 1)
        dbObject.closeDB()
        val nowPR = rpl["principal_ID"].toString().toLong()
        if (nowPR == user.id) {
            MyPluginData.groupIdList.remove(inheritor)
            MyPluginData.pactList.remove(ancestor)
            sendMessage("继承协议已作废")
        } else {
            sendMessage("你不是该群责任人,无法撤销继承协议")
        }
    }

    @SubCommand("撤销继承协议")
    suspend fun MemberCommandSenderOnMessage.revokePact(inheritor: Long) {
        revokePact(group.id, inheritor)
    }

    @SubCommand("撤销继承协议")
    suspend fun MemberCommandSenderOnMessage.revokePact() {
        sendMessage(
            """
            无效参数，设定失败,请参考以下示范命令
            撤销继承协议 [被继承群] [受继承群]
            ——————————
            参数[被继承群] 若是当前群可省略
            本功能仅绑定[被继承群]的责任人有权执行
            执行本命令后，撤销[被继承群]与[受继承群]的继承协议
            """.trimIndent()
        )
    }

}
