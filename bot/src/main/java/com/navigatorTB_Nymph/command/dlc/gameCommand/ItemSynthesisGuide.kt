package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.FriendCommandSenderOnMessage
import net.mamoe.mirai.console.command.GroupTempCommandSenderOnMessage
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object ItemSynthesisGuide : SimpleCommand(
    PluginMain, "AlchemyGuide", "合成指南",
    description = "合成物品指南"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(GameMain(this).alchemyGuide())

    }

    @Handler
    suspend fun GroupTempCommandSenderOnMessage.main() {
        sendMessage(GameMain(this).alchemyGuide())
    }

    @Handler
    suspend fun FriendCommandSenderOnMessage.main() {
        sendMessage(GameMain(this).alchemyGuide())
    }
}