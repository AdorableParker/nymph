package com.navigatorTB_Nymph.data

data class AssetDataAzurLaneConstructTime(val map: MutableMap<String, Any?>) {
    val originalName: String by map
    val alias: String by map
    val time: String by map
    val limitedTime: Int by map
    val type: Int by map
}