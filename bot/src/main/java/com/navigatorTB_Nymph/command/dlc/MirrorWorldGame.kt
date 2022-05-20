package com.navigatorTB_Nymph.command.dlc

import com.navigatorTB_Nymph.data.AssetDataAzurLaneConstructTime
import com.navigatorTB_Nymph.game.simulateCardDraw.AzleBuild
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import com.nymph_TB_DLC.MirrorWorld
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

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
        ItemSynthesisGuide.register()
        SimulateConstruction.register()
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
        ItemSynthesisGuide.unregister()
        SimulateConstruction.unregister()
    }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

            if (PluginMain.DLC_MirrorWorld) MirrorWorld(this).alchemy()
            else sendMessage("缺少依赖DLC")
        }

        @Handler
        suspend fun GroupTempCommandSenderOnMessage.main() {
            if (PluginMain.DLC_MirrorWorld) MirrorWorld(this).alchemy()
            else sendMessage("缺少依赖DLC")
        }

        @Handler
        suspend fun FriendCommandSenderOnMessage.main() {
            if (PluginMain.DLC_MirrorWorld) MirrorWorld(this).alchemy()
            else sendMessage("缺少依赖DLC")
        }
    }

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

            if (PluginMain.DLC_MirrorWorld)
                sendMessage(MirrorWorld(this).alchemyGuide())
            else sendMessage("缺少依赖DLC")
        }

        @Handler
        suspend fun GroupTempCommandSenderOnMessage.main() {
            if (PluginMain.DLC_MirrorWorld)
                sendMessage(MirrorWorld(this).alchemyGuide())
            else sendMessage("缺少依赖DLC")
        }

        @Handler
        suspend fun FriendCommandSenderOnMessage.main() {
            if (PluginMain.DLC_MirrorWorld)
                sendMessage(MirrorWorld(this).alchemyGuide())
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
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }

            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).openBag())
            } else sendMessage("缺少依赖DLC")
        }
    }

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

            if (PluginMain.DLC_MirrorWorld) {
                sendMessage(MirrorWorld(this).useItems(itemName, amount))
            } else sendMessage("缺少依赖DLC")
        }
    }

    object SimulateConstruction : SimpleCommand(
        PluginMain, "SC", "模拟建造",
        description = "碧蓝航线模拟建造"
    ) {
        override val usage = """
            ${CommandManager.commandPrefix}模拟建造 [建造池]
            建造池列表:
            限|  限定池(所有限定船同池)
            轻|  轻型池
            重|  重型池
            特|  特型池
            """.trimIndent()

        @Handler
        suspend fun MemberCommandSenderOnMessage.main(mode: String, cunt: Int) {
            if (group.botMuteRemaining > 0) return
            if (group.id !in ActiveGroupList.user) {
                sendMessage("本群授权已到期,请续费后使用")
                return
            }
            if (cunt !in 1..10) {
                sendMessage("建造次数应为 [1, 10] 区间内")
                return
            }

            val message = if (PluginMain.DLC_MirrorWorld) {
                when (mode) {
                    "限" -> {
                        val r1 = MirrorWorld(this).heavyPool(cunt)
                        if (r1.isBlank()) limit(cunt).draw().uploadAsImage(group) else PlainText(r1)
                    }
                    "轻" -> {
                        val r1 = MirrorWorld(this).lightPool(cunt)
                        if (r1.isBlank()) build(20, cunt).draw().uploadAsImage(group) else PlainText(r1)
                    }
                    "重" -> {
                        val r1 = MirrorWorld(this).heavyPool(cunt)
                        if (r1.isBlank()) build(30, cunt).draw().uploadAsImage(group) else PlainText(r1)
                    }
                    "特" -> {
                        val r1 = MirrorWorld(this).heavyPool(cunt)
                        if (r1.isBlank()) build(50, cunt).draw().uploadAsImage(group) else PlainText(r1)
                    }
                    else -> PlainText("未知模式")
                }
            } else PlainText("缺少依赖DLC")
            sendMessage(message)
        }


        private fun limit(cunt: Int): AzleBuild {
            val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
            val rL = List(cunt) {
                val sql = when ((1..1000).random()) {
                    in 1..20 -> "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 1.0;"
                    in 21..89 -> "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 0.0 AND nums % 10 == 0;"
                    in 90..206 -> "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 0.0 AND (nums - 1) % 10 == 0;"
                    in 207..706 -> "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 0.0 AND (nums - 2) % 10 == 0;"
                    else -> "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 0.0 AND (nums - 3) % 10 == 0;"
                }
                objDB.executeQuerySQL(sql, "模拟建造\nFile:MirrorWorldGame.kt\tLine:418").random().run {
                    AssetDataAzurLaneConstructTime(this)
                }
            }
            objDB.closeDB()
            return AzleBuild(cunt).drawCard(rL)
        }

        private fun build(mode: Int, cunt: Int): AzleBuild {
            val objDB = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
            val rL = List(cunt) {
                val level = when ((1..100).random()) {
                    in 1..7 -> 0
                    in 8..19 -> 1
                    in 20..45 -> 2
                    else -> 3
                }
                objDB.executeQuerySQL(
                    "SELECT * FROM AzurLane_construct_time WHERE LimitedTime = 0.0 AND (nums - $level) % $mode == 0;",
                    "模拟建造\nFile:MirrorWorldGame.kt\tLine:418"
                ).random().run { AssetDataAzurLaneConstructTime(this) }
            }
            objDB.closeDB()

            return AzleBuild(cunt).drawCard(rL)
//            return "本次结果：\n船名：${l.originalName}[${l.alias}]\t建造时间：${l.time}"
        }
    }
}