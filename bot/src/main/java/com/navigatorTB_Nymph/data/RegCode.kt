package com.navigatorTB_Nymph.data

class RegCode(val map: MutableMap<String, Any?>) {
    val code: String by map
    val key: String by map
    val value: Int by map
}