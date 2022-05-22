package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object GarbageCollection : SimpleCommand(
    PluginMain, "GC", "垃圾回收",
    description = "回收背包的垃圾"
) {
    override val usage: String = "${CommandManager.commandPrefix}购买物品 [物品名] <购买数量-默认:1>"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(itemName: String, itemDemand: Int = 1) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (PluginMain.DLC_MirrorWorld) {
            sendMessage(GameMain(this).garbageCollection(itemDemand))
        } else sendMessage("缺少依赖DLC")
    }
}