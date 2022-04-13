package com.navigatorTB_Nymph.pluginMain

import com.mayabot.nlp.module.summary.KeywordSummary
import com.mayabot.nlp.segment.Lexers
import com.navigatorTB_Nymph.command.composite.*
import com.navigatorTB_Nymph.command.dlc.MirrorWorldGame
import com.navigatorTB_Nymph.command.simple.*
import com.navigatorTB_Nymph.data.*
import com.navigatorTB_Nymph.game.crowdVerdict.VoteUser
import com.navigatorTB_Nymph.game.duel.Gun
import com.navigatorTB_Nymph.game.minesweeper.Minesweeper
import com.navigatorTB_Nymph.game.pushBox.PushBox
import com.navigatorTB_Nymph.game.ticTacToe.TicTacToe
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginConfig.MySetting.prohibitedWord
import com.navigatorTB_Nymph.pluginData.*
import com.navigatorTB_Nymph.tool.cronJob.CronJob
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
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
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.*
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
        version = "0.20.0"
    )
) {
    // 分词功能
    val LEXER = Lexers.coreBuilder()
        .withPos() //词性标注功能
        .withPersonName() // 人名识别功能
//        .withNer() // 命名实体识别
        .build()

    // 关键词提取
    val KEYWORD_SUMMARY = KeywordSummary()

    val VOTES: MutableMap<Long, VoteUser> = mutableMapOf()
    val MINESWEEPER_GAME = mutableMapOf<Long, Minesweeper>()
    val PUSH_BOX = mutableMapOf<Long, PushBox>()
    val TIC_TAC_TOE_GAME = mutableMapOf<Long, TicTacToe>()
    val BOTH_SIDES_DUEL = mutableMapOf<Member, Gun>()
    val CRON = CronJob()

    var DLC_MirrorWorld = false

    @OptIn(MiraiExperimentalApi::class)
    override fun onEnable() {
        MySetting.reload()
        MyPluginData.reload()
        UsageStatistics.reload()
        ActiveGroupList.reload()
        Article.reload()
        PushBoxLevelMap.reload()

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
        Wordle.register()           // 猜单词
        PushBoxGame.register()      // 推箱子
        RollDice.register()         // 简易骰娘
        AutoBanned.register()       // 自助禁言
        Duel.register()             // 禁言决斗
        TraceMoe.register()         // 以图搜番
        AcgImage.register()         // 随机图片
        Construction.register()     // 建造时间
        ASoulArticle.register()     // 小作文
        ShipMap.register()          // 打捞地图
        SendDynamic.register()      // 动态查询
        WikiAzurLane.register()     // 碧蓝Wiki
        CalculationExp.register()   // 经验计算器
        Birthday.register()         // 舰船下水日
        Roster.register()           // 碧蓝和谐名
        AI.register()               // 图灵数据库增删改查
        MyHelp.register()           // 帮助功能
        MirrorWorldGame.register()  // DLC_01

        DLC_MirrorWorld = PluginManager.plugins.find { plugin -> plugin.id == "MCP.TB_DLC" } != null

        // 动态更新
        PluginMain.launch {
            CRON.start()
        }
        // 入群申请
//        this.globalEventChannel().subscribeAlways<MemberJoinRequestEvent>{
//            放弃管理入群
//        }
        // 入群播报
        this.globalEventChannel().subscribeAlways<MemberJoinEvent> {
            val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
            val policy = dbObject.selectOne(
                "Policy",
                Triple("groupID", "=", "$groupId"),
                "入群播报\nFile:PluginMain.kt\tLine:133"
            ).run { UserPolicy(this) }
            dbObject.closeDB()
            if (groupId in ActiveGroupList.user && policy.groupNotification == 1)
                group.sendMessage(
                    when (this) {
                        is MemberJoinEvent.Invite -> "${invitor.nameCardOrNick}邀请${member.nameCardOrNick}大佬加入群聊"
                        is MemberJoinEvent.Active -> "欢迎大佬${member.nameCardOrNick}加入群聊"
                        is MemberJoinEvent.Retrieve -> "欢迎群主${member.nameCardOrNick}回归"
                    }
                )
        }
        // 退群播报
        this.globalEventChannel().subscribeAlways<MemberLeaveEvent> {
            val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
            val policy = dbObject.selectOne(
                "Policy",
                Triple("groupID", "=", "$groupId"),
                "退群播报\nFile:PluginMain.kt\tLine:151"
            ).run { UserPolicy(this) }
            dbObject.closeDB()
            if (groupId in ActiveGroupList.user && policy.groupNotification == 1)
                group.sendMessage(
                    when (this) {
                        is MemberLeaveEvent.Kick -> "哇啊,${member.nameCardOrNick}(${member.id})被${(operator ?: bot).nameCardOrNick}鲨掉惹"
                        is MemberLeaveEvent.Quit -> "哇啊,${member.nameCardOrNick}(${member.id})自己跑掉惹"
                    }
                )
        }

        // 入群审核
        this.globalEventChannel().subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (groupId in ActiveGroupList.user) this.accept()
        }
        // 加群后添加信息
        this.globalEventChannel().subscribeAlways<BotJoinGroupEvent> {
            SQLiteJDBC(resolveDataPath("User.db")).apply {
                insert("Policy", arrayOf("groupID"), arrayOf("${it.groupId}"), "加群数据录入:\nFile:PluginMain.kt\tLine:173")
                insert(
                    "SubscribeInfo",
                    arrayOf("groupID"),
                    arrayOf("${it.groupId}"),
                    "加群数据录入:\nFile:PluginMain.Kt\tLine:174"
                )
                insert("ACGImg", arrayOf("groupID"), arrayOf("${it.groupId}"), "加群数据录入:\nFile:PluginMain.Kt\tLine:180")
                insert(
                    "Responsible",
                    arrayOf("groupID"),
                    arrayOf("${it.groupId}"),
                    "加群数据录入:\nFile:PluginMain.Kt\tLine:181"
                )
            }.closeDB()
            ActiveGroupList.activationStatusUpdate(false)
            bot.getFriend(MySetting.AdminID)?.sendMessage("GroupName:${it.group.name}\nGroupID：${it.groupId}\nPASS")
        }
        // 退群清理
        this.globalEventChannel().subscribeAlways<BotLeaveEvent> {
            when (this) {
                is BotLeaveEvent.Kick -> SQLiteJDBC(resolveDataPath("User.db")).run {
                    val responsible = selectOne(
                        "Responsible",
                        Triple("groupID", "=", "${group.id}"),
                        "退群清理:\nFile:PluginMain.Kt\tLine:195"
                    ).let { UserResponsible(it) }
                    delete("Policy", Pair("groupID", "${group.id}"), "退群数据清理:\nFile:PluginMain.Kt\tLine:200")
                    delete("SubscribeInfo", Pair("groupID", "${group.id}"), "退群数据清理:\nFile:PluginMain.Kt\tLine:201")
                    delete("Responsible", Pair("groupID", "${group.id}"), "退群数据清理:\nFile:PluginMain.Kt\tLine:202")
                    delete("ACGImg", Pair("groupID", "${group.id}"), "退群数据清理:\nFile:PluginMain.Kt\tLine:203")
                    closeDB()
                    logger.info { "###\n事件—被移出群:\n- 群ID：${group.id}\n- 相关群负责人：${responsible.principalID}\n###" }
                    bot.getFriend(MySetting.AdminID)?.sendMessage("被移出群:${group.name}\nGroupID：${group.id}")
                }
                is BotLeaveEvent.Active -> {
                    logger.info { "###\n事件—主动退出群:\n- 群ID：${group.id}\n###" }
                    bot.getFriend(MySetting.AdminID)?.sendMessage("主动退出:${group.name}\nGroupID：${group.id}")
                }
                is BotLeaveEvent.Disband -> {
                    logger.info { "###\n事件—群被解散:\n- 群ID：${group.id}\n###" }
                    bot.getFriend(MySetting.AdminID)?.sendMessage("群被解散:${group.name}\nGroupID：${group.id}")
                }
            }
        }
        // 戳一戳
        this.globalEventChannel().subscribeAlways<NudgeEvent>
        {
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
                    logger.info { "File:PluginMain.kt\tLine:241\n发送消息失败，在该群被禁言" }
                }
            }
        }
        // 聊天触发
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.LOWEST)
        {
            atBot {
                if (group.botMuteRemaining > 0 || group.id !in ActiveGroupList.user) return@atBot
                val filterMessageList: List<Message> = message.filter { it !is At }
                val filterMessageChain: MessageChain = filterMessageList.toMessageChain()
                if ((1..5).random() <= 2) {
                    if (filterMessageChain.content.trim().contains(prohibitedWord.joinToString("|").toRegex()) &&
                        group.botPermission > this.sender.permission
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
                if (group.botMuteRemaining > 0 || group.id !in ActiveGroupList.user) return@invoke
                val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
                val policy = dbObject.selectOne(
                    "Policy",
                    Triple("groupID", "=", "${group.id}"),
                    "聊天触发:\nFile:PluginMain.Kt\tLine:276"
                ).run { UserPolicy(this) }
                dbObject.closeDB()
                runCatching {
                    val v1 = (1..100).random()
                    val v2 = if (policy.acgImgAllowed == 1) (1..100).random() else 0
                    if (v1 <= policy.triggerProbability) AI.dialogue(subject, message.content.trim())
                    if (v1 <= 99) return@invoke

                    val supply = when (v2) {
                        in 1..7 -> 3
                        in 8..19 -> 2
                        in 20..46 -> 1
                        else -> 0
                    }
                    if (supply > 0) subject.sendMessage(AcgImage.getReplenishment(subject.id, supply))
                }.onFailure { logger.debug { "问题复现：${group.id}" } }
            }
        }
        // 常驻任务
        if (MySetting.resident) residentTask()

        ActiveGroupList.activationStatusUpdate(false)
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
        val policyList = dbObject.select(
            "Policy",
            Triple("DailyReminderMode", "=", "0"),
            "每日提醒\nFile:PluginMain.kt\tLine:322"
        ).run { List(this.size) { UserPolicy(this[it]) } }
        dbObject.closeDB()
        val script = mapOf(
            1 to arrayOf(
                "Ciallo～(∠・ω< )⌒★今天是周一哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周二哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周三哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周四哦,今天开放的是「战术研修」「商船护送」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周五哦,今天开放的是「战术研修」「海域突进」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周六哦,今天开放的是「战术研修」「斩首行动」，困难也记得打呢。",
                "Ciallo～(∠・ω< )⌒★今天是周日哦,每日全部模式开放，每周两次的破交作战记得打哦，困难模式也别忘了。"
            ),
            2 to arrayOf(
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周一, 今天周回本开放「弓阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周二, 今天周回本开放「枪阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周三, 今天周回本开放「狂阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周四, 今天周回本开放「骑阶修炼场」,「收集火种(枪杀)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周五, 今天周回本开放「术阶修炼场」,「收集火种(剑骑)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周六, 今天周回本开放「杀阶修炼场」,「收集火种(弓术)」。",
                "Ciallo～(∠・ω< )⌒★晚上好,Master,今天是周日, 今天周回本开放「剑阶修炼场」,「收集火种(All)」。"
            )
        )
        for (groupPolicy in policyList) {
            if (groupPolicy.groupID !in ActiveGroupList.user) continue // 激活到期则跳过

            val group = Bot.getInstance(MySetting.BotID).getGroup(groupPolicy.groupID)
            if (group == null || group.botMuteRemaining > 0) {
                continue
            }
            when (groupPolicy.dailyReminderMode) {
                1 -> script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                2 -> script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                3 -> {
                    script[1]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                    script[2]?.get(LocalDateTime.now().dayOfWeek.value - 1)?.let { group.sendMessage(it) }
                }
                else -> logger.warning { "File:PluginMain.kt\tLine:362\n未知的模式" }
            }
        }
    }

    /* 报时 */
    private suspend inline fun tellTime() {
        logger.info { "执行任务：整点报时" }
        val time = LocalDateTime.now().hour

        if (time == 0) ActiveGroupList.activationStatusUpdate() // 更新激活状态

        val dbObject = SQLiteJDBC(resolveDataPath("AssetData.db"))
        val scriptList = dbObject.select(
            "Script",
            Triple("hour", "=", "$time"),
            "报时\nFile:PluginMain.kt\tLine:375"
        ).run { List(this.size) { AssetDataScript(this[it]) } }
        dbObject.closeDB()

        val userDbObject = SQLiteJDBC(resolveDataPath("User.db"))
        val groupList = with(
            if (time in MySetting.undisturbed) userDbObject.select(
                "Policy",
                Triple(arrayOf("undisturbed", "tellTimeMode"), Array(2) { "=" }, arrayOf("1", "0")),
                "AND",
                "报时\nFile:PluginMain.kt\tLine:384"
            ) else userDbObject.select(
                "Policy",
                Triple("tellTimeMode", "=", "0"),
                "报时\nFile:PluginMain.kt\tLine:389"
            )
        ) { List(this.size) { UserPolicy(this[it]) } }
        userDbObject.closeDB()
        val script = mutableMapOf<Int, List<AssetDataScript>>()

        for (groupPolicy in groupList) {
            if (groupPolicy.groupID !in ActiveGroupList.user) continue // 激活到期则跳过
            val group = Bot.getInstance(MySetting.BotID).getGroup(groupPolicy.groupID)
            if (group == null || group.botMuteRemaining > 0) continue
            if (groupPolicy.tellTimeMode == -1) {
                group.sendMessage("现在${time}点咯")
                continue
            }

            if (script.containsKey(groupPolicy.tellTimeMode).not()) {
                script[groupPolicy.tellTimeMode] = scriptList.filter { it.mode == groupPolicy.tellTimeMode }
            }

            val outScript = script[groupPolicy.tellTimeMode]?.random()?.content

            if (groupPolicy.tellTimeMode % 2 == 0) {
                val path = resolveDataPath("./报时语音/$outScript")
                val audio = File("$path").toExternalResource().use { group.uploadAudio(it) }
                audio.let { group.sendMessage(it) }
            } else {
                outScript?.let { group.sendMessage(it) }
            }
        }
    }

    /* 动态推送 */
    private suspend inline fun dynamicPush() {
        logger.info { "执行任务：动态推送" }
        val time = LocalDateTime.now().hour
        for (list in MyPluginData.timeStampOfDynamic) {
            val dynamic = SendDynamic.getDynamic(list.key, 0, flag = true)
            if (dynamic.timestamp == 0L) continue
            val dbObject = SQLiteJDBC(resolveDataPath("User.db"))
            val groupList = MyPluginData.nameOfDynamic[list.key]?.let {
                with(
                    if (time in MySetting.undisturbed) dbObject.executeQuerySQL(
                        "SELECT * FROM Policy JOIN SubscribeInfo USING (groupID) WHERE Policy.undisturbed = false AND SubscribeInfo.${it} = true;",
                        "动态推送\nFile:PluginMain.kt\tLine:433"
                    )
                    else dbObject.select("SubscribeInfo", Triple(it, "=", "1"), "动态推送\nFile:PluginMain.kt\tLine:437")
                ) { List(this.size) { index -> UserSubscribeInfo(this[index]) } }
            }
            dbObject.closeDB()

            if (groupList.isNullOrEmpty()) continue

            val bot = Bot.getInstance(MySetting.BotID)
            val gList = mutableListOf<Contact>()
            groupList.forEach {
                if (it.groupID in ActiveGroupList.user) { // 激活到期则跳过
                    val g = bot.getGroup(it.groupID)
                    if ((g != null) && (g.botMuteRemaining <= 0)) gList.add(g)
                }
            }
            val forwardMessage = dynamic.message2jpg().uploadAsImage(gList.random())
            gList.forEach { runCatching { it.sendMessage(forwardMessage) }.onFailure { err -> logger.warning { "File:PluginMain.kt\tLine:453\nGroup:${it.id}\n${err.message}" } } }
        }
    }

    private fun dataBastInit() {
        SQLiteJDBC(resolveDataPath("User.db")).apply {
            createTable(
                """
                CREATE TABLE "ACGImg" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "score"	INTEGER NOT NULL DEFAULT 0,
                "date"	NUMERIC NOT NULL DEFAULT 0,
                PRIMARY KEY("groupID")
                );
                """.trimIndent(),
                "初始化数据库\nFile:PluginMain.kt\tLine:459"
            )
            createTable(
                """
                CREATE TABLE "Policy" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "tellTimeMode"	INTEGER NOT NULL DEFAULT 0,
                "dailyReminderMode"	INTEGER NOT NULL DEFAULT 0,
                "teaching"	INTEGER NOT NULL DEFAULT 0,
                "triggerProbability"	INTEGER NOT NULL DEFAULT 33,
                "acgImgAllowed"	INTEGER NOT NULL DEFAULT 0,
                "undisturbed"	INTEGER NOT NULL DEFAULT 0,
                "groupNotification"	INTEGER NOT NULL DEFAULT 0
                );
                """.trimIndent(),
                "初始化数据库\nFile:PluginMain.kt\tLine:470"
            )
            createTable(
                """
                CREATE TABLE "Responsible" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "principalID"	NUMERIC NOT NULL DEFAULT 0,
                "active"	INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY("groupID")
                );
                """.trimIndent(),
                "初始化数据库\nFile:PluginMain.kt\tLine:485"
            )
            createTable(
                """
                CREATE TABLE "SubscribeInfo" (
                "groupID"	NUMERIC NOT NULL UNIQUE,
                "azurLane"	INTEGER DEFAULT 0.0,
                "arKnights"	INTEGER DEFAULT 0.0,
                "fateGrandOrder"	INTEGER DEFAULT 0.0,
                "genShin"	INTEGER DEFAULT 0.0
                );
                """.trimIndent(),
                "初始化数据库\nFile:PluginMain.kt\tLine:496"
            )
        }.closeDB()
        SQLiteJDBC(resolveDataPath("AI.db")).apply {
            createTable(
                """
            CREATE TABLE "Corpus" (
            "id"	INTEGER NOT NULL UNIQUE,
            "answer"	TEXT NOT NULL,
            "question"	TEXT NOT NULL,
            "keys"	TEXT NOT NULL,
            "fromGroup"	INTEGER NOT NULL,
            PRIMARY KEY("id" AUTOINCREMENT)
            );
            """.trimIndent(),
                "初始化数据库\nFile:PluginMain.kt\tLine:509"
            )
        }.closeDB()
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
        Schedule.unregister()           // 日程表
        TicTacToeGame.unregister()      // 井字棋
        GroupPolicy.unregister()        // 群策略
        Music.unregister()              // 点歌姬
        Calculator.unregister()         // 计算器
        ASoulArticle.unregister()       // 小作文
        Wordle.unregister()             // 猜单词
        PushBoxGame.unregister()        // 推箱子
        RollDice.unregister()           // 简易骰娘
        Construction.unregister()       // 建造时间
        TraceMoe.unregister()           // 以图搜番
        Duel.unregister()               // 禁言决斗
        MyHelp.unregister()             // 帮助功能
        AutoBanned.unregister()         // 自助禁言
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