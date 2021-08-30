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
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.warning
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

data class MusicInfo(val type: MusicKind, val songName: String, val musicURL: String, val jumpUrl: String) {
    fun constructorMusicCard(pictureUrl: String) = MusicShare(
        type,
        songName,
        "歌曲信息 来源于 领航员-TB 智障搜索",                // 内容:会显示在title下面
        jumpUrl,
        pictureUrl,
        musicURL
    )
}

@MiraiExperimentalApi
@ConsoleExperimentalApi
object Music : SimpleCommand(
    PluginMain, "music", "点歌",
    description = "点歌姬"
) {


    override val usage: String = "${CommandManager.commandPrefix}$primaryName <平台值>\n1\t网易云\n2\tQQ"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(musicName: String, type: Int = 1) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return

        runCatching {
            val rMessage = when (type) {
                1 -> get136Music(musicName, user.avatarUrl)
                2 -> getQQMusic(musicName, user.avatarUrl)
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
        var song = songList[0]
        var mid = song.string("songmid")  // 获取歌曲MID

        var musicURL = queryRealUrl(mid)    // 获取歌曲URL

        var i = 0
        while (isExistent(musicURL).not()) {  // 若该歌曲url无效
            song = songList[i]                // 获取列表内下一首
            mid = song.string("songmid")
            musicURL = queryRealUrl(mid)
            i++
            if (i >= songList.size) return PlainText("获取歌曲信息失败，此歌曲概为付费或会员歌曲")
        }

        val songname = song.string("songname")!!
        val songid = song.int("songid")!!

        return MusicInfo(
            MusicKind.QQMusic,
            songname,
            musicURL!!,
            "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=$songid&source=qqshare&ADTAG=qqshare"
        ).constructorMusicCard(pictureUrl)
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

    private fun queryRealUrl(songMID: String?): String? {
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
