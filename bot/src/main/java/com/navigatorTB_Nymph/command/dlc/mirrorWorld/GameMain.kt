package com.navigatorTB_Nymph.command.dlc.mirrorWorld

import com.navigatorTB_Nymph.command.dlc.data.PermanentData
import com.navigatorTB_Nymph.command.dlc.gameRole.*
import com.navigatorTB_Nymph.command.dlc.item.Cube
import com.navigatorTB_Nymph.command.dlc.item.Item
import com.navigatorTB_Nymph.command.dlc.tool.BattleRecordTool
import com.navigatorTB_Nymph.command.dlc.tool.ItemTool
import com.navigatorTB_Nymph.command.dlc.tool.RoleTool
import com.navigatorTB_Nymph.dataEnum.ConfirmStatus
import com.navigatorTB_Nymph.dataEnum.ConfirmStatus.*
import com.navigatorTB_Nymph.dataEnum.Operation
import com.navigatorTB_Nymph.dataEnum.Operation.*
import com.navigatorTB_Nymph.pluginConfig.MirrorWorldConfig
import com.navigatorTB_Nymph.pluginData.Alchemy
import com.navigatorTB_Nymph.pluginData.FleaMarket.viewShelf
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import java.time.LocalDateTime
import kotlin.math.*

class GameMain(private val messageObject: CommandSender) {
    /** 用户对象 */
    private val user = messageObject.user!!
    private val uid = user.id

    /** 用户确认(三次) */
    private suspend fun yesOrNo(theme: String, no: String): Boolean {
        messageObject.sendMessage("[30秒/3次]$theme<是/否>")
        var i = 0
        val judge = withTimeoutOrNull(30_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, ConfirmStatus> {
                if (it.sender == messageObject.user) {
                    i++
                    when (it.message.content) {
                        "是" -> Affirm
                        "否" -> Negative
                        else -> if (i > 3) Default else {
                            messageObject.sendMessage("[30秒/${2 - i}次]$theme<是/否>")
                            null
                        }
                    }
                } else null
            }
        }
        when (judge) {
            null -> messageObject.sendMessage("输入超时,$no")
            Default -> messageObject.sendMessage("次数用尽,操作取消")
            Negative -> messageObject.sendMessage(no)
            Affirm -> return false
        }
        return true
    }

    /** 用户确认(一次) */
    private suspend fun yesOrNo(message: Message, no: Message, sender: User = user): Boolean {
        messageObject.sendMessage(message)

        val judge = withTimeoutOrNull(30_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Boolean> {
                if (it.sender == sender) it.message.content == "是" else null
            }
        }
        when (judge) {
            null -> messageObject.sendMessage("输入超时,$no")
            true -> return false
            false -> messageObject.sendMessage(no)
        }
        return true
    }

    /** 用户数据汇报 */
    fun gamerInfo() = MirrorWorldUser.outInfo(uid)

