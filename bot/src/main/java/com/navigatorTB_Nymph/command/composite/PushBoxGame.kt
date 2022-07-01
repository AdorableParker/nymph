package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.data.Role
import com.navigatorTB_Nymph.game.pushBox.Direction.*
import com.navigatorTB_Nymph.game.pushBox.PushBox
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser
import com.navigatorTB_Nymph.pluginData.PushBoxLevelMap
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick

object PushBoxGame : CompositeCommand(
    PluginMain, "PushBoxGame", "推箱子",
    description = "推箱子游戏实现"
) {
//    override val usage: String = ""

    @SubCommand("新游戏")
    suspend fun MemberCommandSenderOnMessage.start() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pushBox = PushBox(PushBoxLevelMap.groupCompleteLevel.getOrPut(group.id) { 0 })
        PluginMain.PUSH_BOX[group.id] = pushBox
        subject.sendImage(pushBox.draw())
    }

    @SubCommand("移动")
    suspend fun MemberCommandSenderOnMessage.move(x: Int, y: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pushBox = PluginMain.PUSH_BOX[group.id]
        if (pushBox != null) {
//            pushBox.debug()
            if (pushBox.tryTo(x, y))
                subject.sendImage(pushBox.draw())
            else
                sendMessage("坐标无效,操作失败")
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }

    @SubCommand("推")
    suspend fun MemberCommandSenderOnMessage.push(direction: String, distance: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pushBox = PluginMain.PUSH_BOX[group.id]

        if (pushBox != null) {
            val r = when (direction) {
                "上", "w" -> Up
                "下", "s" -> Down
                "左", "a" -> Left
                "右", "d" -> Right
                else -> {
                    sendMessage("无法解析的方向参数")
                    return
                }
            }.let { pushBox.tryPush(it, distance) }
            when (r) {
                null -> sendMessage("推不动的物块")
                false -> subject.sendImage(pushBox.draw())
                true -> {
                    subject.sendImage(pushBox.draw())
                    PluginMain.PUSH_BOX.remove(group.id)
                    PushBoxLevelMap.upLevel(group.id)
                    MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }.play()
                }
            }
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }

    @SubCommand("重新开始")
    suspend fun MemberCommandSenderOnMessage.restart() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pushBox = PluginMain.PUSH_BOX[group.id]

        if (pushBox != null) {
            pushBox.restart()
            subject.sendImage(pushBox.draw())
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }

}


