package com.navigatorTB_Nymph.command.composite


import com.navigatorTB_Nymph.defaultJob.AtMe
import com.navigatorTB_Nymph.defaultJob.UnMute
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.cronJob.CronJob
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.syncFromEvent
import java.time.LocalDateTime

object Schedule : CompositeCommand(
    PluginMain, "Schedule", "日程",
    description = "日程表"
) {
    @SubCommand("添加任务")
    suspend fun MemberCommandSenderOnMessage.addSchedule(command: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val t = LocalDateTime.now()
        val serialNumber = t.dayOfYear * 10000 + t.hour * 100 + t.minute //序列号

        sendMessage("现在时间序列为$serialNumber,请设定任务执行时间")

        val startTime = withTimeoutOrNull(30_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Int> {
                if (it.sender == user) it.message.toString().toIntOrNull() else null
            }
        } ?: 0

        if (serialNumber >= startTime) {
            sendMessage("任务时间设定无效,任务添加失败")
            return
        }

        when (command) {
            "解除禁言" -> CronJob.addJob(startTime, UnMute(group, user))
            "@我" -> CronJob.addJob(startTime, AtMe(group, user))
//            "禁言" -> CronJob.addJob(startTime, Mute(group, user,))
//            "命令" ->
            else -> {
                sendMessage("其他命令尚未完成")
                return
            }
        }
        sendMessage("添加提醒完成")
    }

    @SubCommand("查询")
    suspend fun MemberCommandSenderOnMessage.querySchedule() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(CronJob.query())
    }

    @SubCommand("查询")
    suspend fun MemberCommandSenderOnMessage.querySchedule(startTime: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(CronJob.query(startTime))
    }
}