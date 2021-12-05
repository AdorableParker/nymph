package com.example.navigatorTB_Nymph

import com.example.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object MirrorWorldGame {

    fun register() {
        PlayerInfo.register()
        PlayerBuild.register()
        APAllot.register()
        CCareer.register()
        PvPBattle.register()
    }

    fun unregister() {
        PlayerInfo.unregister()
        PlayerBuild.unregister()
        APAllot.unregister()
        CCareer.unregister()
        PvPBattle.unregister()
    }

    object PlayerInfo : SimpleCommand(
        PluginMain, "PlayerInfo", "我的信息",
        description = "用户信息"
    ) {

        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().gamerInfo(this)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PlayerBuild : SimpleCommand(
        PluginMain, "PlayerBuild", "建立角色",
        description = "玩家角色建立"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().characterCreation(this)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object APAllot : SimpleCommand(
        PluginMain, "APAllot", "属性点分配",
        description = "分配属性点"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().apAllotted(this)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object CCareer : SimpleCommand(
        PluginMain, "Career", "选择职业",
        description = "角色职业选择"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().chooseCareer(this)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PvPBattle : SimpleCommand(
        PluginMain, "PvP", "玩家对战",
        description = "玩家对战"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(user: User) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld().pvp(this, user)
            } else sendMessage("缺少依赖DLC")
        }
    }
}