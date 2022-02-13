/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.utils.info


object Test : CompositeCommand(
    PluginMain, "Test", "测试",
    description = "功能测试命令"
) {
    @SubCommand("群列表")
    suspend fun MemberCommandSenderOnMessage.gl() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (user.id == MySetting.AdminID) {
            sendMessage("OK")
            val list = mutableSetOf<Long>()
            bot.groups.forEach { list.add(it.id) }
            PluginMain.logger.info { list.joinToString(",") }
        } else sendMessage("权限不足")
    }

    @SubCommand("状态测试")
    suspend fun MemberCommandSenderOnMessage.gst(groupID: Long) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val group = bot.getGroup(groupID)
        if (group != null) {
            runCatching {
                group.sendMessage("状态测试")
            }.onFailure {
                sendMessage(it.message.orEmpty())
            }
        } else {
            sendMessage("获取群对象失败")
        }
    }
}

