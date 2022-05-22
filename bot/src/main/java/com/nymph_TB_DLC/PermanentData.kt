package com.nymph_TB_DLC

import kotlinx.serialization.Serializable

@Serializable
data class PermanentData(var pt: Int = 0, var signIn: Int = 0, val formula: ArrayList<Int> = arrayListOf())