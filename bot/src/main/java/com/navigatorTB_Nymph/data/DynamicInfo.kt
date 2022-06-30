package com.navigatorTB_Nymph.data

class DynamicInfo(
    val timestamp: Long,
    val name: String?,
    val face: String?,
    val text: String,
    val pictureList: List<Picture?> = listOf()
)