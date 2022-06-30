package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.Role
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.nameCardOrNick

object PlayerInfo : SimpleCommand(
    PluginMain, "PlayerInfo", "我的信息",
    description = "用户信息"
) {

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        sendMessage(MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }.info())
    }
}