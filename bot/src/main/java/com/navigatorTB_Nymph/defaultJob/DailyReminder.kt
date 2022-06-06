package com.navigatorTB_Nymph.defaultJob

import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.data.UserPolicy
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.cronJob.PeriodicTask
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.time.LocalDateTime

object DailyReminder : PeriodicTask("每日提醒", "每天八点提醒每日活动", Interval(1, 0, 0)) {
    /* 每日提醒 */
    override suspend fun run() {
        PluginMain.logger.info { "执行任务：每日提醒" }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val policyList = dbObject.select(
            "Policy", Triple("DailyReminderMode", "!=", "0"), "每日提醒\nFile:PluginMain.kt\tLine:322"
        ).run { List(this.size) { UserPolicy(this[it]) } }
        dbObject.closeDB()
        val script = mapOf(
            1 to arrayOf(
                "Ciallo～(∠・ω< )⌒★今天是周一哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周二哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周三哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周四哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周五哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周六哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周日哦,每日全部模式开放，每周两次的破交作战记得打哦，困难模式也别忘了。"
            ), 2 to arrayOf(
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周一, 今天周回本开放「弓阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周二, 今天周回本开放「枪阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周三, 今天周回本开放「狂阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周四, 今天周回本开放「骑阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周五, 今天周回本开放「术阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周六, 今天周回本开放「杀阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周日, 今天周回本开放「剑阶修炼场」,「收集火种(All)」。"
            )
        )
        for (groupPolicy in policyList) {
            if (groupPolicy.groupID !in ActiveGroupList.user) continue // 激活到期则跳过

            val group = Bot.getInstance(MySetting.BotID).getGroup(groupPolicy.groupID)
            if (group == null || group.botMuteRemaining > 0) {
                continue
            }
            when (groupPolicy.dailyReminderMode) {
                1 -> script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                2 -> script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                3 -> {
                    script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                    script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                }
                else -> PluginMain.logger.warning { "File:PluginMain.kt\tLine:362\n未知的模式" }
            }
        }
    }
}