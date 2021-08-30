/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@MiraiExperimentalApi
@ConsoleExperimentalApi
object SendDynamic : CompositeCommand(
    PluginMain, "SendDynamic", "动态查询",
    description = "B站动态查询"
) {
    override val usage: String =
        "${CommandManager.commandPrefix}动态查询 [目标ID] <回溯条数>\n" +
                "目标ID列表：\n" +
                "*1* 小加加,碧蓝公告\n" +
                "*2* 阿米娅,方舟公告\n" +
                "*3* 呆毛王,FGO公告\n" +
                "*4* 派蒙,原神公告\n" +
                "*5* UID,其他"

    @SubCommand("小加加", "碧蓝公告")
    suspend fun MemberCommandSenderOnMessage.azurLane(index: Int = 0) {
        sendMessage(main(group, 233114659, index))
    }

    @SubCommand("阿米娅", "方舟公告")
    suspend fun MemberCommandSenderOnMessage.arKnights(index: Int = 0) {
        sendMessage(main(group, 161775300, index))
    }

    @SubCommand("呆毛王", "FGO公告")
    suspend fun MemberCommandSenderOnMessage.fateGrandOrder(index: Int = 0) {
        sendMessage(main(group, 233108841, index))
    }

    @SubCommand("派蒙", "原神公告")
    suspend fun MemberCommandSenderOnMessage.genShin(index: Int = 0) {
        sendMessage(main(group, 401742377, index))
    }

    @SubCommand("UID", "其他")
    suspend fun MemberCommandSenderOnMessage.other(uid: Int, index: Int = 0) {
        sendMessage(main(group, uid, index))
    }

    suspend fun main(subject: Group, uid: Int, index: Int): Message {
        record(primaryName)
        if (index >= 10) {
            return PlainText("最多只能往前10条哦\n(￣﹃￣)")
        } else if (index < 0) {
            return PlainText("未来的事情我怎么会知道\n=￣ω￣=")
        }
        val dynamic = getDynamic(uid, index)
        val time = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()).format(dynamic.timestamp)
        return dynamic.getMessage(subject) + PlainText("\n发布时间:$time")
    }

    fun getDynamic(uid: Int, index: Int, flag: Boolean = false): Dynamic {
        val doc = Jsoup.connect("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=$uid")
            .ignoreContentType(true)
            .execute().body().toString()
        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val desc = jsonObj.obj("data")
            ?.array<JsonObject>("cards")?.get(index)
            ?.obj("desc")
        val timestamp = desc?.long("timestamp")?.times(1000)
        if (flag) {
            timestamp?.let {
                val oldTime = MyPluginData.timeStampOfDynamic[uid] ?: 0
                if (oldTime >= it) return Dynamic(null, null, null)
                MyPluginData.timeStampOfDynamic[uid] = it
            }
        }
        val typeCode = desc?.int("type")
        val cardStr = jsonObj.obj("data")
            ?.array<JsonObject>("cards")?.get(index)
            ?.string("card")
        val card = Parser.default().parse(StringBuilder(cardStr)) as JsonObject
        return analysis(timestamp, typeCode, card)
    }

    private fun analysis(timestamp: Long?, typeCode: Int?, card: JsonObject): Dynamic {

        when (typeCode) {
            // 无效数据
            0 -> return Dynamic(timestamp, "没有相关动态信息", null)
            // 转发
            1 -> {
                val origType = card.obj("item")?.int("orig_type")
                val origin = Parser.default().parse(StringBuilder(card.string("origin"))) as JsonObject
                val originDynamic = analysis(timestamp, origType, origin)
                return Dynamic(
                    timestamp,
                    "转发并评论：${card.obj("item")?.string("content")}\n转发源：${originDynamic.text}",
                    originDynamic.imageStream
                )
            }
            // 含图动态
            2 -> {
                val description = card.obj("item")?.string("description")   // 描述
                // 获取所有图片
                val imgSrcList = mutableListOf<InputStream>()
                card.obj("item")?.array<JsonObject>("pictures")?.forEach { it ->
                    it.string("img_src")
                        ?.let { imgSrc -> imgSrcList.add(URL(imgSrc).openConnection().getInputStream()) }
                }
                return Dynamic(timestamp, "含图动态:$description", imgSrcList)
                // 返回首张图片地址
//                val imgSrc = card.obj("item")?.array<JsonObject>("pictures")?.get(0)?.string("img_src")
//                return if (imgSrc.isNullOrBlank()) Dynamic(timestamp, description, null)
//                else Dynamic(timestamp, description, imgSrcList)
            }
            // 无图动态
            4 -> return Dynamic(timestamp, "无图动态：${card.obj("item")?.string("content")}", null)
            // 视频
            8 -> {
                val dynamic = card.string("dynamic") // 描述
                val imgSrc = card.string("pic")      //封面图片
                return Dynamic(timestamp, "视频动态：$dynamic", listOf(URL(imgSrc).openConnection().getInputStream()))
            }
            // 专栏
            64 -> {
                val title = card.string("title")       // 标题
                val summary = card.string("summary")   // 摘要
                val imgSrc = card.array<String>("image_urls")?.get(0) // 封面图片
                return if (imgSrc.isNullOrBlank()) Dynamic(timestamp, "专栏标题:$title\n专栏摘要：\n$summary…", null)
                else Dynamic(
                    timestamp,
                    "专栏标题:$title\n专栏摘要：\n$summary…",
                    listOf(URL(imgSrc).openConnection().getInputStream())
                )
            }
            // 卡片
            2048 -> {
                val title = card.obj("sketch")?.string("title")          // 标题
                val context = card.obj("vest")?.string("content")        // 内容
                val targetURL = card.obj("sketch")?.string("target_url") // 相关链接
                return Dynamic(timestamp, "动态标题:$title\n动态内容：\n$context\n相关链接:\n$targetURL", null)
            }
            // 未知类型
            else -> {
                PluginMain.logger.warning("File:SendDynamic.kt\tLine:144\n错误信息:未知的类型码 $typeCode ")
                return Dynamic(timestamp, "是未知的动态类型,无法解析", null)
            }
        }
    }
}