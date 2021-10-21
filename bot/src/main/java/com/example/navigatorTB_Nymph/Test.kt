/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info

@MiraiExperimentalApi
@ConsoleExperimentalApi
object Test : SimpleCommand(
    PluginMain, "Test", "测试",
    description = "功能测试命令"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return

        if (isUser() && user.id == MySetting.AdminID) {
            sendMessage("Hi,Administration")
            val list = mutableSetOf<Long>()
            bot.groups.forEach { list.add(it.id) }
            PluginMain.logger.info { list.joinToString(",") }
        } else {
            sendMessage("权限不足")
        }
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(groupID: Long) {
        if (group.botMuteRemaining > 0) return

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

