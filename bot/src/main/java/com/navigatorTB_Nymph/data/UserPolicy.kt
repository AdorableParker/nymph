package com.navigatorTB_Nymph.data

data class UserPolicy(val map: MutableMap<String, Any?>) {
    val groupID: Long by map
    val tellTimeMode: Int by map
    val dailyReminderMode: Int by map
    val teaching: Int by map
    val triggerProbability: Int by map
    val acgImgAllowed: Int by map
    val undisturbed: Int by map
    val groupNotification: Int by map
}