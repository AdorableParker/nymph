package com.navigatorTB_Nymph.data

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick

class GroupUser(
    val id: Long,
    val avatarUrl: String,
    val nameCardOrNick: String
) {
    constructor(it: Member) : this(it.id, it.avatarUrl, it.nameCardOrNick)
}