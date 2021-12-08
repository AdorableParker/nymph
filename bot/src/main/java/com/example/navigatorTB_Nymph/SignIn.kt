package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.OneWord.hitokoto
import com.example.navigatorTB_Nymph.Tarot.divineTarot
import com.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import javax.imageio.ImageIO


object SignIn : SimpleCommand(
    PluginMain, "SignIn", "签到",
    description = "签到"
) {
    override val usage: String = "${CommandManager.commandPrefix}签到"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (PluginMain.DLC_MirrorWorld) {
            val s = MirrorWorld().pay(user.id, (1..20).random())
            group.sendImage(sing(user.id, s))
        } else {
            group.sendImage(sing(user.id))
        }
    }

    private fun sing(uid: Long, s: String? = null): ByteArrayInputStream {
        val oneWord = hitokoto()
        val tarot = divineTarot(uid)

        val themeColor = when (tarot["Brand"]) {
            "The Hanged Man(倒吊人)" -> Color.decode("#69b960")
            "The Emperor(皇帝)", "Justice(正义)", "The Chariot(战车)", "The Magician(魔术师)" -> Color.decode("#ff9e3e")
            "The Hierophant(教皇)", "Death(死神)", "The Sun(太阳)" -> Color.decode("#c4bcb9")
            "Strength(力量)", "The Empress(女王)" -> Color.decode("#e7c653")
            "The Devil(恶魔)", "The Tower(塔)" -> Color.decode("#553246")
            "The Star(星星)" -> Color.decode("#4c557e")
            "The High Priestess(女祭司)", "Judgement(审判)" -> Color.decode("#4c92c6")
            "The Lovers(恋人)", "Wheel of Fortune(命运之轮)", "The World(世界)" -> Color.decode("#4ea4c5")
            "Temperance(节制)" -> Color.decode("#c2b69e")
            "The Hermit(隐者)" -> Color.decode("#2b5c56")
            "The Fool(愚者)" -> Color.decode("#114e61")
            "The Moon(月亮)" -> Color.decode("#8a677d")
            else -> Color.WHITE
        }

        val image = BufferedImage(720, 500, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        // 绘制背景色
        graphics.color = themeColor
        graphics.fillRect(0, 0, 720, 500)


        // 绘制塔罗牌
        tarot["ImgPath"]?.let { path ->
            val i = ImageIO.read(File(PluginMain.resolveDataPath(path).toString()))
            graphics.drawImage(i, 465, 15, 255, 470, null)
        }

        // 绘制透明白色遮罩
        graphics.color = Color(255, 255, 255, 192)
        graphics.fillRect(0, 15, 720, 470)

        // 开始绘制文字
        graphics.color = themeColor
        graphics.font = Font("大签字笔体", Font.PLAIN, 45)
        graphics.drawString("${tarot["Brand"]}", 5, 60)

        graphics.font = Font("汉仪阿尔茨海默病体", Font.BOLD, 25)
        for ((i, str) in tarot["word"]!!.split("、").withIndex()) {
            graphics.drawString(str, 20, 95 + i * 25)
        }



        graphics.font = Font("华康翩翩体W5-A", Font.PLAIN, 23)
        val fontMetrics1 = graphics.getFontMetrics(Font("华康翩翩体W5-A", Font.PLAIN, 23)) // 创建一个FontMetrics对象
        val l = oneWord.first.chunked(18)
        var i = l.size
        for (word in l) {
            graphics.drawString(word, 465 - fontMetrics1.stringWidth(word), 420 - i * 20)
            i--
        }
        graphics.drawString(oneWord.second, 465 - fontMetrics1.stringWidth(oneWord.second), 440)

        graphics.font = Font("汉仪铸字卡酷体W", Font.PLAIN, 60)
        graphics.drawString("今年还剩", 470, 150)
        graphics.drawString("${365 - LocalDateTime.now().dayOfYear}", 570, 220)
        graphics.drawString("天", 650, 290)

        if (s != null) {
            graphics.color = Color(0, 0, 0, 46)
            graphics.font = Font("方正剪纸简体", Font.PLAIN, 26)
            graphics.drawString(s, 470, 320)
            graphics.color = Color(255, 255, 255, 200)
            graphics.font = Font("方正剪纸简体", Font.PLAIN, 24)
            graphics.drawString(s, 470, 320)
        }

        graphics.color = Color.decode("#9a9a9a")
        graphics.font = Font("Zpix", Font.PLAIN, 15)
        graphics.drawString("一言:https://hitokoto.cn", 5, 480)
        graphics.dispose()

        // 输出图片
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}