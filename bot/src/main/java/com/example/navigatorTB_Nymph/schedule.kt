package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi


@MiraiExperimentalApi
@ConsoleExperimentalApi
object Schedule : CompositeCommand(
    PluginMain, "Schedule", "日程",
    description = "日程表"
) {
    @SubCommand("添加提醒")
    suspend fun MemberCommandSenderOnMessage.main(startTime: Int, content: String) {
        if (group.botMuteRemaining > 0) return

        val m = startTime % 100
        val h = (startTime - m) % 10000 / 100
        val d = (startTime - h - m) / 10000

        PluginMain.CRON.addJob(startTime, Interval(0, 0, 0)) {
            if (group.botMuteRemaining > 0) return@addJob
            sendMessage(At(user) + PlainText(content))
        }
        sendMessage("添加提醒完成\n预计在今年第${d}日${h}时${m}分提醒")
    }
}
