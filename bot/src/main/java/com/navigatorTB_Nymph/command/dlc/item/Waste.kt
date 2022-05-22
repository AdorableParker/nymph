package com.navigatorTB_Nymph.command.dlc.item

import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole

/** 垃圾 */
object Waste : Sundries() {
    override val itemID = 0
    override val itemName: String = "垃圾"
    override val itemInfo: String = "一坨废物"
    override fun useItem(gr: GameRole, s: Int) = "这是一坨无法使用的废物"
}