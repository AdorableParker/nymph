package com.nymph_TB_DLC

import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.nymph_TB_DLC.CharacterLineDictionary as CLD

/**角色对象 */
@Serializable
sealed class GameRole {
    abstract val name: String                               // 名字,使用昵称
    private var gold: Int = 0                               //金币

    var lv: Int = 1                                         //等级
    private var userExp: Int = 0                            //经验
    var pvpUA: Boolean = false                              // 用户协议
    //六维
    /**力量: 1..8*/
    var natureStr = 1

    /**法力: 1..8*/
    var natureMen = 1

    /**智力: 1..8*/
    var natureInt = 1

    /**体质: 1..8*/
    var natureVit = 1

    /**速度: 1..8*/
    var natureAgi = 1

    /**运气: 1..8*/
    var natureLck = 1

    var hp = Bar(0)         // 生命值
    var mp = Bar(0)         // 法力值
    var atk = 0                  // 物理攻击
    var mat = 0                  // 法术攻击
    var tpa = 0                  // 行动速度

    abstract val professionHP: Double
    abstract val professionMP: Double
    abstract val professionATK: Double
    abstract val professionMAT: Double
    abstract val professionTPA: Double

    //    private var bag = arrayOfNulls<Int>(16)                    //物品
    private var traitsList: MutableSet<String> = mutableSetOf()     //特质
    private var skillList: MutableSet<String> = mutableSetOf()     //特质

    //    private var skillList: MutableSet<String> = mutableSetOf()      //技能
    abstract var skillPrint: Int                                           //技能点
    private var attributePrint: Int = 22                            //属性点

    fun showTPA() = tpa
    fun showGold() = gold

    fun info(): String {
        val buffer = StringBuilder()
        for ((i, element) in traitsList.withIndex()) {
            buffer.append(if (i % 2 == 1) "\t${element}" else "\n${element}")
        }
        return """$name
        |等级:$lv  金币:${gold}枚
        |HP:${hp.current}/${hp.max}  MP:${mp.current}/${mp.max}
        |ATK:$atk    MAT:${mat}
        |TPA:$tpa
        |经验:$userExp/${lv * lv}
        |闲置技能点:$skillPrint
        |拥有特质:$buffer
        |拥有技能:$buffer
        |------六维加点------
        |力量:$natureStr    法力:$natureMen
        |智力:$natureInt    体质:$natureVit
        |速度:$natureAgi    运气:$natureLck""".trimIndent()
    }

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

    fun judge(goal: Int) = (0..100).random() + natureLck - 4 + 5 / (2 + natureLck) >= (100 - goal)

    private fun addExp(getExp: Int) {
        userExp += getExp
        while (userExp >= lv * lv) {
            userExp -= lv * lv
            upLv()
        }
    }

    private fun upLv() {
        lv++
        updateCAV()
    }

    //设置六围
    fun set6D(plan: Array<Int>) {
        attributePrint = 0
        natureStr += plan[0]
        natureMen += plan[1]
        natureInt += plan[2]
        natureVit += plan[3]
        natureAgi += plan[4]
        natureLck += plan[5]
        updateCAV()
    }

    /**清算角色*/
    fun valuation(): Int = (gold * 0.1 + lv * lv / 60).toInt()

    /**还原属性*/
    fun recover(archive: Triple<Int, Int, Int>) {
        if (lv <= archive.third) {
            hp.current = archive.first
            mp.current = archive.second
        }
    }

    /**练习场备份属性*/
    fun backup() = Triple(hp.current, mp.current, lv)

    fun giveGold(v: Int) {
        gold += v
    }

    fun loseGold(v: Int) = if (v <= gold) {
        gold -= v
        true
    } else false

    /** 移除 */
    fun snatch(v: Int) = if (v <= gold) gold -= v else gold = 0


    fun transfer(joe: GameRole, amount: Int) = if (gold <= amount) false
    else {
        gold -= amount
        joe.gold += amount
        true
    }

    fun show(): String {
        return """
        等级: 1
        HP: ${hp.max}	MP: ${mp.max}
        ATK: $atk	MAT: $mat
        TPA: $tpa
        ------六维加点------
        力量:$natureStr	法力:$natureMen
        智力:$natureInt	体质:$natureVit
        速度:$natureAgi	运气:$natureLck
        """.trimIndent()
    }

    /**刷新属性*/
    private fun updateCAV() {
        hp = Bar(((9 * natureVit + natureStr + 45) / 8.0 * 5.4 * professionHP + sqrt(lv * 235.7) * 5).roundToInt())
        mp = Bar(((9 * natureMen + natureVit + 45) / 8.0 * 7 * professionMP + sqrt(lv * 75.3) * 5).roundToInt())
        atk = (sqrt(natureStr / 8.0 * 314 * professionATK * lv) * 1.5).roundToInt()
        mat = (sqrt(natureMen / 8.0 * 314 * professionMAT * lv) * 1.5).roundToInt()
        tpa = (sqrt(31.4 * lv) / (4.13 * natureAgi) * professionTPA + 10 - natureAgi).roundToInt()
    }

    /**通用攻击能力*/
    open fun attack(foe: GameRole, logID: String): Pair<Double, Double> {
        BattleRecord().write(logID, "$name${CLD.AttackLine.random()}")
        val magicDif = (natureMen - foe.natureMen) * MirrorWorldConfig.AttackModifier + 1
        val intelligenceDif = (natureInt - foe.natureInt) * MirrorWorldConfig.AttackModifier + 1
        return Pair(atk * magicDif * intelligenceDif, 0.0)
    }

    /**通用防御能力*/
    open fun defense(damage: Pair<Double, Double>, logID: String): Int {
        val d = (damage.first + damage.second).roundToInt() + (8 - natureAgi..natureAgi).random()
        return if (skillList.contains("[皇室荣光]")) {
            if (d <= lv * 2) {
                BattleRecord().write(logID, "${name}触发技能[皇室荣光],护盾吸收了所有的伤害")
                0
            } else {
                val k = d - lv * 2
                BattleRecord().write(logID, "${name}触发技能[皇室荣光],护盾吸收了${lv * 2}点伤害,最终受到了${k}点伤害")
                k
            }
        } else {
            BattleRecord().write(logID, "$name${CLD.DefenseLine.random()},受到了${d}点伤害")
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

    /** 经验结算
     * @param[foe]战败对象
     * @param[buff]经验系数
     * */
    fun expSettlement(foe: GameRole, buff: Double): Pair<Int, Int> {
        // 可获得经验基数
        val base = ((foe.lv - lv) * (foe.lv + lv) * 0.2).absoluteValue + 1
        // 计算所得
        val winnerExp = (base * (natureInt * 0.01 + 1) * getTraits("Exp", true) * buff).toInt()
        val loserExp = (base * (foe.natureInt * 0.01 + 1) * foe.getTraits("Exp", true) * buff).toInt()
        // 给予所得经验
        addExp(winnerExp)
        foe.addExp(loserExp)
        return Pair(winnerExp, loserExp)
    }

    /** 金币结算
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

    fun getPaid(salary: Int): Int {
        val income = (getTraits("Gold", true) * salary).toInt()
        giveGold(income)
        return income
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

    fun treatment(): String {
        val medicalExpenses = (getTraits("Gold", false) * (hp.max - hp.current) / 20).toInt()
        return if (loseGold(medicalExpenses)) {
            hp.full()
            "本次治疗花费${medicalExpenses}枚金币,欢迎下次再来"
        } else "你没有足够的钱进行治疗"
    }
}


