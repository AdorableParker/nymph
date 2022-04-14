package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.AssetDataShipMap
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object ShipMap : SimpleCommand(
    PluginMain, "ShipMap", "打捞定位",
    description = "碧蓝航线舰船打捞定位"
) {
    override val usage = "${CommandManager.commandPrefix}打捞定位 <船名|地图坐标>"
    private val dataDir = PluginMain.resolveDataPath("AssetData.db")

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(uncheckedIndex: String) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val pretreatmentIndex = uncheckedIndex.replace("—", "-").toCharArray()
        pretreatmentIndex.forEachIndexed { index, char ->
            if (char.isLowerCase()) pretreatmentIndex[index] = char.uppercaseChar()
        }
        val treatedIndex = String(pretreatmentIndex)
        val index = Regex("\\d*?-\\d").find(treatedIndex)?.value
        index?.let { sendMessage(mapToName(index)) } ?: sendMessage(nameToMap(treatedIndex))
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage("参数不匹配, 你是否想执行:\n $usage")
    }

    private fun nameToMap(index: String): String {
        val db = SQLiteJDBC(dataDir)
        val result = db.select(
            "ShipMap",
            Triple(arrayOf("originalName", "alias"), Array(2) { "GLOB" }, Array(2) { "*$index*" }),
            "OR",
            "打捞定位\nFile:ShipMap.kt\tLine:48"
        ).run { List(size) { AssetDataShipMap(this[it]) } }
        db.closeDB()
        if (result.isEmpty()) return "没有或尚未收录名字包含有 $index 的主线图可打捞舰船"
        val report = mutableListOf("名字包含有 $index 的可打捞舰船有:")
        result.sortedWith(
            compareBy(
                { it.special.length },
                { it.rarity.length },
                { it.originalName.length },
                { it.alias.length },
                { it.originalName },
                { it.alias }
            )
        ).forEach {
            report.add("船名：${it.originalName}[${it.alias}]-${it.rarity}\t可打捞地点:")
            if (it.special.isEmpty()) {
                var i = 1
                arrayOf(
                    it.chapter1, it.chapter2, it.chapter3, it.chapter4, it.chapter5, it.chapter6, it.chapter7,
                    it.chapter8, it.chapter9, it.chapter10, it.chapter11, it.chapter12, it.chapter13, it.chapter14
                ).forEach { chapter ->
                    if (chapter != 0)
                        report.add(
                            (if (chapter and 1 == 1) "$i-1\t" else "") +
                                    (if (chapter and 2 == 2) "$i-2\t" else "") +
                                    (if (chapter and 4 == 4) "$i-3\t" else "") +
                                    if (chapter and 8 == 8) "$i-4\t" else ""
                        )
                    i++
                }
            } else report.add(it.special)
        }
        return report.joinToString("\n")
    }

    private fun mapToName(index: String): String {
        val coordinate = index.split("-")
        val db = SQLiteJDBC(dataDir)
        val site = 1 shl coordinate[1].toInt() - 1
        val result = db.executeQuerySQL(
            "SELECT * FROM ShipMap WHERE chapter$coordinate & $site = $site",
            "打捞定位\nFile:ShipMap.kt\tLine:93"
        ).run {
            List(size) { AssetDataShipMap(this[it]) }
        }
        db.closeDB()
        if (result.isEmpty()) return "没有记录主线图坐标为 $index 的地图"
        val report = mutableListOf("可在 $index 打捞的舰船有:")
        result.sortedWith(
            compareBy(
                { it.rarity },
                { it.originalName.length },
                { it.alias.length },
                { it.originalName },
                { it.alias }
            )
        ).forEach { report.add("${it.originalName}[${it.alias}]-${it.rarity}") }
        return report.joinToString("\n")
    }
}