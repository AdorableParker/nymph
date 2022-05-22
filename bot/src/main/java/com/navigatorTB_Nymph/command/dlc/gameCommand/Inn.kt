package com.navigatorTB_Nymph.command.dlc.gameCommand

import com.navigatorTB_Nymph.command.dlc.mirrorWorld.GameMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object Inn : SimpleCommand(
    PluginMain, "Inn", "旅店休息",
    description = "恢复所有生命值和法力值"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (PluginMain.DLC_MirrorWorld) {
            GameMain(this).treatment()?.let { it ->
                sendMessage(it)
            } ?: sendMessage("请先建立角色")
        } else sendMessage("缺少依赖DLC")
    }
}