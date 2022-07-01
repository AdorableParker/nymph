package com.navigatorTB_Nymph.data

import com.navigatorTB_Nymph.enumData.Feeling


object TB {
    private var feeling = Feeling.Calm                                //心情
    private var zeal = 10

    private fun moodChanges() {
        if (zeal-- <= 0) {
            zeal = 10
            feeling = Feeling.values().random()
        }
    }

    fun getFeeling() = feeling

    fun nudge(): Int {
        moodChanges()
        return when (feeling) {
            Feeling.Happy, Feeling.Sad -> 12
            Feeling.Boring, Feeling.Calm -> 8
            Feeling.Irritable, Feeling.Angry -> -5
        } * zeal
    }

    fun chat(): Int {
        moodChanges()
        return when (feeling) {
            Feeling.Happy, Feeling.Boring -> 12
            Feeling.Sad, Feeling.Calm -> 10
            Feeling.Angry, Feeling.Irritable -> -8
        } * zeal
    }

    fun play(): Int {
        moodChanges()
        return when (feeling) {
            Feeling.Happy, Feeling.Boring -> 16
            Feeling.Calm -> 11
            Feeling.Angry, Feeling.Irritable, Feeling.Sad -> -10
        } * zeal
    }

    fun abuse(): Int {
        moodChanges()
        return when (feeling) {
            Feeling.Happy -> 5
            Feeling.Calm, Feeling.Boring -> 0
            Feeling.Angry, Feeling.Irritable, Feeling.Sad -> -20
        } * zeal
    }
}

//Happy,      //高兴
//Angry,      //生气
//Irritable,  //烦躁
//Sad,        //伤心
//Boring,     //无聊
//Calm        //平静