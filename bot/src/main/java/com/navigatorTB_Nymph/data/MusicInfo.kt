package com.navigatorTB_Nymph.data

import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare

class MusicInfo(val type: MusicKind, val songName: String, val musicURL: String, val jumpUrl: String) {
    fun constructorMusicCard(pictureUrl: String) = MusicShare(
        type,
        songName,
        "歌曲信息 来源于 领航员-TB 智障搜索",                // 内容:会显示在title下面
        jumpUrl,
        pictureUrl,
        musicURL
    )
}