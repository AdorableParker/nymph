package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.data.AICorpus
import com.navigatorTB_Nymph.data.UserPolicy
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

object AI : CompositeCommand(
    PluginMain, "AI", description = "AI功能"
) {

    @SubCommand("教学")
    suspend fun MemberCommandSenderOnMessage.main(question: String, answer: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val userDBObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        val policy = UserPolicy(
            userDBObject.selectOne(
                "Policy", Triple("groupID", "=", "${group.id}"), "AI教学\nFile:AI.kt\tLine:30"
            )
        )
        userDBObject.closeDB()
        if (policy.teaching == 0) {
            sendMessage("本群禁止教学,请联系管理员开启")
            return
        }
        val keyWord = PluginMain.KEYWORD_SUMMARY.keyword(question, 1).let { if (it.size <= 0) question else it[0] }
//        PluginMain.logger.debug { "*$question*\n$keyWord" }
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        dbObject.executeQuerySQL(
            "SELECT * FROM Corpus WHERE answer = $answer AND question = $question AND keys = $keyWord AND (fromGroup = ${group.id} OR fromGroup = 0);",
            "AI教学\nFile:AI.kt\tLine:41"
        ).run {
            if (isNotEmpty()) {
                dbObject.closeDB()
                sendMessage("问题:$question\n回答:$answer\n该条目已存在，条目ID:${AICorpus(this[0]).id}")
                return
            }
        }

        dbObject.insert(
            "Corpus",
            arrayOf("answer", "question", "keys", "fromGroup"),
            arrayOf("'$answer'", "'$question'", "'$keyWord'", "${group.id}"),
            "AI教学\nFile:AI.kt\tLine:52"
        )
        val entry = AICorpus(
            dbObject.selectOne(
                "Corpus", Triple(
                    arrayOf("answer", "question", "keys", "fromGroup"),
                    Array(4) { "=" },
                    arrayOf(answer, question, keyWord, "${group.id}")
                ), "AND", "AI教学\nFile:AI.kt\tLine:59"
            )
        )

        dbObject.closeDB()
        if ((1..10).random() <= 1) {
            val audio = File("${PluginMain.resolveDataPath("./雷-原来如此.amr")}").toExternalResource().use {
                group.uploadAudio(it)
            }
            sendMessage(audio)
        }
        sendMessage("问题:$question\n回答:$answer\n条目已添加，条目ID:${entry.id}")
    }

    @SubCommand("查询")
    suspend fun MemberCommandSenderOnMessage.main(key: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        val entryList = dbObject.executeQuerySQL(
            "SELECT * FROM Corpus WHERE answer GLOB '*$key*' OR question GLOB '*$key*' OR keys GLOB '*$key*';",
            "AI查询\nFile:AI.kt\tLine:87"
        ).run {
            List(size) { AICorpus(this[it]) }
        }
        dbObject.closeDB()
        val r = when {
            entryList.isEmpty() -> "问答包含关键词${key}的条目不存在"
            entryList.size >= 30 -> "问答包含关键词${key}的条目过多(超过三十条)，请提供更加详细的关键词"
            entryList.size >= 10 -> {
                val report = mutableListOf("问答包含关键词${key}的条目过多(超过十条)，仅提供前十条，本群关键词优先显示")
                entryList.forEach {
                    if (report.size <= 10)
                        if (it.fromGroup == group.id)
                            report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:完全控制\n")
                        else if (it.fromGroup == 0L)
                            report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:只读权限\n")
                }
                report.joinToString("\n")
            }
            else -> {
                val report = mutableListOf("条目清单:")
                entryList.forEach {
                    when (it.fromGroup) {
                        group.id -> report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:完全控制\n")
                        0L -> report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:只读权限\n")
                        else -> report.add("问题:隐藏\t回答:隐藏\n条目ID:${it.id}\n控制权限:不可操作\n")
                    }
                }
                report.joinToString("\n")
            }
        }
        sendMessage(r)
    }

    @SubCommand("统计")
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        val entryList = dbObject.executeQuerySQL("SELECT * FROM Corpus;", "AI统计\nFile:AI.kt\tLine:132").run {
            List(size) { AICorpus(this[it]) }
        }
        dbObject.closeDB()
        val cSpecial = entryList.count { it.fromGroup == group.id }
        val cAvailable = entryList.count { it.fromGroup == 0L } + cSpecial
        sendMessage(
            if (entryList.isEmpty()) "统计查询失败" else "目前数据库教学数据共计${entryList.size}条\n本群可读教学数据${cAvailable}条\n其中本群专属教学数据${cSpecial}条\n占本群可读的${
                "%.2f".format(
                    cSpecial.toDouble() / cAvailable * 100
                )
            }%,占数据库总量的${"%.2f".format(cSpecial.toDouble() / entryList.size * 100)}%"
        )
    }

    @SubCommand("EID查询")
    suspend fun MemberCommandSenderOnMessage.eIDMain(EID: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        val entryList = dbObject.select("Corpus", Triple("id", "=", "$EID"), "AI_EID查询\nFile:AI.kt\tLine:156").run {
            List(size) { AICorpus(this[it]) }
        }
        dbObject.closeDB()
        sendMessage(when {
            entryList.isEmpty() -> "条目${EID}不存在"
            else -> {
                val report = mutableListOf("条目清单:")
                entryList.forEach {
                    when (it.fromGroup) {
                        group.id -> report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:完全控制")
                        0L -> report.add("问题:${it.question}\n回答:${it.answer}\n条目ID:${it.id}\n控制权限:只读权限")
                        else -> report.add("问题:隐藏\t回答:隐藏\n条目ID:${it.id}\n控制权限:不可操作")
                    }
                }
                report.joinToString("\n")
            }
        })
    }

    @SubCommand("删除")
    suspend fun MemberCommandSenderOnMessage.main(EID: Int) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        val entry = dbObject.selectOne("Corpus", Triple("id", "=", "$EID"), "AI删除\nFile:AI.kt\tLine:185").run {
            if (isEmpty()) {
                dbObject.closeDB()
                sendMessage("没有该条目")
                return
            }
            AICorpus(this)
        }
        if (entry.fromGroup == group.id || user.id == MySetting.AdminID) {
            dbObject.delete("Corpus", Pair("id", "$EID"), "AI删除\nFile:AI.kt\tLine:190")
            sendMessage("问题:${entry.question}\n回答:${entry.answer}\n条目ID:${entry.id}\n条目已删除")
            return dbObject.closeDB()
        } else sendMessage("权限不足")
        dbObject.closeDB()
    }

    suspend fun dialogue(subject: Group, content: String, atMe: Boolean = false) {
        val keyWord = PluginMain.KEYWORD_SUMMARY.keyword(content, 1).let { if (it.size <= 0) content else it[0] }
        val wordList = PluginMain.LEXER.scan(content).toList()

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AI.db"))
        val rList = with(
            if (keyWord.isNullOrBlank())
                dbObject.select("Corpus", Triple("answer", "=", "'$content'"), "AI对话\nFile:AI.kt\tLine:205")
            else
                dbObject.select("Corpus", Triple("keys", "=", "'$keyWord'"), "AI对话\nFile:AI.kt\tLine:208")
        ) { List(size) { AICorpus(this[it]) } }
        dbObject.closeDB()
        val r = mutableListOf<String>()
        var jaccardMax = 0.6
        rList.forEach {
            if (it.fromGroup == subject.id || it.fromGroup == 0L) {
                val a = mutableListOf<String>()
                val b = mutableListOf<String>()
                PluginMain.LEXER.scan(it.question).toList().forEach { wordTermA -> a.add(wordTermA.toString()) }
                wordList.forEach { wordTermB -> b.add(wordTermB.toString()) }

                val jaccardIndex = (a intersect b.toSet()).size.toDouble() / (a union b).size.toDouble()
                when {
                    jaccardIndex > jaccardMax -> {
                        jaccardMax = jaccardIndex
                        r.clear()
                        r.add(it.answer)
                    }
                    jaccardIndex == jaccardMax -> r.add(it.answer)
                }
            }
        }
        if (r.size > 0) {
            subject.sendMessage(r.random())
            return
        }
        if (atMe) subject.sendMessage("( -`_´- ) (似乎并没有听懂... ")
    }
}