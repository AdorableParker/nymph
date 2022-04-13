package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.AssetDataRoster
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object Roster : SimpleCommand(
    PluginMain, "Roster", "船名查询", "和谐名",
    description = "碧蓝航线船名查询"
) {
    override val usage: String = "${CommandManager.commandPrefix}船名查询 [船名]"
    private val dataDir = PluginMain.resolveDataPath("AssetData.db")

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage("参数不匹配, 你是否想执行:\n $usage")
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(shipName: String) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val i = shipName.toCharArray()
        i.forEachIndexed { index, char ->
            if (char.isLowerCase()) i[index] = char.uppercaseChar()
        }
        val treated = String(i)
        val dbObject = SQLiteJDBC(dataDir)
        val roster = dbObject.select(
            "Roster",
            Triple(arrayOf("code", "name"), Array(2) { "GLOB" }, Array(2) { "*$treated*" }),
            "OR",
            "船名查询\nFile:Roster.kt\tLine:43"
        ).run { List(size) { AssetDataRoster(this[it]) } }
        dbObject.closeDB()
        if (roster.isEmpty()) {
            sendMessage("没有或尚未收录名字包含有 $shipName 的舰船")
            return
        }
        val report = mutableListOf("名字包含有 $shipName 的舰船有:")
        roster.sortedWith(
            compareBy(
                { it.name.length },
                { it.name }
            )
        ).forEach { report.add("原名：${it.name}\t和谐名：${it.code}") }
        sendMessage(report.joinToString("\n"))
    }
}