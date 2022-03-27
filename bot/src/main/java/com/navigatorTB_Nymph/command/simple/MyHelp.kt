package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

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