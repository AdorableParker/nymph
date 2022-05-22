package com.navigatorTB_Nymph.command.dlc.item

import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole

abstract class Item {
    abstract val itemID: Int
    abstract val itemName: String
    abstract val itemInfo: String
    abstract fun useItem(gr: GameRole, s: Int): String
}