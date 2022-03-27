package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.command.simple.OneWord.hitokoto
import com.navigatorTB_Nymph.command.simple.Tarot.divineTarot
import com.navigatorTB_Nymph.game.signIn.SignInSVG
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
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
        val oneWord = hitokoto(18)
        val tarot = divineTarot(user.id)

        if (PluginMain.DLC_MirrorWorld) {
            group.sendImage(
                SignInSVG().runBeta(
                    oneWord, tarot, 365 - LocalDateTime.now().dayOfYear,
                    MirrorWorld(this).pay((1..20).random())
                ).draw()
            )
        } else {
            group.sendImage(SignInSVG().runBeta(oneWord, tarot, 365 - LocalDateTime.now().dayOfYear).draw())
        }
    }
}