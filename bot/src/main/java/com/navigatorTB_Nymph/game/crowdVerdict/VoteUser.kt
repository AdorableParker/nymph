package com.navigatorTB_Nymph.game.crowdVerdict

import java.time.Instant

class VoteUser {

    private var sentence: Int = 0
    private var sealingLabel: Boolean = true

    private val idList: MutableList<Long> = mutableListOf()

    private val endTime: Long = Instant.now().epochSecond + 600

    fun getSealingLabel(): Boolean = sealingLabel
    fun getBallot(): Int = idList.size
    fun getSentence(): Int = sentence
    fun countdown(): Long = endTime - Instant.now().epochSecond

    private fun poll(voter: Long, i: Int) {
        idList.add(voter)
        sentence += i
    }

    fun punch(): VoteUser {
        sealingLabel = false
        return this
    }

    fun cast(userID: Long, ticketValue: Int = 5): Boolean {
        return if (idList.indexOf(userID) == -1) {
            poll(userID, ticketValue)
            false
        } else true
    }

}