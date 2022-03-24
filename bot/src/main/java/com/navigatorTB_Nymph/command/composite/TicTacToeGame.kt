package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.game.ticTacToe.TicTacToe
import com.navigatorTB_Nymph.main.PluginMain
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Contact.Companion.sendImage

object TicTacToeGame : CompositeCommand(
    PluginMain, "TicTacToe", "井字棋",
    description = "井字棋游戏实现"
) {
    @SubCommand("开始", "新游戏")
    suspend fun MemberCommandSenderOnMessage.start(level: Boolean = true) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val ticTacToe = TicTacToe(level)
        if ((1..100).random() <= 50)
            sendMessage("你先手，运气不错")
        else {
            sendMessage("我先手，祝你好运")
            ticTacToe.aiRun()
        }
        PluginMain.TicTacToe_GAME[group.id] = ticTacToe
        group.sendImage(ticTacToe.getImage())
    }

    @SubCommand("下")
    suspend fun MemberCommandSenderOnMessage.down(index: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val ticTacToe = PluginMain.TicTacToe_GAME[group.id]
        if (ticTacToe != null) {
            when (ticTacToe.down(index)) {
                -1 -> {
                    group.sendImage(ticTacToe.getImage())
                    PluginMain.TicTacToe_GAME.remove(group.id)
                    sendMessage("本局游戏结束")
                    return
                }
                5 -> sendMessage("只能在空白位置落子")
            }
            if (ticTacToe.aiRun() == -1) {
                group.sendImage(ticTacToe.getImage())
                PluginMain.TicTacToe_GAME.remove(group.id)
                sendMessage("本局游戏结束")
                return
            }
            group.sendImage(ticTacToe.getImage())
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }
}