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
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


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
        if (group.botMuteRemaining > 0) return

        sendMessage(main(group, bot, 233114659, index))
    }

    @SubCommand("阿米娅", "方舟公告")
    suspend fun MemberCommandSenderOnMessage.arKnights(index: Int = 0) {
        if (group.botMuteRemaining > 0) return

        sendMessage(main(group, bot, 161775300, index))
    }

    @SubCommand("呆毛王", "FGO公告")
    suspend fun MemberCommandSenderOnMessage.fateGrandOrder(index: Int = 0) {
        if (group.botMuteRemaining > 0) return

        sendMessage(main(group, bot, 233108841, index))
    }

    @SubCommand("派蒙", "原神公告")
    suspend fun MemberCommandSenderOnMessage.genShin(index: Int = 0) {
        if (group.botMuteRemaining > 0) return

        sendMessage(main(group, bot, 401742377, index))
    }

    @SubCommand("UID", "其他")
    suspend fun MemberCommandSenderOnMessage.other(uid: Int, index: Int = 0) {
        if (group.botMuteRemaining > 0) return
        sendMessage(main(group, bot, uid, index))
    }

    @SubCommand("测试")
    suspend fun MemberCommandSenderOnMessage.test(uid: Int, index: Int = 0) {
        if (group.botMuteRemaining > 0) return
        group.sendImage(getDynamic(uid, index).message2jpg())
//        sendMessage(main(group, bot, uid, index))
    }

    suspend fun main(subject: Group, bot: UserOrBot, uid: Int, index: Int): Message {
        record(primaryName)
        if (index >= 10) {
            return PlainText("最多只能往前10条哦\n(￣﹃￣)")
        } else if (index < 0) {
            return PlainText("未来的事情我怎么会知道\n=￣ω￣=")
        }
        val dynamic = getDynamic(uid, index)
        val time = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()).format(dynamic.timestamp)
        return dynamic.getMessage(subject, bot, time, "")
    }

    fun getDynamic(uid: Int, index: Int, flag: Boolean = false): Dynamic {
        val doc = Jsoup.connect("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=$uid")
            .ignoreContentType(true)
            .execute().body().toString()
        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val desc = jsonObj.obj("data")
            ?.array<JsonObject>("cards")?.get(index)
            ?.obj("desc")!!

        val typeCode = desc.int("type") ?: 0
        val timestamp = desc.long("timestamp")?.times(1000) ?: 0

        if (flag) {
            val oldTime = MyPluginData.timeStampOfDynamic[uid] ?: 0
            if (oldTime >= timestamp) return Dynamic("", 0)
            MyPluginData.timeStampOfDynamic[uid] = timestamp
        }

        return analysis(timestamp, typeCode, desc)
    }

    private fun analysis(timestamp: Long, typeCode: Int, card: JsonObject): Dynamic {
        val d = Dynamic(
            name = card.obj("user")?.string("name") ?: "动态更新",
            timestamp = timestamp
        )

        when (typeCode) {
            // 无效数据
            0 -> d.text = "没有相关动态信息"
            // 转发
            1 -> {
                val origType = card.obj("item")?.int("orig_type") ?: 0
                val origin = Parser.default().parse(StringBuilder(card.string("origin"))) as JsonObject
                val originDynamic = analysis(timestamp, origType, origin)
                d.text = "转发并评论：\n${card.obj("item")?.string("content")}\n转发源：\n${originDynamic.text}"
                d.imageStream = originDynamic.imageStream
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
                d.text = if (description.isNullOrEmpty()) "含图动态:" else "含图动态:\n$description"
                d.imageStream = imgSrcList
            }
            // 无图动态
            4 -> d.text = "无图动态：\n${card.obj("item")?.string("content")}"
            // 视频
            8 -> {
                val dynamic = card.string("dynamic") // 描述
                val imgSrc = card.string("pic")      //封面图片
                d.text = if (dynamic.isNullOrEmpty()) "视频动态:" else "视频动态:\n$dynamic"
                d.imageStream = listOf(URL(imgSrc).openConnection().getInputStream())
            }
            // 专栏
            64 -> {
                val title = card.string("title")       // 标题
                val summary = card.string("summary")   // 摘要
                d.text = "专栏标题:$title\n专栏摘要：\n$summary…"
                val imgSrc = card.array<String>("image_urls")?.get(0) // 封面图片
                if (imgSrc != null) d.imageStream = listOf(URL(imgSrc).openConnection().getInputStream())
            }
            // 卡片
            2048 -> {
                val title = card.obj("sketch")?.string("title")          // 标题
                val context = card.obj("vest")?.string("content")        // 内容
                val targetURL = card.obj("sketch")?.string("target_url") // 相关链接
                d.text = "动态标题:$title\n动态内容：\n$context\n相关链接:\n$targetURL"
            }
            // 未知类型
            else -> {
                PluginMain.logger.warning("File:SendDynamic.kt\tLine:162\n错误信息:未知的类型码 $typeCode ")
                d.text = "是未知的动态类型,无法解析"
            }
        }
        return d
    }
}