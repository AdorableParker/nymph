package com.navigatorTB_Nymph.data

data class AssetDataShipBirthday(val map: MutableMap<String, Any?>) {
    val launchYear: String by map
    val launchDay: String by map
    val name: String by map
    val path: String by map
}