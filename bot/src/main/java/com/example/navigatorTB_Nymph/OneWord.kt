package com.example.navigatorTB_Nymph

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import org.jsoup.Jsoup


object OneWord : SimpleCommand(
    PluginMain, "OneWord", "一言",
    description = "一言"
) {
    override val usage: String = "${CommandManager.commandPrefix}一言"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val pair = hitokoto()
        sendMessage("${pair.first}\n${pair.second}\t一言: hitokoto.cn")
    }

    fun hitokoto(): Pair<String, String> {
        val doc = Jsoup.connect("https://v1.hitokoto.cn/")
            .ignoreContentType(true)
            .execute().body().toString()
        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val hitokoto = jsonObj.string("hitokoto")
        val form = jsonObj.string("from")
        if (hitokoto.isNullOrEmpty() && form.isNullOrEmpty()) return Pair("一言-获取失败", "")
        return Pair("『$hitokoto』", "——《$form》")
    }
}