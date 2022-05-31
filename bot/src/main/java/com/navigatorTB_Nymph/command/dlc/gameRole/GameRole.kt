package com.navigatorTB_Nymph.command.dlc.gameRole

import com.navigatorTB_Nymph.command.dlc.data.Bar
import com.navigatorTB_Nymph.command.dlc.tool.BattleRecordTool
import com.navigatorTB_Nymph.command.dlc.tool.ItemTool
import com.navigatorTB_Nymph.pluginConfig.MirrorWorldConfig
import com.navigatorTB_Nymph.pluginData.MirrorWorldAssets
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.navigatorTB_Nymph.pluginConfig.CharacterLineDictionary as CLD

/**角色对象 */
@Serializable
sealed class GameRole {
    abstract val name: String                               // 名字,使用昵称
    abstract val professionHP: Double
    abstract val professionMP: Double
    abstract val professionATK: Double
    abstract val professionMAT: Double
    abstract val professionTPA: Double
    abstract val natureStr: Int                                              // 力量: 1..8
    abstract val natureMen: Int                                              // 法力: 1..8
    abstract val natureInt: Int                                              // 智力: 1..8
    abstract val natureVit: Int                                              // 体质: 1..8
    abstract val natureAgi: Int                                              // 速度: 1..8
    abstract val natureLck: Int                                              // 运气: 1..8
    abstract val profession: String

    val skillList: MutableSet<String> = mutableSetOf()                      //特质
    var lv: Int = 1                                                         // 等级
    var pvpUA: Boolean = false                                              // 用户协议
    var hp = Bar(0)                                                    // 生命值
    var mp = Bar(0)                                                    // 法力值
    var atk = 0                                                             // 物理攻击
    var mat = 0                                                             // 法术攻击
    var tpa = 0                                                             // 行动速度
    private var skillPrint = 0                                            // 技能点

    private val bag: MutableMap<String, Int> = mutableMapOf()               //物品
    private var gold: Int = 0                                               //金币
    private var cube: Int = 0                                               //魔方数量
    private var userExp: Int = 0                                            //经验
    private val traitsList: MutableSet<String> = mutableSetOf()             //特质
    //    private var skillList: MutableSet<String> = mutableSetOf()        //技能

    fun showTPA() = tpa
    fun showGold() = gold
    fun showBag(): String {
        val bagC = arrayListOf<String>()
        val str = StringBuffer()
        bag.forEach { (itemName, num) ->
            if (num == 0) bagC.add(itemName)
            else {
                val item = ItemTool.find(itemName)
                str.append("$itemName - ${num}个\n\t——${item?.itemInfo}\n")
            }
        }
        if (bagC.isNotEmpty()) bagC.forEach { bag.remove(it) }
        return str.toString().ifBlank { "你的背包是空的" }
    }

    fun getCude(amount: Int) {
        cube += amount
    }

    fun putUp(flag: Boolean, count: Int): String {
        val (cubeValue, goalValue) = if (flag) Pair(1 * count, 50 * count) else Pair(2 * count, 150 * count)
        return if (cube >= cubeValue && gold >= goalValue) {
            cube -= cubeValue
            gold -= goalValue
            ""
        } else "魔方或物资不足"
    }

    /** 消耗一个物品
     * @param[itemName]物品名
     */
    fun consumeItem(itemName: String) {
        val item = bag[itemName] ?: return
        if (item == 1) bag.remove(itemName)
        else bag[itemName] = item - 1
    }

    /** 消耗物品
     * @param[itemName]物品名
     * @param[v]消耗数量
     * @return 消耗成功则返回 true 否则返回 false
     */
    fun consumeItems(itemName: String, v: Int): Boolean {
        val item = bag[itemName] ?: return false
        when {
            item < v -> return false
            item == v -> bag.remove(itemName)
            else -> bag[itemName] = item - v
        }
        return true
    }

    /** 获得物品
     * @param[itemName]物品名
     * @param[v]获得量
     */
    fun receiveItem(itemName: String, v: Int): Boolean {
        val have = bag.getOrDefault(itemName, 0)
        if (have <= 0 && bag.size >= 16) return true
        bag[itemName] = have + v
        return false
    }

    fun checkBackpackSpace(itemName: String): Boolean {
        if (bag.size >= 16 && bag.containsKey(itemName).not()) return true
        return false
    }

