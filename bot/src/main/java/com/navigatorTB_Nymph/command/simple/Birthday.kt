package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Birthday : SimpleCommand(
    PluginMain, "舰娘生日", "历史今天",
    description = "历史今日下水舰船"
) {
    override val usage: String = "${CommandManager.commandPrefix}舰娘生日"

    @MiraiExperimentalApi
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("M月d日"))

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val r = dbObject.select("ShipBirthday", "LaunchDay", today)
        dbObject.closeDB()
        if (r.isEmpty()) {
            sendMessage("今天生日的舰娘没有记载哦")
            return
        }
        val chain = MessageChainBuilder()
        r.forEach {
            (it["path"] as String).let { path ->
                File(PluginMain.resolveDataPath(path).toString()).toExternalResource().use { er ->
                    chain.add(group.uploadImage(er))
                }
            }
            chain.add("${it["LaunchYear"]}年的今天,${it["Name"]}下水")
        }
        sendMessage(chain.build())
    }
}