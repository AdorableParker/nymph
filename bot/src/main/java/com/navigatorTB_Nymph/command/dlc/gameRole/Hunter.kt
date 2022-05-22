package com.navigatorTB_Nymph.command.dlc.gameRole

import com.navigatorTB_Nymph.command.dlc.tool.BattleRecordTool
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import com.navigatorTB_Nymph.pluginConfig.CharacterLineDictionary as CLD

/**# 猎手
 *  HP  MP  ATK MAT TPA
 *
 * 1.2  0.8 0.9 0.8 1.3
 * * 20%的几率闪避所有伤害
 */
@Serializable
class Hunter(
    override val name: String,
    override val natureStr: Int,
    override val natureMen: Int,
    override val natureInt: Int,
    override val natureVit: Int,
    override val natureAgi: Int,
    override val natureLck: Int,
) : GameRole() {
    override val professionHP = 1.2
    override val professionMP = 0.8
    override val professionATK = 0.9
    override val professionMAT = 0.8
    override val professionTPA = 1.3
    override val profession: String = "猎手"

    override fun defense(damage: Pair<Double, Double>, logID: String): Int {
        return if (judge(20)) {
            BattleRecordTool().write(logID, "${name}成功闪避了这次攻击")
            0
        } else {
            val d = (damage.first + damage.second).roundToInt() + (natureAgi - 8..natureAgi).random()
            if (skillList.contains("[皇室荣光]")) {
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
    }
}