    /** 获取背包内物品数量
     * @param[itemName]物品名
     * @return 物品存在则返回拥有量,否则返回 -1*/
    fun findBagItemAmount(itemName: String): Int {
        return bag.getOrDefault(itemName, -1)
    }

    /** 获取角色信息 */
    fun info(): String {
        val buffer = StringBuilder()
        for ((i, element) in traitsList.withIndex()) {
            buffer.append(if (i % 2 == 1) "\t${element}" else "\n${element}")
        }
        return """$name[$profession]
        |等级:$lv  金币:${gold}枚
        |HP:${hp.current}/${hp.max}  MP:${mp.current}/${mp.max}
        |ATK:$atk    MAT:$mat    TPA:$tpa
        |魔方:${cube}个
        |经验:$userExp/${lv * lv}
        |闲置技能点:$skillPrint
        |拥有特质:$buffer
        |拥有技能:$buffer
        |------六维加点------
        |力量:$natureStr    法力:$natureMen
        |智力:$natureInt    体质:$natureVit
        |速度:$natureAgi    运气:$natureLck""".trimIndent()
    }

    /** 初始化角色数据 */
    fun newRole(killList: Array<Int>) {
        when (killList[0]) {
            1 -> traitsList.add("-被剥削者-")
            3 -> traitsList.add("-精打细算-")
            4 -> skillList.add("[皇室荣光]")
        }
        when (killList[1]) {
            1 -> traitsList.add("-被通缉者-")
            3 -> traitsList.add("-名人效应-")
            4 -> traitsList.add("-被传颂者-")
        }
        skillPrint = killList[2]
    }

    /** 判定
     * @param[goal]目标值
     * @return 返回判定结果,成功为 true 失败为 false
     */
    fun judge(goal: Int) = (0..100).random() + natureLck - 4 + 5 / (2 + natureLck) >= (100 - goal)

    /** 增加经验
     * @param[getExp]获得的经验
     * */
    private fun addExp(getExp: Int) {
        userExp += getExp
        while (userExp >= lv * lv) {
            userExp -= lv * lv
            upLv()
        }
    }

    /** 升级 */
    private fun upLv() {
        lv++
        updateCAV()
    }

    /**清算角色
     * @return 返回当前角色得分
     */
    fun valuation(): Int = (gold * 0.1 + lv * lv / 60).toInt()

    /** 还原属性 */
    fun recover(archive: Triple<Int, Int, Int>) {
        if (lv <= archive.third) {
            hp.current = archive.first
            mp.current = archive.second
        }
    }

    /**练习场备份属性*/
    fun backup() = Triple(hp.current, mp.current, lv)

    /** 金币直接加 */
    fun giveGold(v: Int) {
        gold += v
    }

    /** 金币判断减 */
    fun loseGold(v: Int) = if (v <= gold) {
        gold -= v
        true
    } else false

    /** 金币强制减 */
    fun snatch(v: Int) = if (v <= gold) gold -= v else gold = 0

    /** 无加成转账 */
    fun transfer(joe: GameRole, amount: Int): Boolean {
        return if (gold <= amount) false
        else {
            gold -= amount
            joe.gold += amount
            true
        }
    }

    /**刷新属性*/
    fun updateCAV() {
        hp = Bar(((9 * natureVit + natureStr + 45) / 8.0 * 5.4 * professionHP + sqrt(lv * 235.7) * 5).roundToInt())
        mp = Bar(((9 * natureMen + natureVit + 45) / 8.0 * 7 * professionMP + sqrt(lv * 75.3) * 5).roundToInt())
        atk = (sqrt(natureStr / 8.0 * 314 * professionATK * lv) * 1.5).roundToInt()
        mat = (sqrt(natureMen / 8.0 * 314 * professionMAT * lv) * 1.5).roundToInt()
        tpa = (sqrt(31.4 * lv) / (4.13 * natureAgi) * professionTPA + 10 - natureAgi).roundToInt()
    }

    /**通用攻击能力*/
    open fun attack(foe: GameRole, logID: String): Pair<Double, Double> {
        BattleRecordTool().write(logID, "$name${CLD.AttackLine.random()}")
        val magicDif = (natureMen - foe.natureMen) * MirrorWorldConfig.AttackModifier + 1
        val intelligenceDif = (natureInt - foe.natureInt) * MirrorWorldConfig.AttackModifier + 1
        return Pair(atk * magicDif * intelligenceDif, 0.0)
    }

