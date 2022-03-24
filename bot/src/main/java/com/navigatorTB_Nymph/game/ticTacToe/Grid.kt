package com.navigatorTB_Nymph.game.ticTacToe

class Grid(val column: Int, val row: Int) {
    fun angle() = column == row || column + row == 4
    fun type(): Int {
        return when {
            column == 2 && row == 2 -> 0
            column == 2 || row == 2 -> 1
            else -> 2
        }
    }
}