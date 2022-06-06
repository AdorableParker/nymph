package com.navigatorTB_Nymph.defaultJob

import com.navigatorTB_Nymph.data.AssetDataScript
import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.data.UserPolicy
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.cronJob.PeriodicTask
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import java.io.File
import java.time.LocalDateTime

object TellTime : PeriodicTask("报时", "整点的报时", Interval(0, 1, 0)) {
    /* 报时 */
    override suspend fun run() {
        PluginMain.logger.info { "执行任务：整点报时" }
        val time = LocalDateTime.now().hour

        if (time == 0) ActiveGroupList.activationStatusUpdate() // 更新激活状态

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val scriptList = dbObject.select(
            "Script", Triple("hour", "=", "$time"), "报时\nFile:PluginMain.kt\tLine:375"
        ).run { List(this.size) { AssetDataScript(this[it]) } }
        dbObject.closeDB()

        val userDbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val groupList = with(
            if (time in MySetting.undisturbed) userDbObject.select(
                "Policy",
                Triple(arrayOf("undisturbed", "tellTimeMode"), Array(2) { "!=" }, arrayOf("1", "0")),
                "AND",
                "报时\nFile:PluginMain.kt\tLine:384"
            ) else userDbObject.select(
                "Policy", Triple("tellTimeMode", "!=", "0"), "报时\nFile:PluginMain.kt\tLine:389"
            )
        ) { List(this.size) { UserPolicy(this[it]) } }
        userDbObject.closeDB()
        val script = mutableMapOf<Int, List<AssetDataScript>>()

        for (groupPolicy in groupList) {
            if (groupPolicy.groupID !in ActiveGroupList.user) continue // 激活到期则跳过
            val group = Bot.getInstance(MySetting.BotID).getGroup(groupPolicy.groupID)
            if (group == null || group.botMuteRemaining > 0) continue
            if (groupPolicy.tellTimeMode == -1) {
                group.sendMessage("现在${time}点咯")
                continue
            }

            if (script.containsKey(groupPolicy.tellTimeMode).not()) {
                script[groupPolicy.tellTimeMode] = scriptList.filter { it.mode == groupPolicy.tellTimeMode }
            }

            val outScript = script[groupPolicy.tellTimeMode]?.random()?.content

            if (groupPolicy.tellTimeMode % 2 == 0) {
                val path = PluginMain.resolveDataPath("./报时语音/$outScript")
                val audio = File("$path").toExternalResource().use { group.uploadAudio(it) }
                audio.let { group.sendMessage(it) }
            } else {
                outScript?.let { group.sendMessage(it) }
            }
        }
    }
}