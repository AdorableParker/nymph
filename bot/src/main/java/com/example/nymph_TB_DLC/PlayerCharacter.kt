package com.example.nymph_TB_DLC

import com.example.nymph_TB_DLC.MirrorWorldAssets.PositiveCorrection
import com.example.nymph_TB_DLC.MirrorWorldConfig.AttackModifier
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Serializable
data class PermanentData(
    var pt: Int = 0,
    var pc: PlayerCharacter? = null,
    var PvPUA: Boolean = false,
    var creationSteps: Int = 0
) {
    fun outInfo(): String {
        return """
        拥有Pt:   $pt
        ==========
        玩家角色:
        ${pc?.info() ?: "角色未建立"}
        """.trimIndent()
    }

    fun destruction() {
        if (pc?.showDestruction() == true) {
            pt = pc!!.valuation()
            pc = null
        }
    }
}


@Serializable
data class Bar(var Max: Int) {
    var current: Int = Max
    fun harm(v: Int, boundaryLine: Double): Boolean {
        return if (current >= v) {
            current -= v
            current > boundaryLine * Max
        } else {
            current = 0
            false
        }
    }

    fun treatment(v: Int) {
        current = if (current + v < Max) {
            current + v
        } else {
            Max
        }
    }
}

/**角色对象 */
@Serializable
class PlayerCharacter(val name: String) {
    private var gold: Int = 0                                       //金币，需要保留

    private var lv: Int = 1                                         //等级
    private var userExp: Int = 0                                    //经验

    //    private var bag = arrayOfNulls<Int>(16)                    //物品
    private var traitsList: MutableSet<String> = mutableSetOf()     //特质
    private var skillList: MutableSet<String> = mutableSetOf()      //技能
    private var skillPrint: Int = 6                                 //技能点
    private var attributePrint: Int = 22                            //属性点
    private var destruction: Boolean = false
    //六维
    /**力量: 1..8*/
    private var _str = 1

    /**法力: 1..8*/
    private var _men = 1

    /**智力: 1..8*/
    private var _int = 1

    /**体质: 1..8*/
    private var _vit = 1

    /**速度: 1..8*/
    private var _agi = 1

    /**运气: 1..8*/
    private var _lck = 1

    /**职业属性曲线
     *0:atk 1:mat 2:spd 3:hp 4:mp
     *<pre> 成长曲线
     * 职业  技能   atk   mat    spd     hp    mp
     * 骑士：[`招架`] 1.3   0.9    0.7    1.4   0.7 = 5
     * 猎手：[`闪避`] 0.9   0.8    1.3    1.2   0.8 = 5
     * 牧师：[`回复`] 1.1   1.1    0.9    0.9   1.0 = 5
     * 法师：[`附魔`] 0.8   1.3    0.9    0.8   1.2 = 5
     *</pre> */
    private var profession = arrayOf<Double>()


    private var hp = Bar(0)         // 生命值
    private var mp = Bar(0)         // 法力值
    private var atk = 0                  // 物理攻击
    private var mat = 0                  // 法术攻击
    private var tpa = 0                  // 行动速度

    fun info(): String {
        val buffer = StringBuilder()
        for ((i, element) in traitsList.withIndex()) {
            buffer.append(if (i % 2 == 1) "\t${element}" else "\n${element}")
        }
        return """
            等级:$lv  金币:${gold}枚
            HP:${hp.current}/${hp.Max}  MP:${mp.current}/${mp.Max}
            ATK:$atk    MAT:${mat}
            经验:$userExp/${lv * lv}
            闲置技能点:$skillPrint
            拥有特质:$buffer
            拥有技能:$buffer
            ------六维加点------
            力量:$_str    法力:$_men
            智力:$_int    体质:$_vit
            速度:$_agi    运气:$_lck
            """.trimIndent()
    }

    fun showAP() = attributePrint
    fun showSP() = skillPrint
    fun showTPA() = tpa
    fun showDestruction() = destruction

