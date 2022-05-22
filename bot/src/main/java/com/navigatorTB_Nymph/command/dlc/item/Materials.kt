package com.navigatorTB_Nymph.command.dlc.item

import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole

/** 炼金产物 */
class Materials(override val itemID: Int, override val itemName: String, override val itemInfo: String) : Sundries() {
    override fun useItem(gr: GameRole, s: Int) = "炼金原料只能用于炼金术"
}