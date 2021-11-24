/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
@ConsoleExperimentalApi
object MyHelp : SimpleCommand(
    PluginMain, "Menu", "帮助", "菜单",
    description = "帮助命令"
) {
    override val usage: String = "${CommandManager.commandPrefix}Menu"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        val helpDocs = mutableListOf<String>()
        CommandManager.allRegisteredCommands.forEach {
            if (it.owner == PluginMain) {
                helpDocs.add("主命令名:${it.primaryName}\t别名：${it.secondaryNames.joinToString(",")}\n说明:${it.description}")
            }
        }
        sendMessage(helpDocs.joinToString("\n"))
//        val commandList = PluginMain.allNames
//        sendMessage("${}")
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(name: String) {
        CommandManager.allRegisteredCommands.forEach {
            if (it.owner == PluginMain && (name == it.primaryName || name in it.secondaryNames)) {
                sendMessage("主命令名:${it.primaryName}\t别名：${it.secondaryNames.joinToString(",")}\n详细说明:${it.usage}")
            }
        }
//        val commandList = PluginMain.allNames
//        sendMessage("${}")
    }
}