package com.nymph_TB_DLC

import kotlinx.serialization.Serializable

@Serializable
class Unemployed(override val name: String) : GameRole() {
    override val professionHP = 1.0
    override val professionMP = 1.0
    override val professionATK = 1.0
    override val professionMAT = 1.0
    override val professionTPA = 1.0
    override var skillPrint: Int = 6
}