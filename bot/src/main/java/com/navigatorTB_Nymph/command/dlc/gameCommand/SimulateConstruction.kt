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
    suspend fun MemberCommandSenderOnMessage.main(mode: String, cunt: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (cunt !in 1..10) {
            sendMessage("建造次数应为 [1, 10] 区间内")
            return
        }

        val message = if (PluginMain.DLC_MirrorWorld) {
            when (mode) {
                "限" -> {
                    val r1 = GameMain(this).heavyPool(cunt)
                    if (r1.isBlank()) limit(cunt).draw().uploadAsImage(group) else PlainText(r1)
                }
                "轻" -> {
                    val r1 = GameMain(this).lightPool(cunt)
                    if (r1.isBlank()) build(20, cunt).draw().uploadAsImage(group) else PlainText(r1)
                }
                "重" -> {
                    val r1 = GameMain(this).heavyPool(cunt)
                    if (r1.isBlank()) build(30, cunt).draw().uploadAsImage(group) else PlainText(r1)
                }
                "特" -> {
                    val r1 = GameMain(this).heavyPool(cunt)
                    if (r1.isBlank()) build(50, cunt).draw().uploadAsImage(group) else PlainText(r1)
                }
                else -> PlainText("未知模式")
            }
        } else PlainText("缺少依赖DLC")
        sendMessage(message)
    }


    private fun limit(cunt: Int): AzleBuild {
        val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val rL = List(cunt) {
            val sql = when ((1..1000).random()) {
                in 1..20 -> "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 1.0;"
                in 21..89 -> "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 0.0 AND type % 10 == 0;"
                in 90..206 -> "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 0.0 AND (type - 1) % 10 == 0;"
                in 207..706 -> "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 0.0 AND (type - 2) % 10 == 0;"
                else -> "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 0.0 AND (type - 3) % 10 == 0;"
            }
            objDB.executeQuerySQL(sql, "模拟建造\nFile:MirrorWorldGame.kt\tLine:418").random().run {
                AssetDataAzurLaneConstructTime(this)
            }
        }
        objDB.closeDB()
        return AzleBuild(cunt).drawCard(rL)
    }

    private fun build(mode: Int, cunt: Int): AzleBuild {
        val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val rL = List(cunt) {
            val level = when ((1..100).random()) {
                in 1..7 -> 0
                in 8..19 -> 1
                in 20..45 -> 2
                else -> 3
            }
            objDB.executeQuerySQL(
                "SELECT * FROM AzurLaneConstructTime WHERE LimitedTime = 0.0 AND (type - $level) % $mode == 0;",
                "模拟建造\nFile:MirrorWorldGame.kt\tLine:418"
            ).random().run { AssetDataAzurLaneConstructTime(this) }
        }
        objDB.closeDB()

        return AzleBuild(cunt).drawCard(rL)
//            return "本次结果：\n船名：${l.originalName}[${l.alias}]\t建造时间：${l.time}"
    }
}