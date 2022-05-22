package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object SellItem : SimpleCommand(
    PluginMain, "Sell", "出售物品",
    description = "出售背包物品"
) {
    override val usage: String = "${CommandManager.commandPrefix}出售物品 [物品名] [出售单价] <出售数量-默认:1>"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(itemName: String, unitPrice: Int, itemDemand: Int = 1) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (PluginMain.DLC_MirrorWorld) {
            sendMessage(GameMain(this).sell(itemName, unitPrice, itemDemand))
        } else sendMessage("缺少依赖DLC")
    }
}