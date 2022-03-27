package com.navigatorTB_Nymph.command.simple

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.navigatorTB_Nymph.data.MusicInfo
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.warning
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

object Music : SimpleCommand(
    PluginMain, "music", "点歌",
    description = "点歌姬"
) {


    override val usage: String = "${CommandManager.commandPrefix}$primaryName <平台值>\n1\tQQ\n2\t网易云"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(musicName: String, type: Int = 1) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        runCatching {
            val rMessage = when (type) {
                1 -> getQQMusic(musicName, user.avatarUrl)
                2 -> get136Music(musicName, user.avatarUrl)
                else -> PlainText("不认识的搜索源")
            }
            sendMessage(rMessage)
        }.onFailure {
            PluginMain.logger.warning { "File:Music.kt\tLine:60\n$it" }
            sendMessage("点歌失败，未知的失败原因")
        }
    }

    private fun get136Music(musicName: String, pictureUrl: String): Message {
        val doc = Jsoup.connect("https://music.163.com/api/search/get/web").data(
            mapOf(
                "type" to "1",
                "s" to musicName
            )
        ).ignoreContentType(true).execute().body().toString()

        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject

        if (jsonObj.int("code") != 200)
            return PlainText("搜索歌曲异常\nHTTP状态码：${jsonObj.int("code")}")

        val musicList = jsonObj.obj("result")
        if (musicList.isNullOrEmpty() || musicList.int("songCount")!! <= 0)
            return PlainText("搜索结果列表为空")

        val musicInfo = musicList.array<JsonObject>("songs")?.get(0)
        if (musicInfo.isNullOrEmpty()) return PlainText("获取歌曲信息失败")

        val name = musicInfo.string("name")!!
        val musicID = musicInfo.long("id")
        return MusicInfo(
            MusicKind.NeteaseCloudMusic,
            name,
            "http://music.163.com/song/media/outer/url?id=$musicID.mp3",
            "https://music.163.com/#/song?id=$musicID"
        ).constructorMusicCard(pictureUrl)
    }

    private fun getQQMusic(musicName: String, pictureUrl: String): Message {
        val doc = Jsoup.connect("https://c.y.qq.com/soso/fcgi-bin/client_search_cp").data(
            mapOf(
                "p" to "1",
                "cr" to "1",
                "aggr" to "1",
                "flag_qc" to "0",
                "n" to "3",
                "w" to musicName,
                "format" to "json"
            )
        )
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73")
            .ignoreContentType(true).execute().body().toString()

        val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject

        val songList = jsonObj.obj("data")?.obj("song")?.array<JsonObject>("list")
        if (songList.isNullOrEmpty()) return PlainText("搜索结果列表为空")

        for(song in songList){
            val musicURL = queryRealUrl(song.string("songmid").toString())    // 获取歌曲URL
            if (isExistent(musicURL)){
                return MusicInfo(
                    MusicKind.QQMusic,
                    song.string("songname").toString(),
                    musicURL!!,
                    "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=${song.int("songid")}&source=qqshare&ADTAG=qqshare"
                ).constructorMusicCard(pictureUrl)
            }
        }
        return PlainText("获取歌曲信息失败，此歌曲概为付费或会员歌曲")
    }

    private fun isExistent(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return try {
            val huc: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            huc.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36"
            )
            huc.requestMethod = "HEAD"
            huc.connect()
            huc.responseCode == 200
        } catch (ex: java.lang.Exception) {
            false
        }
    }

    private fun queryRealUrl(songMID: String): String? {
        try {
            val doc = Jsoup.connect("https://u.y.qq.com/cgi-bin/musicu.fcg").data(
                mapOf(
                    "format" to "json",
                    "data" to """{"req_0":{"module":"vkey.GetVkeyServer","method":"CgiGetVkey","param":{"guid":"0","songmid":["$songMID"],"songtype":[0],"loginflag":1,"platform":"20"}},"comm":{"uin":"18585073516","format":"json","ct":24,"cv":0}}"""
                )
            )
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73")
                .header("Host", "u.y.qq.com")
                .ignoreContentType(true).execute().body().toString()

            val jsonObj = Parser.default().parse(StringBuilder(doc)) as JsonObject
            if (jsonObj.int("code") != 0) return null

            return jsonObj.obj("req_0")?.obj("data")?.array<String>("sip")?.get(0) +
                    jsonObj.obj("req_0")?.obj("data")?.array<JsonObject>("midurlinfo")?.get(0)?.string("purl")

        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return null
    }

}