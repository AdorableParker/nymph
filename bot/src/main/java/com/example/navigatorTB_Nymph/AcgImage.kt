package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand


object AcgImage : SimpleCommand(
    PluginMain, "acgImage", "随机图片",
    description = "随机发送acg图片"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        record(primaryName)
        if (group.botMuteRemaining > 0) return
        sendMessage("功能整改中")
/*
        if (MyPluginData.AcgImageRun.contains(group.id)) {
            sendMessage("功能运行中，请等待")
            return
        }
        MyPluginData.AcgImageRun.add(group.id)
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val status = dbObject.selectOne("Policy", "group_id", group.id, 1)
        if (status["ACGImgAllowed"] != 1) {
            dbObject.closeDB()
            sendMessage("本群尚未启用该功能，请联系群主或管理员开启")
            MyPluginData.AcgImageRun.remove(group.id)
            return
        }

        val quota = dbObject.selectOne("ACGImg", "group_id", group.id, 1)
        val score = quota["score"] as Int
        val date = (quota["date"] as Int).toLong()
        val timeLeft = 60 - Instant.now().epochSecond + date
        if (score <= 0) {
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
            getRandomImg()?.let { inputStream ->
                inputStream.toExternalResource().use {
                    group.sendImage(it)
                }
                dbObject.update(
                    "ACGImg",
                    "group_id",
                    "${group.id}",
                    arrayOf("score", "date"),
                    arrayOf("${score - 1}", "${Instant.now().epochSecond}")
                )
                if (score - 1 < 10) {
                    sendMessage("ℹ本群剩余配给已经不足10点了")
                }
            } ?: throw IllegalAccessException("图片数据流为空")
        }.onFailure {
            PluginMain.logger.warning { "File:AcgImage.kt    Line:78\n${it.message}" }
            sendMessage("数据传输失败...嗯.一定是塞壬的问题..")
        }
        dbObject.closeDB()
        MyPluginData.AcgImageRun.remove(group.id)
 */
    }

/*
    private fun getRandomImg(): InputStream? {
        val webClient = WebClient(BrowserVersion.EDGE) //新建一个浏览器客户端对象 指定内核
        webClient.options.isCssEnabled = false //是否启用CSS, 因为不需要展现页面, 所以不需要启用
        runCatching {
            val page: HtmlPage = webClient.getPage(MySetting.ImageHostingService) //尝试加载给出的网页
            val link = Jsoup.parse(page.asXml()).text()
            webClient.close()
            //            return ImmutableImageleImage.loader().fromStream(inputStream).bytes(PngWriter.MaxCompression).inputStream() // com.sksamuel.scrimage.ImageParseException 原因不详
            return URL(link).openConnection().getInputStream()
        }.onFailure {
            PluginMain.logger.warning("File:AcgImage.kt\tLine:95\n$it")
        }
        webClient.close()
        return null
    }
 */

    fun getReplenishment(group: Long, supply: Int): String {
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val quota = dbObject.selectOne("ACGImg", "group_id", group, 1)["score"] as Int
        if (quota + supply >= 200) {
            dbObject.update("ACGImg", "group_id", group, "score", 200)
            dbObject.closeDB()
            return "补给已达上限"
        }
        dbObject.update("ACGImg", "group_id", group, "score", quota + supply)
        dbObject.closeDB()
        return "是司令部的补给！色图配给+$supply"
    }


}