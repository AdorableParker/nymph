package com.navigatorTB_Nymph.command.simple

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.navigatorTB_Nymph.data.Dynamic
import com.navigatorTB_Nymph.data.DynamicInfo
import com.navigatorTB_Nymph.data.Picture
import com.navigatorTB_Nymph.pluginConfig.MySetting.DynamicNameList
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MyPluginData
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.jsoup.Jsoup

object SendDynamic : SimpleCommand(
    PluginMain, "SendDynamic", "动态查询",
    description = "B站动态查询"
) {
    override val usage: String =
        "${CommandManager.commandPrefix}动态查询 [简称|UID] <回溯条数>\n简称列表：\n${DynamicNameList.keys.joinToString("\t")}"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(name: String, index: Int = 0) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage(main(group, DynamicNameList.getOrDefault(name, name.toIntOrNull()), index))
    }

    private suspend fun main(group: Group, uid: Int?, index: Int): Message {
        if (uid == null) return PlainText(usage)
        UsageStatistics.record(primaryName)
        if (index >= 10) {
            return PlainText("最多只能往前10条哦\n(￣﹃￣)")
        } else if (index < 0) {
            return PlainText("未来的事情我怎么会知道\n=￣ω￣=")
        }
        val dynamic = Dynamic(getDynamic(uid, index))
        dynamic.layoutDynamic()
        return dynamic.draw().uploadAsImage(group, null)
    }

    fun getDynamic(uid: Int, index: Int, flag: Boolean = false): DynamicInfo {
        val doc = Jsoup.connect("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=$uid")
            .ignoreContentType(true)
            .execute().body().toString()
        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val dynamicObj = jsonObj.obj("data")
            ?.array<JsonObject>("cards")?.get(index)
        val desc = dynamicObj?.obj("desc")!!
        val card = Parser.default().parse(StringBuilder(dynamicObj.string("card"))) as JsonObject
        val typeCode = desc.int("type") ?: 0
        val timestamp = desc.long("timestamp")?.times(1000) ?: 0

        if (flag) {
            val oldTime = MyPluginData.timeStampOfDynamic[uid] ?: 0
            if (oldTime >= timestamp) return DynamicInfo(0, null, null, "")
            MyPluginData.timeStampOfDynamic[uid] = timestamp
        }

        return analysis(timestamp, typeCode, card)
    }

    private fun analysis(timestamp: Long, typeCode: Int, card: JsonObject): DynamicInfo {
        return when (typeCode) {
            // 无效数据
            0 -> DynamicInfo(
                name = card.obj("user")?.string("name"),
                face = card.obj("user")?.string("face"),
                timestamp = timestamp,
                text = "没有相关动态信息",
                pictureList = listOf()
            )
            // 转发
            1 -> {
                val origType = card.obj("item")?.int("orig_type") ?: 0
                val origin = Parser.default().parse(StringBuilder(card.string("origin"))) as JsonObject
                val originDynamic = analysis(timestamp, origType, origin)
                DynamicInfo(
                    name = card.obj("user")?.string("name"),
                    face = card.obj("user")?.string("face"),
                    timestamp = timestamp,
                    text = "转发并评论：\n${card.obj("item")?.string("content")}\n\n${originDynamic.text}",
                    pictureList = originDynamic.pictureList
                )
            }
            // 含图动态
            2 -> {
                val description = card.obj("item")?.string("description")   // 描述
                // 获取所有图片
                val pictures = card.obj("item")?.array<JsonObject>("pictures")
                DynamicInfo(
                    name = card.obj("user")?.string("name"),
                    face = card.obj("user")?.string("head_url"),
                    timestamp = timestamp,
                    text = "$description",
                    pictureList = List(pictures?.size ?: 0) { index ->
                        pictures?.get(index)?.let {
                            if (it.int("img_width") != null && it.int("img_height") != null && it.string("img_src") != null)
                                Picture(
                                    imgWidth = it.int("img_width")!!,
                                    imgHeight = it.int("img_height")!!,
                                    imgSrc = it.string("img_src")!!
                                )
                            else null
                        }
                    }
                )
            }
            // 无图动态
            4 -> DynamicInfo(
                name = card.obj("user")?.string("uname"),
                face = card.obj("user")?.string("face"),
                timestamp = timestamp,
                text = "${card.obj("item")?.string("content")}",
                pictureList = listOf()
            )
            // 视频
            8 -> {
                val dynamic = card.string("dynamic") // 描述
                DynamicInfo(
                    name = card.obj("owner")?.string("name"),
                    face = card.obj("owner")?.string("face"),
                    timestamp = timestamp,
                    text = "视频投稿:$dynamic",
                    pictureList = listOf(Picture(394, 700, card.string("pic")!!))
                )
            }
            // 专栏
            64 -> {
                val title = card.string("title")       // 标题
                val summary = card.string("summary")   // 摘要
                val imgSrc = card.array<String>("image_urls")?.get(0) // 封面图片

                DynamicInfo(
                    name = card.obj("author")?.string("name"),
                    face = card.obj("author")?.string("face"),
                    timestamp = timestamp,
                    text = "专栏标题:$title\n专栏摘要：\n$summary…",
                    pictureList = if (imgSrc != null) listOf(Picture(394, 700, card.string("pic")!!)) else listOf()
                )
            }
            // 卡片
            2048 -> {
                val title = card.obj("sketch")?.string("title")          // 标题
                val context = card.obj("vest")?.string("content")        // 内容
                val targetURL = card.obj("sketch")?.string("target_url") // 相关链接
                DynamicInfo(
                    name = card.obj("user")?.string("name"),
                    face = card.obj("user")?.string("face"),
                    timestamp = timestamp,
                    text = "动态标题:$title\n动态内容：\n$context\n相关链接:\n$targetURL",
                    pictureList = listOf()
                )
            }
            // 未知类型
            else -> {
                PluginMain.logger.warning("File:SendDynamic.kt\tLine:162\n错误信息:未知的类型码 $typeCode ")
                DynamicInfo(
                    name = card.obj("user")?.string("name"),
                    face = card.obj("user")?.string("face"),
                    timestamp = timestamp,
                    text = "是未知的动态类型,无法解析",
                    pictureList = listOf()
                )
            }
        }
    }
}