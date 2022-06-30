package com.navigatorTB_Nymph.data

class AICorpus(val map: MutableMap<String, Any?>) {
    val id: Int by map
    val answer: String by map
    val question: String by map
    val keys: String by map
    val fromGroup: Long by map
}