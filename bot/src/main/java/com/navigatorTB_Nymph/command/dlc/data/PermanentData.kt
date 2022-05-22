package com.navigatorTB_Nymph.command.dlc.data

import kotlinx.serialization.Serializable

@Serializable
data class PermanentData(
    var pt: Int = 0,
    var nichianTime: Int = 0,
    var signIn: Int = 0,
    val formula: MutableMap<String, MutableSet<Int>> = mutableMapOf()
)