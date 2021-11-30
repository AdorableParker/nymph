package com.example.navigatorTB_Nymph

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.warning
import org.jsoup.Jsoup
import java.net.URL


object TraceMoe : SimpleCommand(
    PluginMain, "TraceMoe", "搜番",
    description = "以图搜番"

) {
    override val usage: String = "${CommandManager.commandPrefix}搜番 [图片]"


    @Handler
    suspend fun MemberCommandSenderOnMessage.main(image: Image) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return

        sendMessage("开始查询，请稍后...")
        val jsonObjString = getJSON(image.queryUrl())
        if (jsonObjString == null) {
            sendMessage("远端服务器超时或无响应,请稍后再试")
            return
        }
        val jsonObject = Parser.default().parse(StringBuilder(jsonObjString)) as JsonObject
        val error = jsonObject.string("error")
        if (error.isNullOrBlank()) {
            sendMessage(writeReport(jsonObject, group))
            return
        }
        sendMessage("$error")
    }


    private fun getJSON(img: String): String? {
        val url = "https://api.trace.moe/search?anilistInfo&cutBorders&url=$img"
        runCatching {
            Jsoup.connect(url)
                .ignoreContentType(true)
                .execute()
                .body()
                .toString()
        }.onSuccess {
            return it
        }.onFailure {
            PluginMain.logger.warning { "File:TraceMoe.kt\tLine:60\n$it" }
            return null
        }
        return null
    }

    private suspend fun writeReport(jsonObject: JsonObject, group: Group): Message {
        val result = jsonObject.array<JsonObject>("result")?.get(0)
        if (result != null) {
            val title = result.obj("anilist")?.obj("title")
            val native = title?.string("native")
            val romaji = title?.string("romaji")
            val english = title?.string("english")

            val episode = result["episode"]
            val from = result.int("from")
            val to = result.int("to")
            val similarity = result.float("similarity")

            val image = result.string("image")?.getImgMessage()
//            uploadImage(it) }

            val r = if (episode == null) {
                val filename = result.string("filename")
                PlainText(
                    "原名:$native\n罗马音:$romaji\n英文名:$english\n匹配结果位于\n$filename\n${from?.div(60)}分${from?.rem(60)}秒至${
                        to?.div(60)
                    }分${to?.rem(60)}秒\n结果相似度：${similarity}"
                )
            } else {
                PlainText(
                    "原名:$native\n罗马音:$romaji\n英文名:$english\n匹配结果位于第${episode}集${from?.div(60)}分${from?.rem(60)}秒至${
                        to?.div(60)
                    }分${to?.rem(60)}秒\n结果相似度：${similarity}"
                )
            }
            return if (image != null) {
                buildMessageChain {
                    +r
                    +PlainText("\n结果样图：")
                    +image.uploadAsImage(group)
                }
            } else r
        }
        return PlainText("结果解析异常")
    }

    private fun String.getImgMessage() = URL(this).openConnection().getInputStream()
}

