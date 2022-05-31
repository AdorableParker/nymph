package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.data.AssetDataAzurLaneConstructTime
import com.navigatorTB_Nymph.game.simulateCardDraw.AzleBuild
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

object SimulateConstruction : SimpleCommand(
    PluginMain, "SC", "模拟建造",
    description = "碧蓝航线模拟建造"
) {
    override val usage = """
        ${CommandManager.commandPrefix}模拟建造 [建造池]
        建造池列表:
        限|  限定池(所有限定船同池)
        轻|  轻型池
        重|  重型池
        特|  特型池
        """.trimIndent()

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(mode: String, count: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (count !in 1..10) {
            sendMessage("建造次数应为 [1, 10] 区间内")
            return
        }

        when (mode) {
            "轻" -> {
                val r1 = GameMain(this).lightPool(count)
                if (r1.isBlank()) build(1000, count).draw().uploadAsImage(group) else PlainText(r1)
            }
            "重" -> {
                val r1 = GameMain(this).heavyPool(count)
                if (r1.isBlank()) build(100, count).draw().uploadAsImage(group) else PlainText(r1)
            }
            "特" -> {
                val r1 = GameMain(this).heavyPool(count)
                if (r1.isBlank()) build(10, count).draw().uploadAsImage(group) else PlainText(r1)
            }
            "限" -> {
                val r1 = GameMain(this).heavyPool(count)
                if (r1.isBlank()) limit(count).draw().uploadAsImage(group) else PlainText(r1)
            }
            else -> PlainText("未知模式")
        }.let { sendMessage(it) }
    }

    /*
        switch_1 = {"轻型舰": 1000, "重型舰": 100, "特型舰": 10, "限定舰": 1}
        switch_2 = ("传奇", "超稀有", "精锐", "稀有", "普通","无")
     */
    private fun limit(count: Int): AzleBuild {
        val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val rL = List(count) {
            val level = when ((1..1000).random()) {
                in 1..12 -> 0
                in 13..82 -> 1
                in 83..202 -> 2
                in 203..712 -> 3
                else -> 4
            }
            objDB.executeQuerySQL(
                "SELECT * FROM AzurLaneConstructTime WHERE type % 10 == $level;",
                "模拟建造\nFile:MirrorWorldGame.kt\tLine:418"
            ).random().run {
                AssetDataAzurLaneConstructTime(this)
            }
        }
        objDB.closeDB()
        return AzleBuild(count).drawCard(rL)
    }

    private fun build(mode: Int, count: Int): AzleBuild {
        val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val rL = List(count) {
            val level = when ((1..1000).random()) {
                in 1..7 -> 1
                in 8..19 -> 2
                in 20..45 -> 3
                else -> 4
            }
            objDB.executeQuerySQL(
                "SELECT * FROM AzurLaneConstructTime WHERE limitedTime = 0 AND type / $mode % 10 == $level;",
                "模拟建造\nFile:MirrorWorldGame.kt\tLine:418"
            ).random().run { AssetDataAzurLaneConstructTime(this) }
        }
        objDB.closeDB()

        return AzleBuild(count).drawCard(rL)
//            return "本次结果：\n船名：${l.originalName}[${l.alias}]\t建造时间：${l.time}"
    }
}