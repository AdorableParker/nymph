package com.navigatorTB_Nymph.data

class UserResponsible(val map: MutableMap<String, Any?>) {
    val groupID: Long by map
    val principalID: Long by map
    val active: Int by map
}