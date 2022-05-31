package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.AssetDataAzurLaneConstructTime
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object Construction : SimpleCommand(
    PluginMain, "Construction", "建造时间",
    description = "碧蓝航线建造时间查询"
) {
    override val usage: String = "${CommandManager.commandPrefix}建造时间 [时间|船名]"
    private val dataDir = PluginMain.resolveDataPath("AssetData.db")

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(uncheckedIndex: String) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pretreatmentIndex = uncheckedIndex.replace('：', ':').toCharArray()
        pretreatmentIndex.forEachIndexed { index, char ->
            if (char.isLowerCase()) pretreatmentIndex[index] = char.uppercaseChar()
        }
        val treatedIndex = String(pretreatmentIndex)
        val index = Regex("\\d:\\d\\d").find(treatedIndex)?.value
        index?.let { sendMessage(timeToName(index)) } ?: sendMessage(nameToTime(treatedIndex))
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

    private fun timeToName(index: String): String {
        val db = SQLiteJDBC(dataDir)
        val result = db.select(
            "AzurLaneConstructTime",
            Triple("time", "GLOB", "'$index*'"),
            "建造时间\nFile:Construction.kt\tLine:48"
        ).run { List(size) { AssetDataAzurLaneConstructTime(this[it]) } }
        db.closeDB()
        if (result.isEmpty()) return "没有或尚未收录建造时间为 $index 的可建造舰船"
        val report = mutableListOf("建造时间为 $index 的舰船有:")
        result.sortedBy { it.originalName.length }.sortedWith(
            compareBy(
                { it.limitedTime },
                { it.originalName.length },
                { it.alias.length },
                { it.originalName },
                { it.alias }
            )
        ).forEach {
            report.add(
                "船名：${it.originalName}[${it.alias}]\t${
                    when (it.limitedTime) {
                        1 -> "限时"
                        2 -> "建造绝版"
                        else -> "常驻"
                    }
                }"
            )
        }
        db.closeDB()
        return report.joinToString("\n")
    }

    private fun nameToTime(index: String): String {
        val db = SQLiteJDBC(dataDir)
        val result = db.select(
            "AzurLaneConstructTime",
            Triple(arrayOf("originalName", "alias"), Array(2) { "GLOB" }, Array(2) { "'*$index*'" }),
            "OR",
            "建造时间\nFile:Construction.kt\tLine:81"
        ).run { List(size) { AssetDataAzurLaneConstructTime(this[it]) } }
        db.closeDB()
        if (result.isEmpty()) return "没有或尚未收录名字包含有 $index 的可建造舰船"
        val report = mutableListOf("名字包含有 $index 的可建造舰船有:")
        result.sortedWith(
            compareBy(
                { it.limitedTime },
                { it.time.split(":")[0] },
                { it.time.split(":")[1] },
                { it.time.split(":")[2] },
                { it.originalName.length },
                { it.alias.length },
                { it.originalName },
                { it.alias }
            )
        ).forEach {
            report.add(
                "船名：${it.originalName}[${it.alias}]\t建造时间：${it.time}\t${
                    when (it.limitedTime) {
                        1 -> "限时"
                        2 -> "建造绝版"
                        else -> "常驻"
                    }
                }"
            )
        }
        return report.joinToString("\n")
    }
}