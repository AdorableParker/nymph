package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.TB
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage


object CultivationSystem : CompositeCommand(
    PluginMain, MySetting.name, MySetting.nickname,
    description = "养成系统"
) {

    @SubCommand("心情")
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage(TB.getFeeling().value)
    }

//    @SubCommand("")
//    suspend fun MemberCommandSenderOnMessage.main() {
//        //        val role = MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }
//        sendMessage(TB.getFeeling().value)
//    }
}