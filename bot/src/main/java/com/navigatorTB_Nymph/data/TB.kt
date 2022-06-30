package com.navigatorTB_Nymph.data

import com.navigatorTB_Nymph.enumData.Feeling

object TB {
    private var feeling = Feeling.Happy                                //心情

    private var patience = 5        //耐心
    private var enthusiasm = 5      //兴趣
    private var fear = 0            //恐惧
    private var vigor = 5           //精力


    fun moodChanges() {
        // TODO: 2022/7/1 待构筑决策树
//        Feeling.Happy,      //高兴 -> 精力 >= 4 耐心 > 3 兴趣 > 2 恐惧 <= 1
//        Feeling.Angry,      //生气 -> 精力 >= 4 耐心 >= 3 兴趣 >= 1 恐惧 >= 3
//        Feeling.Irritable,  //烦躁 -> 精力 >= 2 耐心 <= 2 兴趣 <= 2 恐惧 <= 3
//        Feeling.Sad,        //伤心 -> 精力 >= 3 耐心 <= 2 兴趣 <= 2 恐惧 >= 4
//        Feeling.Boring,     //无聊 -> 精力 >= 4 耐心 <= 5 兴趣 <= 1 恐惧 >= 0
//        Feeling.Calm        //平静 -> 精力 >= 3 耐心 >= 3 兴趣 >= 3 恐惧 <= 1
    }


    fun touch(): Double {
        return when (feeling) {
            Feeling.Happy, Feeling.Sad -> 1.5
            Feeling.Boring, Feeling.Calm -> 1.0
            Feeling.Irritable, Feeling.Angry -> -0.5
        }
    }

    fun chat(): Double {
        return when (feeling) {
            Feeling.Happy, Feeling.Boring -> 1.5
            Feeling.Sad, Feeling.Calm -> 1.0
            Feeling.Angry, Feeling.Irritable -> -0.5
        }
    }

    fun play(): Double {
        return when (feeling) {
            Feeling.Happy, Feeling.Boring -> 1.5
            Feeling.Calm -> 1.0
            Feeling.Angry, Feeling.Irritable, Feeling.Sad -> -0.5
        }
    }
}