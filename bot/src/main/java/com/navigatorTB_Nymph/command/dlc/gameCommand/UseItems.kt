package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object UseItems : SimpleCommand(
    PluginMain, "UseItems", "使用",
    description = "使用物品"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(itemName: String, amount: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }


        sendMessage(GameMain(this).useItems(itemName, amount))

    }
}