    /** 玩家角色建立 */
    suspend fun characterCreation() {
        val userData = MirrorWorldUser.userRole[uid]
        if (userData != null) {
            if (yesOrNo("创建新角色？\n清除存档,不可恢复", "角色建立取消")) return
        }
        messageObject.sendMessage(
            """
        角色已建立,开始分配属性点
        现在拥有 22 点属性点可分配至
        力量:与物攻相关
        法力:与法术相关
        智力:与经验获取,战斗效率相关
        体质:与生命值和法力值相关
        速度:与行动速度相关
        运气:与事件判定相关
        
        [120秒]请按照以下顺序输入点数分配方案(各项之间使用冒号分割,每项最多分配7点)：
        力量:法力:智力:体质:速度:运气
        """.trimIndent()
        )
        // 点数分配
        val plan = withTimeoutOrNull(120_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Array<Int>> {
                if (it.sender == messageObject.user) {
                    it.message.content.replace("：", ":").let { str ->
                        if (Regex("^([1-7]:){5}\\d\$").matches(str)) {
                            Array(6) { index -> str.split(":")[index].toInt() }.let { ary ->
                                if (ary.sum() > 22) {
                                    messageObject.sendMessage("[120秒]该方案所用点数超出可用点数总量,请重新输入")
                                    null
                                } else ary
                            }
                        } else {
                            messageObject.sendMessage("[120秒]参数格式不正确,请重新输入")
                            null
                        }
                    }
                } else null
            }
        }
        if (plan.isNullOrEmpty()) {
            messageObject.sendMessage("输入超时,角色建立取消")
            return
        }

        // 输出汇报
        messageObject.sendMessage("方案有效,生成角色属性预览如下\n${RoleTool(plan).show()}")
        // 用户确定
        if (yesOrNo(
                "确定当前方案？（剩余未分配的属性点将会以1属性点:${MirrorWorldConfig.ExchangeRate}金币的比例自动转化为金币值）\n确定属性后,不可再次更改",
                "放弃方案,角色建立取消"
            )
        ) return

        //选择特质及职业
        var sp = 6
        messageObject.sendMessage(
            """
            [30秒]点数设定完成,请进行特性/职业选择
            现拥有 $sp SP
            请选择特性(输入序号:单选)
            1:奴隶出身(+1SP)
            2:平民出身(-1SP)
            3:商贾世家(-2SP)
            4:皇室出身(-3SP)
            """.trimIndent()
        )
        val kill1 = withTimeoutOrNull(30_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Int> {
                if (it.sender == messageObject.user) {
                    when (val v = it.message.content.toIntOrNull()) {
                        in 1..4 -> v
                        else -> {
                            messageObject.sendMessage("[30秒]参数错误,请重新选择特性(输入序号:单选)")
                            null
                        }
                    }
                } else null
            }
        }
        when (kill1) {
            1 -> sp += 1
            2 -> sp -= 1
            3 -> sp -= 2
            4 -> sp -= 3
            null -> {
                messageObject.sendMessage("输入超时,角色建立取消")
                return
            }
        }

        messageObject.sendMessage(
            """
            [30秒]剩余 $sp SP
            请选择特性(输入序号:单选)
            1:恶名远扬(+1SP)
            2:默默无闻(-1SP)
            3:小有名气(-2SP)
            4:声名远扬(-3SP)
            """.trimIndent()
        )
        val kill2 = withTimeoutOrNull(30_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Int> {
                if (it.sender == messageObject.user) {
                    when (val v = it.message.content.toIntOrNull()) {
                        in 1..4 -> v
                        else -> {
                            messageObject.sendMessage("[30秒]参数错误,请重新选择特性(输入序号:单选)")
                            null
                        }
                    }
                } else null
            }
        }
        when (kill2) {
            1 -> sp += 1
            2 -> sp -= 1
            3 -> sp -= 2
            4 -> sp -= 3
            null -> {
                messageObject.sendMessage("输入超时,角色建立取消")
                return
            }
        }

        val kill3 = if (sp >= 2) {
            messageObject.sendMessage(
                """
                [30秒]现拥有 $sp SP
                请选择职业(可选)
                1:骑士(-2SP)
                2:猎手(-2SP)
                3:牧师(-2SP)
                4:法师(-2SP)
                [除选项外任意输入放弃职业选择]
                """.trimIndent()
            )

            withTimeoutOrNull(30_000) {
                GlobalEventChannel.syncFromEvent<MessageEvent, Int> {
                    if (it.sender == messageObject.user) it.message.content.toIntOrNull() ?: 0 else null
                }
            }
        } else 0
        if (kill3 == null) messageObject.sendMessage("输入超时,放弃职业选择")

        val role = when (kill3) {
            1 -> Knight(user.nick, plan[0] + 1, plan[1] + 1, plan[2] + 1, plan[3] + 1, plan[4] + 1, plan[5] + 1)
            2 -> Hunter(user.nick, plan[0] + 1, plan[1] + 1, plan[2] + 1, plan[3] + 1, plan[4] + 1, plan[5] + 1)
            3 -> Priest(user.nick, plan[0] + 1, plan[1] + 1, plan[2] + 1, plan[3] + 1, plan[4] + 1, plan[5] + 1)
            4 -> Wizard(user.nick, plan[0] + 1, plan[1] + 1, plan[2] + 1, plan[3] + 1, plan[4] + 1, plan[5] + 1)
            else -> Unemployed(user.nick, plan[0] + 1, plan[1] + 1, plan[2] + 1, plan[3] + 1, plan[4] + 1, plan[5] + 1)
        }

        role.newRole(arrayOf(kill1!!, kill2!!, if (role is Unemployed) sp else sp - 2))
        role.giveGold((22 - plan.sum()) * 10)
        role.updateCAV()
        MirrorWorldUser.userRole[uid] = role
        messageObject.sendMessage("角色创建全部完成")
    }

