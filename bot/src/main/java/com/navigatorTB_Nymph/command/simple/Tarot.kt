package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.AssetDataTarot
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File
import java.time.LocalDateTime
import kotlin.random.Random

object Tarot : SimpleCommand(
    PluginMain, "DailyTarot", "每日塔罗",
    description = "塔罗占卜"
) {
    override val usage: String = "${CommandManager.commandPrefix}每日塔罗"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val r = divineTarot(user.id)
        r["ImgPath"]?.let { path ->
            File(PluginMain.resolveDataPath(path).toString()).toExternalResource().use {
                sendMessage(
                    PlainText("${r["side"]}${r["Brand"]}\n牌面含义关键词:${r["word"]}") + group.uploadImage(it)
                )
            }
        } ?: sendMessage("占卜失败")

    }

    fun divineTarot(uid: Long): Map<String, String> {
        val today = LocalDateTime.now()
        var i = 1
        while (uid / 10 * i <= 0) i *= 10
        val seeds = (today.year * 1000L + today.dayOfYear) * i + uid

        val brand = listOf(
            "The Fool(愚者)",
            "The Magician(魔术师)", "The High Priestess(女祭司)", "The Empress(女王)", "The Emperor(皇帝)", "The Hierophant(教皇)",
            "The Lovers(恋人)", "The Chariot(战车)", "Strength(力量)", "The Hermit(隐者)", "Wheel of Fortune(命运之轮)",
            "Justice(正义)", "The Hanged Man(倒吊人)", "Death(死神)", "Temperance(节制)", "The Devil(恶魔)",
            "The Tower(塔)", "The Star(星星)", "The Moon(月亮)", "The Sun(太阳)", "Judgement(审判)",
            "The World(世界)"
        ).random(Random(seeds))

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val tarot =
            AssetDataTarot(
                dbObject.selectOne(
                    "Tarot",
                    Triple("brand", "=", "'$brand'"),
                    "每日塔罗\nFile:Tarot.kt\tLine:59"
                )
            )
        dbObject.closeDB()
        return when ((0..100).random(Random(seeds))) {
            in 0..50 -> mapOf(
                "side" to "判定！顺位-",
                "Brand" to brand,
                "word" to tarot.upright,
                "ImgPath" to tarot.uprightImg
            )
            else -> mapOf(
                "side" to "判定！逆位-",
                "Brand" to brand,
                "word" to tarot.reversed,
                "ImgPath" to tarot.invertImg
            )
        }
    }
}