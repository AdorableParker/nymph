package com.example.nymph_TB_DLC

import com.example.nymph_TB_DLC.MirrorWorldAssets.PositiveCorrection
import com.example.nymph_TB_DLC.MirrorWorldUser.userPermanent
import kotlinx.serialization.Serializable

@Serializable
data class PermanentData(var pt: Int)

//val traits = mapOf(
//    "-钞能力-"  to "可以通过花费金币改变判定结果",
//    "-被剥削者-" to "金币收入-60%",
//    "-皇室荣光-" to "可以终止对战或拒绝终止对战",
//    "-被通缉者-" to "击败惩罚+50%",
//    "-名人效应-" to "所有奖励和惩罚加成+50%",
//    "-被传颂者-" to "所有惩罚-10%,所有奖励+10%",
//    "-精打细算-" to "金币收入+50%"
//)


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

@Serializable
class UserObj {
    private var gold: Int = 0                                       //金币，需要保留

    private var lv: Int = 1                                         //等级，不保留
    private var userExp: Int = 0                                    //经验，不保留
    private var ship = arrayOfNulls<Int>(5)                    //同伴，不保留
    private var bag = arrayOfNulls<Int>(16)                    //物品，不保留
    private var traitsList: MutableSet<String> = mutableSetOf()     //特质，不保留
    private var skillList: MutableSet<String> = mutableSetOf()      //技能，不保留
    private var skillPrint: Int = 0                                  //技能点，不保留

    //六维 不保留
    private var strength = 1                    //力量，最低为1，最高为7
    private var magic = 1                       //法力，最低为1，最高为7
    private var intelligence = 1                //智力，最低为1，最高为7
    private var physique = 1                    //体质，最低为1，最高为7
    private var agile = 1                       //敏捷，最低为1，最高为7
    private var luck = 0                        //运气，最低为0，最高为7

    private var hp = Bar(lv * 100)           // 生命值，不保留
    private var mp = Bar(lv * 20)            // 法力值，不保留
    private var atp = 0                         // 物理攻击，不保留
    private var spp = 0                         // 法术攻击，不保留


    fun newRole(origin: Int, reputation: Int, profession: Int, traitsPrint: Int) {
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
        when (profession) {
            0 -> skillList.add("[招架]")
            1 -> skillList.add("[回复]")
            2 -> skillList.add("[闪避]")
            3 -> skillList.add("[附魔]")
        }
        skillPrint = traitsPrint
    }

    private fun judge(goal: Int) = (0..100).random() + luck * 2 >= goal

    //普通攻击计算
    fun attack(foe: UserObj): String {
        //闪避能力最先
        if (foe.skillList.contains("[闪避]") && foe.judge(80)) return "闪避了这次攻击"
        //闪避计算完成
        //初始化值
        var ad = atp.toDouble()
        var ap = 0.0
        val magicDif = (magic - foe.magic) * 0.1 + 1
        val strengthDif = (strength - foe.strength) * 0.1 + 1
        val intelligenceDif = (intelligence - foe.intelligence) * 0.1 + 1
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
        val damage = (ap + ad).toInt()
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
    fun settlement(foe: UserObj): String {
        val exp = (((foe.lv * foe.lv) - (lv * lv)) * 0.2 * ((intelligence * 0.1) + 1) * getTraits("Exp")).toInt()
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

    fun getAPS() = 40 / (agile + 2)
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
    2.骑士：[招架]
    2.牧师：[回复]
    2.猎手：[闪避]
    2.法师：[附魔]
 */