    /**通用防御能力*/
    open fun defense(damage: Pair<Double, Double>, logID: String): Int {
        val d = (damage.first + damage.second).roundToInt() + (natureAgi - 8..natureAgi).random()
        return if (skillList.contains("[皇室荣光]")) {
            if (d <= lv * 2) {
                BattleRecordTool().write(logID, "${name}触发技能[皇室荣光],护盾吸收了所有的伤害")
                0
            } else {
                val k = d - lv * 2
                BattleRecordTool().write(logID, "${name}触发技能[皇室荣光],护盾吸收了${lv * 2}点伤害,最终受到了${k}点伤害")
                k
            }
        } else {
            BattleRecordTool().write(logID, "$name${CLD.DefenseLine.random()},受到了${d}点伤害")
            d
        }
    }

    /**对战逻辑*/
    fun executeAction(joe: GameRole, boundaryLine: Double, logID: String): Boolean {
        val damage = joe.defense(attack(joe, logID), logID)
        return if (joe.hp.current - damage > joe.hp.max * boundaryLine) {
            joe.hp.harm(damage)
        } else {
            false
        }
    }

    /** 战斗 - 经验结算
     * @param[foe]战败对象
     * @param[buff]经验系数
     * */
    fun expSettlement(foe: GameRole, buff: Double): Pair<Int, Int> {
        // 可获得经验基数

        val base = (foe.lv + lv) * (foe.lv + lv) * (1 - lv / (foe.lv + lv)) / 6.0

        // 计算所得
        val winnerExp = (base * (natureInt * 0.01 + 1) * getTraits("Exp", true) * buff + 0.5).toInt()
        val loserExp = (base * (foe.natureInt * 0.01 + 0.2) * foe.getTraits("Exp", true) * buff + 0.5).toInt()
        // 给予所得经验
        val rExp = Pair(if (winnerExp <= 0) 1 else winnerExp, if (loserExp <= 0) 1 else loserExp)

        addExp(rExp.first)
        foe.addExp(rExp.second)
        return rExp
    }

    /** 战斗 - 金币结算
     * @param[foe]战败对象
     * @param[buff]金币系数
     * */
    fun goldSettlement(foe: GameRole, buff: Double): Pair<Int, Int> {
        // 可获得金币基数
        val base = foe.gold * buff
        // 计算所得
        val winnerGold = (getTraits("Gold", true) * base).toInt()
        val loserGold = (foe.getTraits("Gold", false) * base).toInt()
        // 给予所得金币
        giveGold(winnerGold)
        // 移除所罚金币
        foe.loseGold(loserGold)
        return Pair(winnerGold, loserGold)
    }

    /** 商业 - 赚取收益 */
    fun getPaid(salary: Int): Int {
        val income = (getTraits("Gold", true) * salary).toInt()
        giveGold(income)
        return income
    }

    /** 商业 - 支付金额 */
    fun payFine(salary: Int): Int {
        val income = (getTraits("Gold", false) * salary).toInt()
        return if (loseGold(income)) income else -1
    }

    /** 加成计算
     * @param[type]
     * 计算加成类型
     * @param[inOrEx]计算收入加成或是支出加成
     * * 收入: true
     * * 支出: false
     */
    private fun getTraits(type: String, inOrEx: Boolean): Double {
        var negative = 0.0     //负面加成修正计算
        var positive = 0.0     //正面加成修正计算

        if (inOrEx) {  //收入加成
            for ((name, traits) in MirrorWorldAssets.PositiveCorrection.first.getOrDefault(type, mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive += traits.first
                    negative -= traits.second
                }
            }
            for ((name, traits) in MirrorWorldAssets.PositiveCorrection.first.getOrDefault("bonus", mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive *= traits.first
                    negative *= traits.second
                }
            }
        } else {      //支出加成
            for ((name, traits) in MirrorWorldAssets.PositiveCorrection.second.getOrDefault(type, mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive += traits.first
                    negative -= traits.second
                }
            }
            for ((name, traits) in MirrorWorldAssets.PositiveCorrection.second.getOrDefault("bonus", mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive *= traits.first
                    negative *= traits.second
                }
            }
        }
        return negative + positive + 1
    }

    /** 休息治疗 */
    fun treatment(): String {
        val medicalExpenses = (getTraits("Gold", false) * (hp.max - hp.current) / 20).toInt()
        return if (loseGold(medicalExpenses)) {
            hp.full()
            "本次治疗花费${medicalExpenses}枚金币,欢迎下次再来"
        } else "你没有足够的钱进行治疗"
    }
}