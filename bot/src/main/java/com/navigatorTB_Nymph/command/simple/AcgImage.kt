package com.navigatorTB_Nymph.command.simple

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.navigatorTB_Nymph.data.UserACGImg
import com.navigatorTB_Nymph.data.UserPolicy
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MyPluginData
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.warning
import org.jsoup.Jsoup
import java.net.URL
import java.time.Instant

object AcgImage : SimpleCommand(
    PluginMain, "acgImage", "随机图片",
    description = "随机发送acg图片"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(tag: String? = null) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (MyPluginData.AcgImageRun.contains(group.id)) {
            sendMessage("功能运行中，请等待")
            return
        }

        MyPluginData.AcgImageRun.add(group.id)
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val status = UserPolicy(
            dbObject.selectOne(
                "Policy",
                Triple("groupID", "=", "${group.id}"),
                "随机图片\nFile:AcgImage.kt\tLine:46"
            )
        )
        if (status.acgImgAllowed != 1) {
            dbObject.closeDB()
            sendMessage("本群尚未启用该功能，请联系群主或管理员开启")
            MyPluginData.AcgImageRun.remove(group.id)
            return
        }

        val quota = UserACGImg(
            dbObject.selectOne(
                "ACGImg",
                Triple("groupID", "=", "${group.id}"),
                "随机图片\nFile:AcgImage.kt\tLine:58"
            )
        )
        val timeLeft = 60 - Instant.now().epochSecond + quota.date
        if (quota.score <= 0) {
            dbObject.closeDB()
            sendMessage("⚠系统错误⚠\n本群的配额已用尽")
            MyPluginData.AcgImageRun.remove(group.id)
            return
        }
        if (timeLeft >= 0) {
            dbObject.closeDB()
            sendMessage("⚠WARNING⚠\n功能整备中，整备剩余时间：${timeLeft}秒")
            MyPluginData.AcgImageRun.remove(group.id)
            return
        }
        runCatching {
            sendMessage(getRandomImg(group, tag))
            dbObject.update(
                "ACGImg",
                Pair("groupID", "${group.id}"),
                Pair(arrayOf("score", "date"), arrayOf("${quota.score - 1}", "${Instant.now().epochSecond}")),
                "随机图片\nFile:AcgImage.kt\tLine:81"
            )
            if (quota.score <= 10) sendMessage("ℹ本群剩余配给已经不足10点了")
        }.onFailure {
            PluginMain.logger.warning { "File:AcgImage.kt\tLine:89\n${it.message}" }
            sendMessage("数据传输失败...嗯.一定是塞壬的问题..")
        }
        dbObject.closeDB()
        MyPluginData.AcgImageRun.remove(group.id)
    }

    private suspend fun getRandomImg(group: Group, tag: String?): Message {
        val doc =
            Jsoup.connect("https://api.lolicon.app/setu/v2?r18=0&size=regular&proxy=${MySetting.proxy}${if (tag != null) "&tag=$tag" else ""}")
                .ignoreContentType(true)
                .execute().body().toString()

        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
        val errorInfo = jsonObj.string("error")
        if (!errorInfo.isNullOrEmpty()) return PlainText(errorInfo)
        val data = jsonObj.array<JsonObject>("data")?.run {
            if (isNotEmpty()) get(0) else null
        }

        val url = data?.obj("urls")?.string("regular") ?: return PlainText("图片资源获取失败")
        return buildMessageChain {
            +"PID:${data.int("pid")}\n"         // 作品ID
            +"UID:${data.int("uid")}\n"         // 作者ID
            +"标题:${data.string("title")}"     // 作品名
            +URL(url).openConnection().getInputStream().use {
                it.uploadAsImage(group)
            }
        }
    }

    fun getReplenishment(group: Long, supply: Int): String {
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val quota = UserACGImg(
            dbObject.selectOne(
                "ACGImg",
                Triple("groupID", "=", "$group"),
                "随机图片\nFile:AcgImage.kt\tLine:119"
            )
        )
        if (quota.score + supply >= 200) {
            dbObject.update(
                "ACGImg",
                Pair("groupID", "$group"),
                Pair(arrayOf("score"), arrayOf("200")),
                "随机图片\nFile:AcgImage.kt\tLine:127"
            )
            dbObject.closeDB()
            return "补给已达上限"
        }
        dbObject.update(
            "ACGImg",
            Pair("groupID", "$group"),
            Pair(arrayOf("score"), arrayOf("${quota.score + supply}")),
            "随机图片\nFile:AcgImage.kt\tLine:136"
        )
        dbObject.closeDB()
        return "是司令部的补给！色图配给+$supply"
    }
}