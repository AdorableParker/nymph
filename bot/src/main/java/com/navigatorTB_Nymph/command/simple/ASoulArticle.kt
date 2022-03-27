package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.Article
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

object ASoulArticle : SimpleCommand(
    PluginMain, "ASoulArticle", "小作文",
    description = "犯病小作文"
) {
    override val usage: String = "${CommandManager.commandPrefix}小作文 [目标]"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(name: String) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val templateScript = Article.template.random().replace("\${name}", name)
        sendMessage(templateScript)
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage("参数不匹配, 你是否想执行:\n $usage")
    }
}