    /** PvP */
    suspend fun pvp(blue: User) {
        val userRole = MirrorWorldUser.userRole[uid]
        if (userRole == null) {
            messageObject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }
        val blueRole = MirrorWorldUser.userRole[blue.id]
        if (blueRole == null) {
            messageObject.sendMessage("对方角色未创建完成，请等待其建立角色后操作")
            return
        }

        if (userRole.pvpUA.not()) {
            messageObject.sendMessage(
                """
            PvP挑战须知:
            挑战依据战败惩罚分为[练习][切磋][死斗][自定义]四类
            [练习]
            1、生命值降至0即判负
            2、对战结束后双方状态将恢复至对战前状态
            3、开启练习对战将会消耗双方金币各10枚
            4、练习场经验收益为正常的10%
            [切磋]
            1、生命值降至总生命值40%以下即判负
            2、对战结束后双方状态将会保留
            3、胜方获得败方10%的金币(加成前)
            [死斗]
            1、生命值降至0即判负
            2、败方将会进行角色清算（结算分数,删除角色）
            3、胜方获得败方25%的金币(加成前)
            4、开启死斗对战需双方同意
            [自定义]
            1、判负标准可自定
            2、无任何奖励
            3、开启自定义对战将会消耗双方金币各20枚
            """.trimIndent()
            )
            if (yesOrNo("已知上述情况？", "拒绝")) return
            userRole.pvpUA = true
        }
        messageObject.sendMessage("[120秒/1次]请选择模式\n1:练习\n2:切磋\n3:死斗\n4:自定义")
        val mods = withTimeoutOrNull(120_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Int> {
                if (it.sender == messageObject.user) {
                    it.message.content.toIntOrNull() ?: 0
                } else null
            }
        }
        if (mods == null) {
            messageObject.sendMessage("输入超时,对战取消")
            return
        }

        val logID = when (mods) {
            1 -> {
                if (userRole.showGold() < 10 || blueRole.showGold() < 10) {
                    messageObject.sendMessage("金币余额不足,创建对战失败")
                    return
                }
                drillBattleSequence(userRole, blueRole)
            }
            2 -> {
                if (userRole.lv >= blueRole.lv && yesOrNo(At(blue) + "是否接受对战", PlainText("对战被拒绝"), blue)) return
                battleSequence(userRole, blueRole)
            }
            3 -> {
                if (yesOrNo(At(blue) + "是否接受对战", PlainText("对战被拒绝"), blue)) return
                battleSequence(userRole, uid, blueRole, blue.id)
            }
            4 -> {
                if (userRole.showGold() < 20 || blueRole.showGold() < 20) {
                    messageObject.sendMessage("金币余额不足,创建对战失败")
                    return
                }
                val bl = getBoundaryLine() ?: return
                battleSequence(userRole, blueRole, bl)
            }
            else -> {
                messageObject.sendMessage("未选择模式,对战取消")
                return
            }
        }
        messageObject.sendMessage(BattleRecordTool().readResults(logID))
        if (yesOrNo("是否输出战斗记录？", "战斗记录放弃输出")) return
        messageObject.sendMessage(BattleRecordTool().read(logID))
    }

    /** 转账 */
    suspend fun transfer(blueUid: Long, amount: Int) {
        val userRole = MirrorWorldUser.userRole[uid]
        if (userRole == null) {
            messageObject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }
        val blueRole = MirrorWorldUser.userRole[blueUid]
        if (blueRole == null) {
            messageObject.sendMessage("对方角色未创建完成，请等待其建立角色后操作")
            return
        }
        val v = (amount * 1.01 + 1).roundToInt()
        messageObject.sendMessage(
            if (userRole.transfer(
                    blueRole,
                    v
                )
            ) "转账成功,本次操作收取手续费${v - amount}枚" else "账户金额不足,转账失败"
        )
        blueRole.loseGold(v - amount)
    }

