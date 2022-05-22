package com.nymph_TB_DLC

import com.nymph_TB_DLC.CharacterLineDictionary.WizardAttackLine
import kotlinx.serialization.Serializable

/**# 法师
 *  HP  MP  ATK MAT TPA
 *
 * 0.8  1.2 0.8 1.3 0.9
 * * 物理攻击的50%作为法术伤害计算
 */
@Serializable
class Wizard(override val name: String) : GameRole() {
    override val professionHP = 0.8
    override val professionMP = 1.2
    override val professionATK = 0.8
    override val professionMAT = 1.3
    override val professionTPA = 0.9
    override var skillPrint: Int = 4    //技能点
    override fun attack(foe: GameRole, logID: String): Pair<Double, Double> {
        val ad = atk.toDouble()
        val magicDif = (natureMen - foe.natureMen) * MirrorWorldConfig.AttackModifier + 1
        val strengthDif = (natureStr - foe.natureStr) * MirrorWorldConfig.AttackModifier + 1
        val intelligenceDif = (natureInt - foe.natureInt) * MirrorWorldConfig.AttackModifier + 1

        BattleRecord().write(logID, "$name${WizardAttackLine.random()}")

        return Pair(magicDif * intelligenceDif * ad * 0.5, strengthDif * intelligenceDif * ad * 0.5)
    }
}