    fun newRole(origin: Int?, reputation: Int?, prof: Int?, traitsPrint: Int) {
        when (origin) {
            1 -> traitsList.add("-被剥削者-")
            3 -> {
                traitsList.add("-钞能力-")
                traitsList.add("-精打细算-")
            }
            4 -> {
                traitsList.add("-皇室荣光-")
                gold += 500
            }
        }
        when (reputation) {
            1 -> traitsList.add("-被通缉者-")
            3 -> traitsList.add("-名人效应-")
            4 -> traitsList.add("-被传颂者-")
        }
        profession = when (prof) {
            1 -> {      //骑士
                skillList.add("[招架]")
                arrayOf(1.3, 0.9, 0.7, 1.4, 0.7)
            }
            2 -> {      //猎手
                skillList.add("[闪避]")
                arrayOf(0.9, 0.8, 1.3, 1.2, 0.8)
            }
            3 -> {      //牧师
                skillList.add("[回复]")
                arrayOf(1.1, 1.1, 0.9, 0.9, 1.0)
            }
            4 -> {      //法师
                skillList.add("[附魔]")
                arrayOf(0.8, 1.3, 0.9, 0.8, 1.2)
            }
            else -> {   //无职者
                arrayOf(1.0, 1.0, 1.0, 1.0, 1.0)
            }
        }
        skillPrint = traitsPrint
        updateCAV()
    }

    private fun judge(goal: Int) = (0..100).random() + _lck - 4 + 5 / (2 + _lck) >= goal

    /**普通攻击计算
     * @return [闪避: -2, 判负: -1, 伤害值: Any]
     */
    fun attack(foe: PlayerCharacter, boundaryLine: Double): Int {
        //闪避能力最先
        if (foe.skillList.contains("[闪避]") && foe.judge(80)) return -2
        //闪避计算完成
        //初始化值
        var ad = atk.toDouble()
        var ap = 0.0
        val magicDif = (_men - foe._men) * AttackModifier + 1
        val strengthDif = (_str - foe._str) * AttackModifier + 1
        val intelligenceDif = (_int - foe._int) * AttackModifier + 1
        //攻击技能先算
        if (skillList.contains("[附魔]")) {
            ap = magicDif * intelligenceDif * ad * 0.5
            ad *= strengthDif * intelligenceDif * 0.5
        } else {
            ad *= magicDif * intelligenceDif
        }
        //防御技能后算
        if (foe.skillList.contains("[招架]") && judge(50)) {
            ad /= 7
        }
        val totalDamage = (ap + ad).roundToInt()
        val damage = if (foe.skillList.contains("[皇室荣光]")) {
            if (totalDamage - foe.lv * 2 > 0) totalDamage - foe.lv * 2 else 0
        } else totalDamage
        return if (foe.hp.harm(damage, boundaryLine)) damage else -1
    }

    /** 加成计算 */
    private fun getTraits(type: String, mods: Boolean): Double {
        var negative = 0.0     //负面加成修正计算
        var positive = 0.0     //正面加成修正计算

        if (mods) {  //收入加成
            for ((name, traits) in PositiveCorrection.first.getOrDefault(type, mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive += traits.first
                    negative -= traits.second
                }
            }
            for ((name, traits) in PositiveCorrection.first.getOrDefault("bonus", mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive *= traits.first
                    negative *= traits.second
                }
            }
        } else {      //支出加成
            for ((name, traits) in PositiveCorrection.second.getOrDefault(type, mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive += traits.first
                    negative -= traits.second
                }
            }
            for ((name, traits) in PositiveCorrection.second.getOrDefault("bonus", mutableMapOf())) {
                if (traitsList.contains(name)) {
                    positive *= traits.first
                    negative *= traits.second
                }
            }
        }
        return negative + positive + 1
    }

