package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object PvPBattle : SimpleCommand(
    PluginMain, "PvP", "玩家对战",
    description = "玩家对战"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(user: User) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (PluginMain.DLC_MirrorWorld) {
            GameMain(this).pvp(user)
        } else sendMessage("缺少依赖DLC")
    }
}