    /** 收益 */
    fun pay(gold: Int, cude: Int = 0): String? {
        val userRole = MirrorWorldUser.userRole[uid] ?: return null
        val userData = MirrorWorldUser.userData.getOrPut(uid) { PermanentData() }
        val today = LocalDateTime.now().dayOfYear
        return if (today != userData.signIn) {
            userData.signIn = today
            userRole.getCude(cude)
            "获得${userRole.getPaid(gold)}枚金币,${cude}个魔方"
        } else "你今天已经签到过了"
    }

    /** 增加账户金额 */
    fun toBestow(objID: Long, amount: Int) {
        MirrorWorldUser.userRole[objID]?.giveGold(amount)
    }

    /** 剥夺账户金额 */
    fun strip(objID: Long, amount: Int) {
        MirrorWorldUser.userRole[objID]?.snatch(amount)
    }

    /** 休息治疗 */
    fun treatment() = MirrorWorldUser.userRole[uid]?.treatment()

    /** 自定义场获取判定线数据 */
    private suspend fun getBoundaryLine(): Double? {
        messageObject.sendMessage("[120秒/1次]请设定战败判定线(范围:0 ~ 0.9)")
        val boundaryLine = withTimeoutOrNull(120_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Double> {
                if (it.sender == messageObject.user) it.message.content.toDoubleOrNull() ?: 1.0 else null
            }
        }
        when (boundaryLine) {
            null -> messageObject.sendMessage("输入超时,对战取消")
            in 0.0..0.9 -> return boundaryLine
            else -> messageObject.sendMessage("输入非法,对战取消")
        }

        return null
    }

