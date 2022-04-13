package com.navigatorTB_Nymph.data

data class AssetDataScript(val map: MutableMap<String, Any?>) {
    val content: String by map
    val hour: Int by map
    val mode: Int by map
}