package com.navigatorTB_Nymph.data

data class AssetDataTarot(val map: MutableMap<String, Any?>) {
    val brand: String by map
    val upright: String by map
    val reversed: String by map
    val uprightImg: String by map
    val invertImg: String by map
}