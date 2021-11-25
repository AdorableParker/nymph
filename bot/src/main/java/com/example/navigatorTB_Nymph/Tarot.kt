/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File
import java.time.LocalDateTime
import kotlin.random.Random


@MiraiExperimentalApi
@ConsoleExperimentalApi
object Tarot : SimpleCommand(
    PluginMain, "DailyTarot", "每日塔罗",
    description = "塔罗占卜"
) {
    override val usage: String = "${CommandManager.commandPrefix}每日塔罗"

    @Handler
    @MiraiExperimentalApi
    suspend fun MemberCommandSenderOnMessage.main() {
        record(primaryName)
        if (group.botMuteRemaining > 0) return

        val r = divineTarot(user.id)
//        PluginMain.logger.info { r["ImgPath"] }
        r["ImgPath"]?.let { path ->
            File(PluginMain.resolveDataPath(path).toString()).toExternalResource().use {
                sendMessage(
                    PlainText("${r["Brand"]}\n牌面含义关键词:${r["word"]}") + group.uploadImage(it)
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
        val r = dbObject.selectOne("Tarot", "Brand", brand)
        dbObject.closeDB()
        r["seeds"] = seeds
        return when ((0..100).random(Random(seeds))) {
            in 0..50 -> mapOf(
                "side" to "顺位",
                "Brand" to brand,
                "word" to r["Upright"].toString(),
                "ImgPath" to r["uprightImg"].toString()
            )
            else -> mapOf(
                "side" to "逆位",
                "Brand" to brand,
                "word" to r["Reversed"].toString(),
                "ImgPath" to r["invertImg"].toString()
            )
        }
    }
}