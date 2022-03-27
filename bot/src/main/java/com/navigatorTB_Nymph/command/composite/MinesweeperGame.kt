package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.game.minesweeper.GameState
import com.navigatorTB_Nymph.game.minesweeper.LevelSet
import com.navigatorTB_Nymph.game.minesweeper.Minesweeper
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Contact.Companion.sendImage

object MinesweeperGame : CompositeCommand(
    PluginMain, "MinesweeperGame", "扫雷",
    description = "扫雷游戏实现"
) {
//    override val usage: String = ""

    @SubCommand("开始", "新游戏")
    suspend fun MemberCommandSenderOnMessage.start(level: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val minesweeperGame = when (level) {
            "中级" -> Minesweeper(LevelSet.General)
            "高级" -> Minesweeper(LevelSet.Difficulty)
            "初级" -> Minesweeper(LevelSet.Easy)
            else -> {
                sendMessage("异常参数,难度设定为初级")
                Minesweeper(LevelSet.Easy)
            }
        }
        PluginMain.MINESWEEPER_GAME[group.id] = minesweeperGame
        subject.sendImage(minesweeperGame.draw())
    }

    @SubCommand("开始", "新游戏")
    suspend fun MemberCommandSenderOnMessage.start(width: Int, height: Int, mines: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (width >= 40 || height >= 60 || mines >= width * height) {
            sendMessage("创建自定义游戏失败,请检查：\n1、棋盘宽不能大于40;\n2、高不能大于60;\n3、雷数不应超出地块总数;")
        } else {
            val minesweeperGame = Minesweeper(width, height, mines)
            PluginMain.MINESWEEPER_GAME[group.id] = minesweeperGame
            subject.sendImage(minesweeperGame.draw())
        }
    }

    @SubCommand("挖开")
    suspend fun MemberCommandSenderOnMessage.dig(x: Int, y: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val minesweeperGame = PluginMain.MINESWEEPER_GAME[group.id]
        if (minesweeperGame != null) {
            when (minesweeperGame.dig(x, y)) {
                GameState.Effective -> sendMessage("坐标无效,操作失败")
                GameState.Invalid -> group.sendImage(minesweeperGame.draw())
                GameState.GameOver -> {
                    group.sendImage(minesweeperGame.draw())
                    PluginMain.MINESWEEPER_GAME.remove(group.id)
                }
            }

        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }

    }

    @SubCommand("插旗")
    suspend fun MemberCommandSenderOnMessage.flag(x: Int, y: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val minesweeperGame = PluginMain.MINESWEEPER_GAME[group.id]
        if (minesweeperGame != null) {
            minesweeperGame.put(x, y)
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }

    @SubCommand("拔旗")
    suspend fun MemberCommandSenderOnMessage.unplug(x: Int, y: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val minesweeperGame = PluginMain.MINESWEEPER_GAME[group.id]
        if (minesweeperGame != null) {
            minesweeperGame.unplug(x, y)
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }
}