package com.navigatorTB_Nymph.command.composite


import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.main.PluginMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.LocalDateTime

import com.navigatorTB_Nymph.command.simple.Birthday.main as birthday
import com.navigatorTB_Nymph.command.simple.Tarot.main as tarot

object Schedule : CompositeCommand(
    PluginMain, "Schedule", "日程",
    description = "日程表"
) {
    @OptIn(MiraiExperimentalApi::class)
    @SubCommand("添加")
    suspend fun MemberCommandSenderOnMessage.addSchedule(startTime: Int, content: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val t = LocalDateTime.now()
        val serialNumber = t.dayOfYear * 10000 + t.hour * 100 + t.minute //序列号

        val m = startTime % 100
        val h = (startTime - m) % 10000 / 100
        val d = (startTime - h - m) / 10000

        if (d > 366 || h > 23 || m > 59 || serialNumber >= startTime) {
            sendMessage("任务开始时间错误，请核实,现在时间为$serialNumber")
            return
        }

        when (content) {
            "舰娘生日", "历史今天" -> PluginMain.CRON.addJob(startTime, Interval(0, 0, 0)) {
                if (group.botMuteRemaining > 0) return@addJob
                sendMessage(At(user))
                birthday()
            }
            "DailyTarot", "每日塔罗" -> PluginMain.CRON.addJob(startTime, Interval(0, 0, 0)) {
                if (group.botMuteRemaining > 0) return@addJob
                sendMessage(At(user))
                tarot()
            }
            else -> PluginMain.CRON.addJob(startTime, Interval(0, 0, 0)) {
                if (group.botMuteRemaining > 0) return@addJob
                sendMessage(At(user) + PlainText(content))
            }
        }
        sendMessage("添加提醒完成\n预计在今年第${d}日${h}时${m}分提醒")
    }

    @SubCommand("查询")
    suspend fun MemberCommandSenderOnMessage.querySchedule(startTime: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(PluginMain.CRON.query(startTime))
    }
}