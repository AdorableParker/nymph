package com.example.nymph_TB_DLC

import com.example.nymph_TB_DLC.MirrorWorldAssets.PositiveCorrection
import com.example.nymph_TB_DLC.MirrorWorldConfig.AttackModifier
import com.example.nymph_TB_DLC.MirrorWorldUser.userPermanent
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Serializable
data class PermanentData(var pt: Int = 0, var pc: PlayerCharacter? = null) {
    fun outInfo(): String {
        return """
        拥有Pt:   $pt
        ==========
        玩家角色:
        ${pc?.info() ?: "角色未建立"}
        """.trimIndent()
    }
}
/*
val traits = mapOf(
    "-钞能力-"  to "可以通过花费金币改变判定结果",
    "-被剥削者-" to "金币收入-60%",
    "-皇室荣光-" to "可以终止对战或拒绝终止对战",
    "-被通缉者-" to "击败惩罚+50%",
    "-名人效应-" to "所有奖励和惩罚加成+50%",
    "-被传颂者-" to "所有惩罚-10%,所有奖励+10%",
    "-精打细算-" to "金币收入+50%"
)
 */


@Serializable
data class Bar(var Max: Int) {
    var current: Int = Max
    fun harm(v: Int): Boolean {
        return if (current >= v) {
            current -= v
            true
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
class PlayerCharacter {
    private var gold: Int = 0                                       //金币，需要保留

    private var lv: Int = 1                                         //等级
    private var userExp: Int = 0                                    //经验
    private var ship = arrayOfNulls<Int>(5)                    //同伴
    private var bag = arrayOfNulls<Int>(16)                    //物品
    private var traitsList: MutableSet<String> = mutableSetOf()     //特质
    private var skillList: MutableSet<String> = mutableSetOf()      //技能
    private var skillPrint: Int = 0                                 //技能点
    private var attributePrint: Int = 22                            //属性点

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
    private var profession = arrayOf<Double>()  //职业属性曲线

    /* 成长曲线
                atk   mat    spd     hp    mp
    2.骑士：[招架] 1.3   0.9    0.7    1.4   0.7 = 5
    2.猎手：[闪避] 0.9   0.8    1.3    1.2   0.8 = 5
    2.牧师：[回复] 1.1   1.1    0.9    0.9   1.0 = 5
    2.法师：[附魔] 0.8   1.3    0.9    0.8   1.2 = 5
     */

    private var hp = Bar(0)         // 生命值
    private var mp = Bar(0)         // 法力值
    private var atk = 0                  // 物理攻击
    private var mat = 0                  // 法术攻击
    private var spd = 0                  // 行动速度

    fun info(): String {
        val buffer = StringBuilder()
        for ((i, element) in traitsList.withIndex()) {
            buffer.append(if (i % 2 == 1) "\t${element}" else "\n${element}")
        }
        return """
            等级:$lv\t金币:${gold}枚
            HP:${hp.current}/${hp.Max}\tMP:${mp.current}/${mp.Max}
            ATK:$atk\tMAT:${mat}
            经验:$userExp/${lv * lv}
            闲置技能点:$skillPrint
            拥有特质:$buffer
            拥有技能:$buffer
            ------六维加点------
            力量:$_str\t法力:$_men
            智力:$_int\t体质:$_vit
            速度:$_agi\t运气:$_lck
            """.trimIndent()
    }

    fun showAP() = attributePrint

    fun newRole(origin: Int, reputation: Int, prof: Int, traitsPrint: Int) {
        when (origin) {
            0 -> traitsList.add("-被剥削者-")
            2 -> {
                traitsList.add("-钞能力-")
                traitsList.add("-精打细算-")
            }
            3 -> {
                traitsList.add("-皇室荣光-")
                gold += 500
            }
        }
        when (reputation) {
            0 -> traitsList.add("-被通缉者-")
            2 -> traitsList.add("-名人效应-")
            3 -> traitsList.add("-被传颂者-")
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
    }

    private fun judge(goal: Int) = (0..100).random() + _lck - 4 + 5 / (2 + _lck) >= goal

    //普通攻击计算
    fun attack(foe: PlayerCharacter): String {
        //闪避能力最先
        if (foe.skillList.contains("[闪避]") && foe.judge(80)) return "闪避了这次攻击"
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
        val damage = (ap + ad).roundToInt()
        return if (foe.hp.harm(damage)) "最终受到了${damage}点伤害" else "被打败了"
    }


    private fun getTraits(type: String): Double {
        var negative = 0.0     //负面加成修正计算
        var positive = 0.0     //正面加成修正计算

        for ((name, traits) in PositiveCorrection.getOrDefault("All", mutableMapOf())) {
            if (traitsList.contains(name)) {
                positive += traits.first
                negative -= traits.second
            }
        }
        for ((name, traits) in PositiveCorrection.getOrDefault(type, mutableMapOf())) {
            if (traitsList.contains(name)) {
                positive += traits.first
                negative -= traits.second
            }
        }
        for ((name, traits) in PositiveCorrection.getOrDefault("bonus", mutableMapOf())) {
            if (traitsList.contains(name)) {
                positive *= traits.first
                negative *= traits.second
            }
        }
        return negative + positive + 1
    }

    //玩家战斗胜利结算
    fun settlement(foe: PlayerCharacter): String {
        val exp = (((foe.lv * foe.lv) - (lv * lv)) * 0.2 * ((_int * 0.1) + 1) * getTraits("Exp")).toInt()
        val getGold = (getTraits("Gold") * foe.gold * 0.1).toInt()
        userExp += exp
        while (userExp >= lv * lv) {
            userExp -= lv * lv
            lv++
        }
        gold += getGold
        return "获得了${exp}点经验,${getGold}枚金币"
    }

    //玩家战斗失败结算
    fun destroy(id: Long): String {
        val pt = gold / 3 + lv
        userPermanent[id]!!.pt = pt
        return "你死了,本局游戏结束,角色数据删除,你获得${pt}积分"
    }

    //战斗序列计算
    fun getAPS() = 40 / (_agi + 2)
    fun set6D(plan: Array<Int>) {
        _str += plan[0]
        _men += plan[1]
        _int += plan[2]
        _vit += plan[3]
        _agi += plan[4]
        _lck += plan[5]
    }
}

class Tool(sixD: Array<Int>) {
    private val _str = sixD[0] + 1  //力
    private val _men = sixD[1] + 1  //法
    private val _int = sixD[2] + 1  //智
    private val _vit = sixD[3] + 1  //体
    private val _agi = sixD[4] + 1  //敏
    private val _lck = sixD[5] + 1  //运

    fun draftHP(lv: Int = 1, profession: Double = 1.0) =
        ((9 * _vit + _str + 45) / 8.0 * 54 * profession + sqrt(lv * 235.7) * 5).roundToInt()

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
    力量:$_str\t法力:$_men
    智力:$_int\t体质:$_vit
    速度:$_agi\t运气:$_lck
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