package com.nymph_TB_DLC

import kotlinx.serialization.Serializable
import com.nymph_TB_DLC.CharacterLineDictionary as CLD

/**# 牧师
 *  HP  MP  ATK MAT TPA
 *
 * 1.1  1.1 0.9 0.9 1.0
 * * 每次攻击都有30%几率回复已损失生命值的10%
 */
@Serializable
class Priest(override val name: String) : GameRole() {

    override val professionHP = 1.1
    override val professionMP = 1.1
    override val professionATK = 0.9
    override val professionMAT = 0.9
    override val professionTPA = 1.0
    override var skillPrint = 4    //技能点
    override fun attack(foe: GameRole, logID: String): Pair<Double, Double> {
        if (judge(30)) {
            val nowHP = talentSkills()
            BattleRecord().write(logID, "$name${CLD.AttackLine.random()},并触发了恢复效果,生命值恢复到$nowHP")
        } else BattleRecord().write(logID, "$name${CLD.AttackLine.random()}")
        val magicDif = (natureMen - foe.natureMen) * MirrorWorldConfig.AttackModifier + 1
        val intelligenceDif = (natureInt - foe.natureInt) * MirrorWorldConfig.AttackModifier + 1
        return Pair(atk.toDouble() * magicDif * intelligenceDif, 0.0)
    }

    private fun talentSkills(): Int {
        return hp.treatment((hp.max - hp.current) / 10)
    }
}