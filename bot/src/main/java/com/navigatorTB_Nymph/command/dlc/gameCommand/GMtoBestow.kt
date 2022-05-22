package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object GMtoBestow : SimpleCommand(
    PluginMain, "toBestow", "金币有",
    description = "GM-金币增加"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(foe: User, amount: Int) {
        if (group.botMuteRemaining > 0 || user.id != MySetting.AdminID) return
        if (PluginMain.DLC_MirrorWorld) {
            GameMain(this).toBestow(foe.id, amount)
        } else sendMessage("缺少依赖DLC")
    }
}