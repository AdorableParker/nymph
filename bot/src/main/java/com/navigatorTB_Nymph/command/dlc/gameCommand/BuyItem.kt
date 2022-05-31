package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object BuyItem : SimpleCommand(
    PluginMain, "Buy", "购买物品",
    description = "购买商店物品"
) {
    override val usage: String = "${CommandManager.commandPrefix}购买物品 [物品名] <购买数量-默认:1>"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(itemName: String, itemDemand: Int = 1) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        sendMessage(GameMain(this).buy(itemName, itemDemand))
    }
}