package com.example.navigatorTB_Nymph

import com.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object MirrorWorldGame {

    fun register() {
        PlayerInfo.register()
        PlayerBuild.register()
        PvPBattle.register()
        PlayerTransfer.register()
        GMtoBestow.register()
        GMStrip.register()
        Inn.register()
        PvEBattle.register()
        Shopping.register()
        BuyItem.register()
        SellItem.register()
        ItemSynthesis.register()
        OpenBag.register()
        UseItems.register()
    }

    fun unregister() {
        PlayerInfo.unregister()
        PlayerBuild.unregister()
        PvPBattle.unregister()
        PlayerTransfer.unregister()
        GMtoBestow.unregister()
        GMStrip.unregister()
        Inn.unregister()
        PvEBattle.unregister()
        Shopping.unregister()
        BuyItem.unregister()
        SellItem.unregister()
        ItemSynthesis.unregister()
        OpenBag.unregister()
        UseItems.unregister()
    }

    object PlayerInfo : SimpleCommand(
        PluginMain, "PlayerInfo", "我的信息",
        description = "用户信息"
    ) {

        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).gamerInfo())
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
                MirrorWorld(this).characterCreation()
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
                MirrorWorld(this).pvp(user)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PlayerTransfer : SimpleCommand(
        PluginMain, "TransferP", "金币转账",
        description = "玩家金币转移"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(user: User, amount: Int) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld(this).transfer(user.id, amount)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object GMtoBestow : SimpleCommand(
        PluginMain, "toBestow", "金币有",
        description = "GM-金币增加"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(foe: User, amount: Int) {
            if (group.botMuteRemaining > 0 || user.id != MySetting.AdminID) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld(this).toBestow(foe.id, amount)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object GMStrip : SimpleCommand(
        PluginMain, "GMStrip", "金币没",
        description = "GM-金币减少"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(foe: User, amount: Int) {
            if (group.botMuteRemaining > 0 || user.id != MySetting.AdminID) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld(this).strip(foe.id, amount)
            } else sendMessage("缺少依赖DLC")
        }
    }

    object Inn : SimpleCommand(
        PluginMain, "Inn", "旅店休息",
        description = "恢复所有生命值和法力值"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                MirrorWorld(this).treatment()?.let { it ->
                    sendMessage(it)
                } ?: sendMessage("请先建立角色")
            } else sendMessage("缺少依赖DLC")
        }
    }

    object PvEBattle : SimpleCommand(
        PluginMain, "PvE", "进入副本",
        description = "副本对战"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) MirrorWorld(this).pve()
            else sendMessage("缺少依赖DLC")
        }
    }

    object Shopping : SimpleCommand(
        PluginMain, "Shop", "进入商店",
        description = "查看商店物品"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).enterStore())
            } else sendMessage("缺少依赖DLC")
        }
    }

    object BuyItem : SimpleCommand(
        PluginMain, "Buy", "购买物品",
        description = "购买商店物品"
    ) {
        override val usage: String = "${CommandManager.commandPrefix}购买物品 [物品名] <购买数量-默认:1>"

        @Handler
        suspend fun MemberCommandSenderOnMessage.main(itemName: String, itemDemand: Int = 1) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).buy(itemName, itemDemand))
            } else sendMessage("缺少依赖DLC")
        }
    }

    object SellItem : SimpleCommand(
        PluginMain, "Sell", "出售物品",
        description = "出售背包物品"
    ) {
        override val usage: String = "${CommandManager.commandPrefix}出售物品 [物品名] [出售单价] <出售数量-默认:1>"

        @Handler
        suspend fun MemberCommandSenderOnMessage.main(itemName: String, unitPrice: Int, itemDemand: Int = 1) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).sell(itemName, unitPrice, itemDemand))
            } else sendMessage("缺少依赖DLC")
        }
    }

    object ItemSynthesis : SimpleCommand(
        PluginMain, "Alchemy", "合成",
        description = "合成物品"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) MirrorWorld(this).alchemy()
            else sendMessage("缺少依赖DLC")
        }
    }

    object OpenBag : SimpleCommand(
        PluginMain, "OpenBag", "背包",
        description = "打开背包"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main() {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).openBag())
            } else sendMessage("缺少依赖DLC")
        }
    }

    object UseItems : SimpleCommand(
        PluginMain, "UseItems", "使用",
        description = "打开背包"
    ) {
        @Handler
        suspend fun MemberCommandSenderOnMessage.main(itemName: String) {
            if (group.botMuteRemaining > 0) return
            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).useItems(itemName))
            } else sendMessage("缺少依赖DLC")
        }
    }

}