    fun addExp(getExp: Int) {
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

    /** 玩家战斗胜利结算*/
    fun settlement(foe: PlayerCharacter, mods: Int): Pair<String, String> {
        var expModBuff = 1.0
        var goalModBuff = 0.0
        val expBase = ((foe.lv - lv) * (foe.lv + lv) * 0.2).absoluteValue
        when (mods) {
            1 -> expModBuff = 0.2
            2 -> goalModBuff = 0.1
            3 -> goalModBuff = 0.25
        }
        // 计算所得
        val winnerExp = (expBase * (_int * 0.01 + 1) * getTraits("Exp", true) * expModBuff).toInt()
        val loserExp = (expBase * (foe._int * 0.01 + 1) * foe.getTraits("Exp", true) * expModBuff).toInt()
        val winnerGold = (getTraits("Gold", true) * foe.gold * goalModBuff).toInt()
        val loserGold = (foe.getTraits("Gold", false) * foe.gold * goalModBuff).toInt()
        // 给予所得
        addExp(winnerExp)
        addExp(loserExp)
        gold += winnerGold
        foe.gold -= loserGold
        if (mods == 3) foe.destruction = true
        return Pair("获得了 $winnerExp 点经验,$winnerGold 枚金币", "获得了 $winnerExp 点经验,失去了 $loserGold 枚金币")
    }

    //设置六围
    fun set6D(plan: Array<Int>) {
        attributePrint = 0
        _str += plan[0]
        _men += plan[1]
        _int += plan[2]
        _vit += plan[3]
        _agi += plan[4]
        _lck += plan[5]
    }

    /**设置属性*/
    fun updateCAV() {
        hp = Bar(((9 * _vit + _str + 45) / 8.0 * 5.4 * profession[3] + sqrt(lv * 235.7) * 5).roundToInt())
        mp = Bar(((9 * _men + _vit + 45) / 8.0 * 7 * profession[4] + sqrt(lv * 75.3) * 5).roundToInt())
        atk = (sqrt(_str / 8.0 * 314 * profession[0] * lv) * 1.5).roundToInt()
        mat = (sqrt(_men / 8.0 * 314 * profession[1] * lv) * 1.5).roundToInt()
        tpa = (sqrt(31.4 * lv) / (4.13 * _agi) * profession[2] + 10 - _agi).roundToInt()
    }

    fun giveGold(v: Int) {
        gold += v
    }

    fun backup() = Pair(hp, mp)

    fun recover(archive: Pair<Bar, Bar>) {
        hp = archive.first
        mp = archive.second
    }

    fun valuation(): Int = (gold * 0.1 + lv * lv / 60).toInt()
}

class Tool(sixD: Array<Int>) {
    private val _str = sixD[0] + 1  //力
    private val _men = sixD[1] + 1  //法
    private val _int = sixD[2] + 1  //智
    private val _vit = sixD[3] + 1  //体
    private val _agi = sixD[4] + 1  //敏
    private val _lck = sixD[5] + 1  //运

    fun draftHP(lv: Int = 1, profession: Double = 1.0) =
        ((9 * _vit + _str + 45) / 8.0 * 5.4 * profession + sqrt(lv * 235.7) * 5).roundToInt()

    fun draftMP(lv: Int = 1, profession: Double = 1.0) =
        ((9 * _men + _vit + 45) / 8.0 * 7 * profession + sqrt(lv * 75.3) * 5).roundToInt()

    fun draftATK(lv: Int = 1, profession: Double = 1.0) =
        (sqrt(_str / 8.0 * 314 * profession * lv) * 1.5).roundToInt()

    fun draftMAT(lv: Int = 1, profession: Double = 1.0) =
        (sqrt(_men / 8.0 * 314 * profession * lv) * 1.5).roundToInt()

    fun draftSPD(lv: Int = 1, profession: Double = 1.0) =
        (sqrt(31.4 * lv) / (4.13 * _agi) * profession + 10 - _agi).roundToInt()

    fun show6D(): String = """
    ------六维加点------
    力量:$_str    法力:$_men
    智力:$_int    体质:$_vit
    速度:$_agi    运气:$_lck
    """.trimIndent()
}

/*
    userList:
        user：
            Lv: //用户等级
            Exp:
            Gold:
            ship:
            bag:
    //--------六维数据
    力量：影响物理攻击能力
    魔力：影响法术攻击能力
    智力:影响经验获取能力，攻击能力微调
    体质：影响生命值,影响抗性
    敏捷：影响攻击序列执行顺序
    运气：影响事件成功率

    18/3



    物理攻击伤害 = (我方力量-敌方力量）*(智力*0.1+1*系数）*物理攻击值
    法力攻击伤害 = (我方魔力-敌方魔力）*(智力*0.1+1*系数）*法术攻击值
    附魔攻击伤害 = (我方力量-敌方力量）*(智力*0.1+1*系数）*物理攻击值*0.5+(我方魔力-敌方魔力）*(智力*0.1+1*系数）*物理攻击值*0.5

    经验值 = (智力*0.1+1）*((A等级*A等级)-(B等级*B等级))*0.1

    回合耗时 = 40/（敏捷+2）
    1   2   3   4   5   6   7

    4   2   1.3 1   0.8    0.6


    (0..100).random()+运气*2 >= 目标

    //--------特质
    #出身
    0 奴隶出身：获得 -被剥削者-
    1 平民出身：获得
    2 商贾世家：获得 -钞能力-
    3 皇室出身：获得 -皇室荣光-
    #声誉
    0 恶名远扬：获得 -被通缉者-
    1 默默无闻：无
    2 小有名气：获得 -名人效应-
    3 声名远扬：获得 -被传颂者-
    #职业


 */