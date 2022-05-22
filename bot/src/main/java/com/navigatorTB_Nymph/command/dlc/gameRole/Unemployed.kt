package com.navigatorTB_Nymph.command.dlc.gameRole

import kotlinx.serialization.Serializable

@Serializable
class Unemployed(
    override val name: String,
    override val natureStr: Int,
    override val natureMen: Int,
    override val natureInt: Int,
    override val natureVit: Int,
    override val natureAgi: Int,
    override val natureLck: Int,
) : GameRole() {
    override val professionHP = 1.0
    override val professionMP = 1.0
    override val professionATK = 1.0
    override val professionMAT = 1.0
    override val professionTPA = 1.0
    override val profession = "无职业"
}