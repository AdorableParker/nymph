package com.navigatorTB_Nymph.defaultJob

import com.navigatorTB_Nymph.command.simple.SendDynamic
import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.data.UserSubscribeInfo
import com.navigatorTB_Nymph.miscellaneous.Dynamic
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MyPluginData
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.cronJob.PeriodicTask
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.time.LocalDateTime

object DynamicPush : PeriodicTask("动态推送", "查询并推送最新B站动态", Interval(0, 0, 3)) {
    /* 动态推送 */
    override suspend fun run() {
        PluginMain.logger.info { "执行任务：动态推送" }
        val time = LocalDateTime.now().hour
        for (list in MyPluginData.timeStampOfDynamic) {
            val dynamicInfo = SendDynamic.getDynamic(list.key, 0, flag = true)
            if (dynamicInfo.timestamp == 0L) continue
            val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
            val groupList = MyPluginData.nameOfDynamic[list.key]?.let {
                with(
                    if (time in MySetting.undisturbed) dbObject.executeQuerySQL(
                        "SELECT * FROM Policy JOIN SubscribeInfo USING (groupID) WHERE Policy.undisturbed = false AND SubscribeInfo.${it} = true;",
                        "动态推送\nFile:PluginMain.kt\tLine:433"
                    )
                    else dbObject.select("SubscribeInfo", Triple(it, "=", "1"), "动态推送\nFile:PluginMain.kt\tLine:437")
                ) { List(this.size) { index -> UserSubscribeInfo(this[index]) } }
            }
            dbObject.closeDB()

            if (groupList.isNullOrEmpty()) continue

            val bot = Bot.getInstance(MySetting.BotID)
            val gList = mutableListOf<Contact>()
            groupList.forEach {
                if (it.groupID in ActiveGroupList.user) { // 激活到期则跳过
                    val g = bot.getGroup(it.groupID)
                    if ((g != null) && (g.botMuteRemaining <= 0)) gList.add(g)
                }
            }
            val dynamic = Dynamic(dynamicInfo)
            dynamic.layoutDynamic()
            val forwardMessage = dynamic.draw().uploadAsImage(gList.random())
            gList.forEach { runCatching { it.sendMessage(forwardMessage) }.onFailure { err -> PluginMain.logger.warning { "File:PluginMain.kt\tLine:453\nGroup:${it.id}\n${err.message}" } } }
        }
    }
}