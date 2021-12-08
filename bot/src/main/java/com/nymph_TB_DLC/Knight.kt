package com.nymph_TB_DLC

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import com.nymph_TB_DLC.CharacterLineDictionary as CLD


/**# 骑士
 *  HP  MP  ATK MAT TPA
 *
 * 1.3  0.7 1.3 0.9 0.7
 * * 每次防御都有50%几率抵挡30%的AD伤害
 */
@Serializable
class Knight(override val name: String) : GameRole() {
    override val professionHP = 1.3
    override val professionMP = 0.7
    override val professionATK = 1.3
    override val professionMAT = 0.9
    override val professionTPA = 0.7
    override var skillPrint = 4    //技能点
    override fun defense(damage: Pair<Double, Double>, logID: String): Int {
        var t = ""
        val k = if (judge(50)) {
            t += "${name}成功招架了这次攻击"
            damage.first * 0.7
        } else {
            t += "$name${CLD.DefenseLine.random()},"
            damage.first
        }
        val d = (damage.second + k).roundToInt()
        return if (MirrorWorldAssets.skillList.contains("[皇室荣光]")) {
            if (d <= lv * 2) {
                BattleRecord().write(logID, t + "并触发了技能[皇室荣光],护盾吸收了所有的伤害")
                0
            } else {
                val a = (damage.first + damage.second).roundToInt() - lv * 2
                BattleRecord().write(logID, t + "并触发了技能[皇室荣光],护盾吸收了${lv * 2}点伤害,最终受到了${a}点伤害")
                a
            }
        } else {
            BattleRecord().write(logID, t + ",受到了${d}点伤害")
            d
        }
    }
}