    /** 练习场 */
    private fun drillBattleSequence(red: GameRole, blue: GameRole): String {
        // 支付费用
        red.loseGold(10)
        blue.loseGold(10)
        // 备份属性
        val redR = red.backup()
        val blueR = blue.backup()
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + System.currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.0, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(red.name, blue.name),
                            red.expSettlement(blue, 0.1),
                            Pair(0, 0)
                        )
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.0, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(blue.name, red.name),
                            blue.expSettlement(red, 0.1),
                            Pair(0, 0)
                        )
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        // 恢复属性
        red.recover(redR)
        blue.recover(blueR)
        return logID
    }

    /** 切磋场 */
    private fun battleSequence(red: GameRole, blue: GameRole): String {
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + System.currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.4, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(red.name, blue.name),
                            red.expSettlement(blue, 1.0),
                            red.goldSettlement(blue, 0.1)
                        )
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.4, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(blue.name, red.name),
                            blue.expSettlement(red, 1.0),
                            blue.goldSettlement(red, 0.1)
                        )
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }

    /** 死斗场 */
    private fun battleSequence(red: GameRole, redID: Long, blue: GameRole, blueID: Long): String {
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + System.currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.0, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(red.name, blue.name),
                            red.expSettlement(blue, 1.0),
                            red.goldSettlement(blue, 0.25)
                        )
                        MirrorWorldUser.userData.getOrPut(blueID) { PermanentData() }.pt += blue.valuation()
                        MirrorWorldUser.userRole.remove(blueID)
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.0, logID).not()) {
                        BattleRecordTool().write(
                            logID,
                            Pair(blue.name, red.name),
                            blue.expSettlement(red, 1.0),
                            blue.goldSettlement(red, 0.25)
                        )
                        MirrorWorldUser.userData.getOrPut(redID) { PermanentData() }.pt += red.valuation()
                        MirrorWorldUser.userRole.remove(redID)
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }

    /** 自定义场 */
    private fun battleSequence(red: GameRole, blue: GameRole, boundaryLine: Double): String {
        // 支付场地费
        red.loseGold(20)
        red.loseGold(20)
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + System.currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {      // A 进攻 B
                    redRounds++
                    if (red.executeAction(blue, boundaryLine, logID).not()) {
                        BattleRecordTool().write(logID, Pair(blue.name, red.name), Pair(0, 0), Pair(0, 0))
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {      // B 进攻 A
                    blueRounds++
                    if (blue.executeAction(red, boundaryLine, logID).not()) {
                        BattleRecordTool().write(logID, Pair(blue.name, red.name), Pair(0, 0), Pair(0, 0))
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }

    /** 查看货架 */
    fun enterStore(): String = viewShelf().ifBlank { "商店尚无物品" }

    /** 查看背包 */
    fun openBag(): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        return userRole.showBag()
    }

    /** 采购物品 */
    fun buy(productName: String, demand: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        return Shop(userRole).buy(productName, demand)
    }

    /** 挂售物品 */
    fun sell(productName: String, unitPrice: Int, demand: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        return Shop(userRole).sell(productName, uid, unitPrice, demand)
    }

    /** PvE */
    suspend fun pve() { // TODO: 2021/12/14 副本选择 => 随机怪物
        val userRole = MirrorWorldUser.userRole[uid]
        if (userRole == null) {
            messageObject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }
        messageObject.sendMessage("设计上这里开始选择副本,假设已经打过了,获得掉落物")
        if (userRole.receiveItem(ItemTool.randomCommonThings(), (1..3).random()))
            messageObject.sendMessage("背包已满,无法获得新物品")
    }

    fun alchemyGuide(): String = "物质属性指南:\n" +
            "以太(殁) < 炎椒果 < 影甲虫壳 <\n" +
            "殁性药水 <\n" +
            "殁露菇 < 地精菇 < 血红石 < 硼辉石 < 巫毒菌 <" +
            "冰晶蓝宝石 < 云晶 < 芒草 < 火凰晶 < 疣腕章鱼 <\n" +
            "炎熔根果 < 硫铜矿 < 旭日菊 < 血棘藤 < 星芯藤 <\n" +
            "紫苏云母 < 风铃花 < 阳斑菇 < 水银藤 < 硫磺泉水 <\n" +
            "元素基质 < 殊剂 <\n" +
            "蓝色风信子 < 雷蓟花 < 磷灵草 < 奇异菇 < 黑曜石 <\n" +
            "毒沼菇 < 希灵水晶 < 雷蝎毒液 < 冰霜果 < 暗影棘条 <\n" +
            "氨香树菇 < 蓝枫树果 < 金棘藤 < 墓穴鹅膏菌 < 缠枝藤 <\n" +
            "生性药水 <\n" +
            "月见花 < 黄铁矿 < 以太(生)"

    fun lightPool(count: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        return userRole.putUp(true, count)
    }

    fun heavyPool(count: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        return userRole.putUp(false, count)
    }

    /** 炼金术 */
    suspend fun alchemy() {
        val userRole = MirrorWorldUser.userRole[uid]
        if (userRole == null) {
            messageObject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }

        val itemList = ArrayList<String>()
        val operationList = ArrayList<Operation>()
        // 获取基材
        messageObject.sendMessage("现拥有以下物品,请选择其中一项作为炼金基材:\n" + userRole.showBag())

        for (i in 0..3) {
            // 获取材料
            obtainMaterials(userRole)?.let { itemList.add(it) } ?: return
            // 处理基材
            messageObject.sendMessage(
                """
            选择处理材料方式(输入序号):
            1、贫化    2、蒸馏    3、加热
            4、冷冻    5、反相    6、活化
            7、过滤    8、搅拌    9、收集产物
            0、什么也不做
            """.trimIndent()
            )
            val operate = handlingMaterials() ?: return
            operationList.add(operate)
            if (operate == CollectProduct) break
            // 获取配料
            if (i <= 2) messageObject.sendMessage("请选择其中一项作为炼金辅料")
        }
        var productValue = 0.0
        for ((index, materials) in itemList.withIndex()) {
            productValue += ItemTool.find(materials)?.itemID ?: return error()
            productValue = when (operationList[index]) {
                DoNothing -> productValue
                Dilution -> productValue / 2
                Distillation -> productValue * 2
                Heating -> if (productValue < 0) -productValue * productValue else productValue * productValue
                Freezing -> if (productValue < 0) -sqrt(0 - productValue) else sqrt(productValue)
                ReversedPhase -> productValue / -3
                Activation -> productValue.absoluteValue
                Filter -> floor(productValue)
                Stir -> ceil(productValue)
                CollectProduct -> break
            }
        }
        itemList.forEach { userRole.consumeItem(it) }
        val product = ItemTool.find(productValue.toInt())
        val message = "炼金完成," + if (product is Cube) {
            userRole.getCude(1)
            "你获得了一枚心智魔方"
        } else {
            if (userRole.receiveItem(product.itemName, 1)) "背包已满,无法获得新物品" else "你获得了一份${product.itemName}"
        }
        if (yesOrNo(PlainText("$message,是否保存配方？"), PlainText("放弃配方保存"))) return
        saveFormula(itemList, product)
        messageObject.sendMessage("配方保存完成")
    }

    private suspend fun error() {
        messageObject.sendMessage("程序异常执行,请截图上下文发予开发者")
    }

    /** 使用物品 */
    fun useItems(itemName: String, s: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        val num = userRole.findBagItemAmount(itemName)
        if (num == -1) return "你没有这个东西"
        if (num < s) return "无法使用${s}个$itemName,因为你只有${num}个"
        val t = ItemTool.find(itemName)?.useItem(userRole, s) ?: return ""
        if (userRole.hp.current == 0) {
            MirrorWorldUser.userData.getOrPut(uid) { PermanentData() }.pt += userRole.valuation()
            MirrorWorldUser.userRole.remove(uid)
        }
        return t
    }

    /** 回收垃圾 */
    fun garbageCollection(s: Int): String {
        val userRole = MirrorWorldUser.userRole[uid] ?: return "你的角色未创建完成，请建立角色后操作"
        val num = userRole.findBagItemAmount("垃圾")
        if (num == -1) return "你没有这个东西"
        if (num < s) return "无法回收${s}个,因为你只有${num}个"
        userRole.consumeItems("垃圾", s)
        return "获得了${userRole.getPaid(s / 3)}枚金币"
    }

    /** 保存配方 */
    private fun saveFormula(itemList: ArrayList<String>, product: Item) {
        val formula = itemList.sorted().joinToString("-")
        val alchemy = Alchemy.formula.getOrPut(product.itemName) { mutableListOf() }
        val index = alchemy.indexOf(formula)
        val k = if (index == -1) {
            alchemy.add(formula)
            alchemy.size - 1
        } else index
        MirrorWorldUser.userData.getOrPut(uid) { PermanentData() }.formula.getOrPut(product.itemName) { mutableSetOf() }
            .add(k)
    }

    /** 炼金操作 */
    private suspend fun handlingMaterials(): Operation? {
        val judge = withTimeoutOrNull(120_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, Operation> {
                if (it.sender == messageObject.user)
                    when (it.message.content.toIntOrNull()) {
                        0 -> DoNothing
                        1 -> Dilution
                        2 -> Distillation
                        3 -> Heating
                        4 -> Freezing
                        5 -> ReversedPhase
                        6 -> Activation
                        7 -> Filter
                        8 -> Stir
                        9 -> CollectProduct
                        else -> {
                            messageObject.sendMessage("无此操作,请重新输入")
                            null
                        }
                    }
                else null
            }
        }
        if (judge == null) messageObject.sendMessage("输入超时,本次炼金取消")
        return judge
    }

    /** 获取原料 */
    private suspend fun obtainMaterials(userRole: GameRole): String? {
        val judge = withTimeoutOrNull(120_000) {
            GlobalEventChannel.syncFromEvent<MessageEvent, String> {
                if (it.sender == messageObject.user)
                    it.message.content.let { judge ->
                        if (userRole.findBagItemAmount(judge) == -1) {
                            messageObject.sendMessage("背包中无此物品,请重新输入")
                            null
                        } else judge
                    }
                else null
            }
        }
        if (judge == null) messageObject.sendMessage("输入超时,本次炼金取消")
        return judge
    }
}

/*
 * 我们周围的所有物质都是由一些基本元素组成的.
 * 通过研究这些元素及其嬗变过程,炼金术士得以了解宇宙运行的奥秘
 * 炼金术是自然科学,是基于证据的严谨学科,由千百年的精心实验和理论研究发展而来
 * 其研究物质的组成,性质,结构与变化规律,以创造新物质
 * 炼金术是最高的艺术,其从业人员指引着人类的进程
 * 作为沟通世界物质的重要桥梁,炼金术是人类认识和改造物质世界的主要方法和手段
 * 古往今来,炼金术士指引了社会的发展,辅佐了一代代的君王,塑造了他们周围的世界.
 */