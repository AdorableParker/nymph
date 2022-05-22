package com.navigatorTB_Nymph.command.dlc.item

import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole

/** 心智魔方 **/
object Cube : Sundries() {
    override val itemID: Int = 0
    override val itemName: String = "心智魔方"
    override val itemInfo: String = "即使是业内最先进的炼金理论也无法对其性质进行解释的物质,散发着微蓝的荧光"
    override fun useItem(gr: GameRole, s: Int): String = "不知道如何使用它"
}