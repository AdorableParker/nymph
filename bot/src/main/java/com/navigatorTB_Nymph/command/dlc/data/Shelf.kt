package com.navigatorTB_Nymph.command.dlc.data

import kotlinx.serialization.Serializable

@Serializable
data class Shelf(val merchant: Long, val unitPrice: Int, val quantity: Int)