/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URL

// 碧蓝几大基本榜单查询
@MiraiExperimentalApi
@ConsoleExperimentalApi
object WikiAzurLane : CompositeCommand(
    PluginMain, "WikiAzurLane", "碧蓝wiki", // "primaryName" 是主指令名
    description = "碧蓝几大基本榜单查询"
) {
    override val usage: String =
        "${CommandManager.commandPrefix}碧蓝wiki [榜单ID]\n" +
                "榜单ID列表：\n" +
                "*1* 强度榜,强度主榜\n" +
                "*2* 强度副榜\n" +
                "*3* 装备榜\n" +
                "*4* P站榜,社保榜"

    @SubCommand("强度榜", "强度主榜")
    suspend fun MemberCommandSenderOnMessage.strengthRanking() {
        getWikiImg("PVE用舰船综合性能强度榜", 1).use {
            if (it != null) {
                subject.sendImage(it)
            } else {
                sendMessage("访问Wiki失败惹,这一定是塞壬的阴谋\nε(┬┬﹏┬┬)3")
            }
        }
    }

    @SubCommand("强度副榜")
    suspend fun MemberCommandSenderOnMessage.strengthDeputyRanking() {
        getWikiImg("PVE用舰船综合性能强度榜", 2).use {
            if (it != null) {
                subject.sendImage(it)
            } else {
                sendMessage("访问Wiki失败惹,这一定是塞壬的阴谋\nε(┬┬﹏┬┬)3")

            }
        }
    }

    @SubCommand("装备榜")
    suspend fun MemberCommandSenderOnMessage.equipmentRanking() {
        getWikiImg("装备一图榜", 0).use {
            if (it != null) {
                subject.sendImage(it)
            } else {
                sendMessage("访问Wiki失败惹,这一定是塞壬的阴谋\nε(┬┬﹏┬┬)3")
            }
        }
    }

    @SubCommand("P站榜", "社保榜")
    suspend fun MemberCommandSenderOnMessage.pixivRanking() {
        getWikiImg("P站搜索结果一览榜（社保榜）", 0).use {
            if (it != null) {
                subject.sendImage(it)
            } else {
                sendMessage("访问Wiki失败惹,这一定是塞壬的阴谋\nε(┬┬﹏┬┬)3")
            }
        }
    }

    private fun getWikiImg(index: String, sub: Int): InputStream? {
        record(primaryName)
        val doc = Jsoup.connect("https://wiki.biligame.com/blhx/$index").get()
        val links = doc.select("div#mw-content-text").select(".mw-parser-output").select("img[src]")
        runCatching {
            val url = URL(links[sub].attr("abs:src"))
            return url.openConnection().getInputStream()
        }.onFailure {
            PluginMain.logger.warning(it.cause)
        }
        return null
    }
}