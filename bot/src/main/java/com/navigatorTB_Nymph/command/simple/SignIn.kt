package com.navigatorTB_Nymph.command.simple

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.navigatorTB_Nymph.data.AssetDataTarot
import com.navigatorTB_Nymph.data.Role
import com.navigatorTB_Nymph.game.signIn.SignInSVG
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.seed.Cycle
import com.navigatorTB_Nymph.tool.seed.Seed
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import org.jsoup.Jsoup
import java.time.LocalDateTime

object SignIn : SimpleCommand(
    PluginMain, "SignIn", "签到",
    description = "签到"
) {
    override val usage: String = "${CommandManager.commandPrefix}签到"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val oneWord = hitokoto()
        val tarot = divineTarot(user.id)
        val role = MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }

        group.sendImage(
            SignInSVG().runBeta(oneWord, tarot, 365 - LocalDateTime.now().dayOfYear, role.checkInReward()).draw()
        )
    }


    private fun hitokoto(): Pair<String, String> {
        val doc = Jsoup.connect("https://v1.hitokoto.cn/?max_length=18")
            .ignoreContentType(true)
            .execute().body().toString()
        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val hitokoto = jsonObj.string("hitokoto")
        val form = jsonObj.string("from")
        if (hitokoto.isNullOrEmpty() && form.isNullOrEmpty()) return Pair("一言-获取失败", "")
        return Pair("『$hitokoto』", "——《$form》")
    }

    private fun divineTarot(uid: Long): Map<String, String> {
        val brand = listOf(
            "The Fool(愚者)",
            "The Magician(魔术师)", "The High Priestess(女祭司)", "The Empress(女王)", "The Emperor(皇帝)", "The Hierophant(教皇)",
            "The Lovers(恋人)", "The Chariot(战车)", "Strength(力量)", "The Hermit(隐者)", "Wheel of Fortune(命运之轮)",
            "Justice(正义)", "The Hanged Man(倒吊人)", "Death(死神)", "Temperance(节制)", "The Devil(恶魔)",
            "The Tower(塔)", "The Star(星星)", "The Moon(月亮)", "The Sun(太阳)", "Judgement(审判)",
            "The World(世界)"
        ).random(Seed.getSeed(uid, Cycle.Day))

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
        return when ((0..100).random(Seed.getSeed(uid, Cycle.Day))) {
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