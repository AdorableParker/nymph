/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.example.navigatorTB_Nymph


import com.example.navigatorTB_Nymph.MyPluginData.nameOfDynamic
import com.example.navigatorTB_Nymph.MySetting.prohibitedWord
import com.mayabot.nlp.module.summary.KeywordSummary
import com.mayabot.nlp.segment.Lexers.coreBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.io.File
import java.time.LocalDateTime


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.navigatorTB_Nymph",
        name = "navigatorTB",
        version = "0.16.1"
    )
) {
    // 分词功能
    val LEXER = coreBuilder()
        .withPos() //词性标注功能
        .withPersonName() // 人名识别功能
//        .withNer() // 命名实体识别
        .build()

    // 关键词提取
    val KEYWORD_SUMMARY = KeywordSummary()

    val VOTES: MutableMap<Long, VoteUser> = mutableMapOf()
    val MINESWEEPER_GAME = mutableMapOf<Long, Minesweeper>()
    val TicTacToe_GAME = mutableMapOf<Long, TicTacToe>()
    val BothSidesDuel = mutableMapOf<Member, Gun>()
    val CRON = CronJob()

    var DLC_MirrorWorld = false

    @OptIn(MiraiExperimentalApi::class)
    override fun onEnable() {
        MySetting.reload()
        MyPluginData.reload()  //(PluginMain.kt:70)
        UsageStatistics.reload()

        if (MyPluginData.initialization) {  // 首次启动初始化数据库
            dataBastInit()
            MyPluginData.initialization = false
        } else {                            // 重置状态数据防止出现状态锁定
            MyPluginData.AcgImageRun.clear()
        }

        Tarot.register()            // 塔罗
        SignIn.register()           // 签到
        OneWord.register()          // 一言
        CrowdVerdict.register()     // 众裁
        SauceNAO.register()         // 搜图
        MinesweeperGame.register()  // 扫雷
        Test.register()             // 测试
        Schedule.register()         // 日程表
        TicTacToeGame.register()    // 井字棋
        Calculator.register()       // 计算器
        Music.register()            // 点歌姬
        GroupPolicy.register()      // 群策略
        RollDice.register()         // 简易骰娘
        AutoBanned.register()       // 自助禁言
        Duel.register()             // 禁言决斗
        TraceMoe.register()         // 以图搜番
        AcgImage.register()         // 随机图片
        Construction.register()     // 建造时间
        ShipMap.register()          // 打捞地图
        SendDynamic.register()      // 动态查询
        Request.register()          // 加群操作
        WikiAzurLane.register()     // 碧蓝Wiki
        CalculationExp.register()   // 经验计算器
        Birthday.register()         // 舰船下水日
        Roster.register()           // 碧蓝和谐名
        AssetDataAccess.register()  // 资源数据库处理
        AI.register()               // 图灵数据库增删改查
        MyHelp.register()           // 帮助功能
        MirrorWorldGame.register()  // DLC_01

        DLC_MirrorWorld = PluginManager.plugins.find { plugin -> plugin.id == "MCP.TB_DLC" } != null

        // 动态更新
        PluginMain.launch {
            CRON.start()
        }
        // 入群审核
        this.globalEventChannel().subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            logger.debug { "File:PluginMain.kt\tLine:135\nGroupName:${it.groupName}\nGroupID：${it.groupId}" }
            MyPluginData.groupIdList.forEach { (groupID, user) ->
                logger.debug { "GroupID:$groupID\tUserID：${user.principal_ID}\tFrom：${user.from}" }
            }
            if (MyPluginData.groupIdList.contains(it.groupId)) {
                val gc = MyPluginData.groupIdList[it.groupId]!! // 获取群证书
                it.accept()
                val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
                if (gc.flag) {
                    dbObject.update("Policy", "group_id", "${gc.from}", "group_id", "${it.groupId}")
                    dbObject.update("SubscribeInfo", "group_id", "${gc.from}", "group_id", "${it.groupId}")
                    dbObject.update("Responsible", "group_id", "${gc.from}", "group_id", "${it.groupId}")
                    dbObject.update("ACGImg", "group_id", "${gc.from}", "group_id", "${it.groupId}")
                    val ancestor = Bot.getInstance(MySetting.BotID).getGroup(gc.from)
                    if (ancestor != null) {
                        ancestor.sendMessage("受继承群已接受继承，即将退出本群")
                        MyPluginData.pactList.remove(gc.from)
                        ancestor.quit()
                    }
                } else {
                    dbObject.insert("Policy", arrayOf("group_id"), arrayOf("${it.groupId}"))
                    dbObject.insert("SubscribeInfo", arrayOf("group_id"), arrayOf("${it.groupId}"))
                    dbObject.insert("ACGImg", arrayOf("group_id"), arrayOf("${it.groupId}"))
                    dbObject.insert(
                        "Responsible",
                        arrayOf("group_id", "principal_ID"),
                        arrayOf("${it.groupId}", "${gc.principal_ID}")
                    )
                }
                MyPluginData.groupIdList.remove(it.groupId)
                dbObject.closeDB()
                logger.info { "PASS" }
                bot.getFriend(MySetting.AdminID)?.sendMessage("GroupName:${it.groupName}\nGroupID：${it.groupId}\nPASS")
            } else {
                it.ignore()
                logger.info { "FAIL" }
            }
        }
        // 退群清理
        this.globalEventChannel().subscribeAlways<BotLeaveEvent.Kick> {
            val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
            val pR = dbObject.selectOne("Responsible", "group_id", group.id, 1)
            dbObject.delete("Policy", "group_id", group.id.toString())
            dbObject.delete("SubscribeInfo", "group_id", group.id.toString())
            dbObject.delete("Responsible", "group_id", group.id.toString())
            dbObject.delete("ACGImg", "group_id", group.id.toString())
            dbObject.closeDB()
            logger.info { "###\n事件—被移出群:\n- 群ID：${group.id}\n- 相关群负责人：${pR["principal_ID"]}\n###" }
        }
        // 戳一戳
        this.globalEventChannel().subscribeAlways<NudgeEvent> {
            if (this.target == bot && this.from != bot) {
                runCatching {
                    if ((1..5).random() <= 4) {
                        subject.sendMessage(
                            arrayOf(
                                "指挥官，请不要做出这种行为",
                                "这只是全息交互界面",
                                "指挥官，请专心于工作",
                                "全息投影是不会被接触到的",
                                "指挥官，我一直陪着你哦",
                                "可望不可及",
                                "请不要试图干扰全息投影",
                                "传输...信.号...数据...干扰..."
                            ).random()
                        )
                    } else {
                        this.from.nudge().sendTo(subject)
                        subject.sendMessage("戳回去")
                    }
                }.onFailure {
                    logger.info { "File:PluginMain.kt\tLine:322\n发送消息失败，在该群被禁言" }
                }
            }
        }
        // 聊天触发
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.LOWEST) {
            atBot {
                if (group.botMuteRemaining > 0) return@atBot
                val filterMessageList: List<Message> = message.filter { it !is At }
                val filterMessageChain: MessageChain = filterMessageList.toMessageChain()
                if ((1..5).random() <= 2) {
                    if (filterMessageChain.content.trim()
                            .contains(prohibitedWord.toRegex()) && group.botPermission > this.sender.permission
                    ) {
                        this.sender.mute((300..900).random())
                        group.sendMessage(
                            arrayOf(
                                "给爷爬╰（‵□′）╯",
                                "爬远点(ノ｀Д)ノ",
                                "再您妈的见(#◠‿◠)",
                                "给大佬递口球~(￣▽￣)~*",
                                "可是，这值得吗(⊙o⊙)？",
                                "一条指令，一切都索然无味┑(￣Д ￣)┍",
                                "￣へ￣"
                            ).random()
                        )
                        return@atBot
                    }
                }
                AI.dialogue(subject, filterMessageChain.content.trim(), true)
            }
            atBot().not().invoke {
                if (group.botMuteRemaining > 0) return@invoke
                val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
                val groupInfo = dbObject.selectOne("Policy", "group_id", group.id, 1)
                dbObject.closeDB()

                try {
                    val numerator = groupInfo["TriggerProbability"] as Int
                    val v1 = (1..100).random()
                    val v2 = if (groupInfo["ACGImgAllowed"] as Int == 1) (1..100).random() else 0
                    //                PluginMain.logger.info { "不at执行这里,$v" }
                    if (v1 <= numerator) AI.dialogue(subject, message.content.trim())
                    if (v1 <= 99) return@invoke

                    val supply = when (v2) {
                        in 1..7 -> 10
                        in 8..19 -> 4
                        in 20..46 -> 1
                        else -> 0
                    }
                    if (supply > 0) {
                        subject.sendMessage(AcgImage.getReplenishment(subject.id, supply))
                    }
                } catch (e: NullPointerException) {
                    for (i in 0..10) {
                        logger.debug { "问题复现：" }
                        logger.debug { group.id.toString() }
                    }
                }
            }
        }
        // 常驻任务
        if (MySetting.resident) {
            residentTask()
        }
    }

    private fun residentTask() {
        val t = LocalDateTime.now() //序列号
        val n1 = CRON.getNext(
            t.dayOfYear * 10000 + t.hour * 100 + t.minute,
            t.year,
            Interval(0, 0, 10 - t.minute % 10)
        ) // 下一个10分
        //            val n2 = t.dayOfYear * 10000 + t.hour * 100 // 现在整点
        //            val n3 = t.dayOfYear * 10000 + 20 * 100     // 今天20点
        CRON.addJob(n1, Interval(0, 0, 3)) { dynamicPush() }
        CRON.addJob(t.dayOfYear * 10000 + t.hour * 100, Interval(0, 1, 0)) { tellTime() }
        CRON.addJob(t.dayOfYear * 10000 + 20 * 100, Interval(1, 0, 3)) { dailyReminder() }
    }

    /* 每日提醒 */
    private suspend inline fun dailyReminder() {
        logger.info { "执行任务：每日提醒" }
        val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
        val groupList = dbObject.select("Policy", "DailyReminderMode", 0, 5)
        dbObject.closeDB()
        val script = mapOf(
            1 to arrayListOf(
                "Ciallo～(∠・ω< )⌒★今天是周一哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周二哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周三哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周四哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周五哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周六哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周日哦,每日全部模式开放，每周两次的破交作战记得打哦，困难模式也别忘了。"
            ),
            2 to arrayListOf(
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周一, 今天周回本开放「弓阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周二, 今天周回本开放「枪阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周三, 今天周回本开放「狂阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周四, 今天周回本开放「骑阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周五, 今天周回本开放「术阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周六, 今天周回本开放「杀阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周日, 今天周回本开放「剑阶修炼场」,「收集火种(All)」。"
            )
        )
        for (groupPolicy in groupList) {
            val groupID = groupPolicy["group_id"] as Int
            val group = Bot.getInstance(MySetting.BotID).getGroup(groupID.toLong())
            if (group == null || group.botMuteRemaining > 0) {
                continue
            }
            when (groupPolicy["DailyReminderMode"]) {
                1 -> script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                2 -> script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                3 -> {
                    script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                    script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                }
                else -> logger.warning { "File:PluginMain.kt\tLine:242\n未知的模式" }
            }
        }
    }

    /* 报时 */
    private suspend inline fun tellTime() {
        logger.info { "执行任务：整点报时" }
        val time = LocalDateTime.now().hour
        val dbObject = SQLiteJDBC(resolveDataPath("AssetData.db"))
        val scriptList = dbObject.select("script", "Hour", time, 1)
        dbObject.closeDB()

        val userDbObject = SQLiteJDBC(resolveDataPath("User.db"))
        val groupList = if (time in MySetting.undisturbed) { // 免打扰模式判断
            userDbObject.select("Policy", listOf("undisturbed", "TellTimeMode"), listOf("1", "0"), "AND", 5)
        } else userDbObject.select("Policy", "TellTimeMode", 0, 5)

        userDbObject.closeDB()
        val script = mutableMapOf<Int, List<MutableMap<String?, Any?>>>()

        for (groupPolicy in groupList) {
            val groupID = groupPolicy["group_id"] as Int
            val group = Bot.getInstance(MySetting.BotID).getGroup(groupID.toLong())
            if (group == null || group.botMuteRemaining > 0) continue

            val groupMode = groupPolicy["TellTimeMode"] as Int
            if (groupMode == -1) {
                group.sendMessage("现在${time}点咯")
                continue
            }

            if (script.containsKey(groupMode).not()) {
                script[groupPolicy["TellTimeMode"] as Int] =
                    scriptList.filter { it["mode"] == groupPolicy["TellTimeMode"] }
            }
            val outScript = script[groupMode]?.random()?.get("content") as String

            if (groupMode % 2 == 0) {      //偶数
                val path = resolveDataPath("./报时语音/$outScript")
                val audio = File("$path").toExternalResource().use {
                    group.uploadAudio(it)
                }
                audio.let { group.sendMessage(it) }
            } else {                      //奇数
                group.sendMessage(outScript)
            }
        }
    }

    /* 动态推送 */
    private suspend inline fun dynamicPush() {
        logger.info { "执行任务：动态推送" }
        for (list in MyPluginData.timeStampOfDynamic) {
            val dynamic = SendDynamic.getDynamic(list.key, 0, flag = true)
            if (dynamic.timestamp == 0L) continue
            val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
            val groupList = nameOfDynamic[list.key]?.let {
                if (LocalDateTime.now().hour in MySetting.undisturbed) { // 免打扰模式判断
                    dbObject.executeStatement(
                        "SELECT * FROM Policy JOIN SubscribeInfo USING (group_id) " +
                                "WHERE Policy.undisturbed = false AND SubscribeInfo.${it} = true;"
                    )
                } else dbObject.select("SubscribeInfo", it, 1.0, 1)
            }
            dbObject.closeDB()

            if (groupList.isNullOrEmpty()) continue

            val bot = Bot.getInstance(MySetting.BotID)
            val gList = mutableListOf<Contact>()
            groupList.forEach {
                val g = bot.getGroup((it["group_id"] as Int).toLong())
                if ((g != null) && (g.botMuteRemaining <= 0)) gList.add(g)
            }
            val forwardMessage = dynamic.message2jpg().uploadAsImage(gList.random())

            for (g in gList) {
                runCatching {
                    g.sendMessage(forwardMessage)
                }.onFailure {
                    logger.warning { "File:PluginMain.kt\tLine:475\nGroup:${g.id}\n${it.message}" }
                }
            }
        }
    }

    private fun dataBastInit() {
        val userDB = SQLiteJDBC(resolveDataPath("User.db"))
        userDB.createTable(
            """
                CREATE TABLE "ACGImg" (
                	"group_id"	INTEGER NOT NULL UNIQUE,
                	"score"	INTEGER NOT NULL DEFAULT 0,
                	"date"	INTEGER NOT NULL DEFAULT 0,
                	PRIMARY KEY("group_id")
                );
            """.trimIndent()
        )
        userDB.createTable(
            """
                CREATE TABLE "Policy" (
                	"group_id"	NUMERIC NOT NULL UNIQUE,
                	"TellTimeMode"	INTEGER NOT NULL DEFAULT 0,
                	"DailyReminderMode"	INTEGER NOT NULL DEFAULT 0,
                	"Teaching"	INTEGER NOT NULL DEFAULT 0,
                	"TriggerProbability"	INTEGER NOT NULL DEFAULT 33,
                	"ACGImgAllowed"	INTEGER NOT NULL DEFAULT 0,
                    "undisturbed"   INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent()
        )
        userDB.createTable(
            """
                CREATE TABLE "Responsible" (
                	"group_id"	INTEGER NOT NULL UNIQUE,
                	"principal_ID"	NUMERIC NOT NULL
                );
            """.trimIndent()
        )
        userDB.createTable(
            """
                CREATE TABLE "SubscribeInfo" (
                	"group_id"	NUMERIC NOT NULL UNIQUE,
                	"AzurLane"	INTEGER DEFAULT 0.0,
                	"ArKnights"	INTEGER DEFAULT 0.0,
                	"FateGrandOrder"	INTEGER DEFAULT 0.0,
                	"GenShin"	INTEGER DEFAULT 0.0
                );
            """.trimIndent()
        )
        userDB.closeDB()
        val aiDB = SQLiteJDBC(resolveDataPath("AI.db"))
        aiDB.createTable(
            """
            CREATE TABLE "Corpus" (
            	"ID"	INTEGER NOT NULL,
            	"answer"	TEXT NOT NULL,
            	"question"	TEXT NOT NULL,
            	"keys"	TEXT NOT NULL,
            	"fromGroup"	INTEGER NOT NULL,
            	PRIMARY KEY("ID" AUTOINCREMENT)
            );
        """.trimIndent()
        )
        aiDB.closeDB()
        logger.info("初始化基础数据库完成")
        logger.warning("请自行检查 AssetData.db 是否存在于 Data 目录")
    }

    override fun onDisable() {
//        PluginMain.launch{ announcement("正在关闭") } // 关闭太快发不出来
        MirrorWorldGame.unregister()  // DLC_01
        Tarot.unregister()              // 塔罗
        SignIn.unregister()             // 签到
        OneWord.unregister()            // 一言
        CrowdVerdict.unregister()       // 众裁
        SauceNAO.unregister()           // 搜图
        Test.unregister()               // 测试
        MinesweeperGame.unregister()    // 扫雷
        Schedule.unregister()         // 日程表
        TicTacToeGame.unregister()      // 井字棋
        GroupPolicy.unregister()        // 群策略
        Music.unregister()              // 点歌姬
        Calculator.unregister()         // 计算器
        RollDice.unregister()           // 简易骰娘
        Construction.unregister()       // 建造时间
        TraceMoe.unregister()           // 以图搜番
        Duel.unregister()               // 禁言决斗
        MyHelp.unregister()             // 帮助功能
        AutoBanned.unregister()         // 自助禁言
        Request.unregister()            // 加群操作
        AcgImage.unregister()           // 随机图片
        ShipMap.unregister()            // 打捞地图
        SendDynamic.unregister()        // 动态查询
        WikiAzurLane.unregister()       // 碧蓝Wiki
        Roster.unregister()             // 碧蓝和谐名
        Birthday.unregister()           // 舰船下水日
        CalculationExp.unregister()     // 经验计算器
        AI.unregister()                 // 图灵数据库增删改查
        PluginMain.cancel()
    }
}