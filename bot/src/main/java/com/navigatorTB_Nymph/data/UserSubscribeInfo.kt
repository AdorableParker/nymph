package com.navigatorTB_Nymph.data

data class UserSubscribeInfo(val map: MutableMap<String, Any?>) {
    val groupID: Long by map
    val azurLane: Int by map
    val arKnights: Int by map
    val fateGrandOrder: Int by map
    val genShin: Int by map
}