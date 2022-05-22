package com.navigatorTB_Nymph.command.dlc.gameRole

import com.navigatorTB_Nymph.command.dlc.tool.BattleRecordTool
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import com.navigatorTB_Nymph.pluginConfig.CharacterLineDictionary as CLD

/**# 骑士
 *  HP  MP  ATK MAT TPA
 *
 * 1.3  0.7 1.3 0.9 0.7
 * * 每次防御都有50%几率抵挡30%的AD伤害
 */
@Serializable
class Knight(
    override val name: String,
    override val natureStr: Int,
    override val natureMen: Int,
    override val natureInt: Int,
    override val natureVit: Int,
    override val natureAgi: Int,
    override val natureLck: Int,
) : GameRole() {
    override val professionHP = 1.3
    override val professionMP = 0.7
    override val professionATK = 1.3
    override val professionMAT = 0.9
    override val professionTPA = 0.7
    override val profession: String = "骑士"

    override fun defense(damage: Pair<Double, Double>, logID: String): Int {
        var t = ""
        val k = if (judge(50)) {
            t += "${name}成功招架了这次攻击"
            damage.first * 0.7
        } else {
            t += "$name${CLD.DefenseLine.random()},"
            damage.first
        }
        val d = (damage.second + k).roundToInt() + (natureAgi - 8..natureAgi).random()
        return if (skillList.contains("[皇室荣光]")) {
            if (d <= lv * 2) {
                BattleRecordTool().write(logID, t + "并触发了技能[皇室荣光],护盾吸收了所有的伤害")
                0
            } else {
                val a = d - lv * 2
                BattleRecordTool().write(logID, t + "并触发了技能[皇室荣光],护盾吸收了${lv * 2}点伤害,最终受到了${a}点伤害")
                a
            }
        } else {
            BattleRecordTool().write(logID, t + ",受到了${d}点伤害")
            d
        }
    }
}