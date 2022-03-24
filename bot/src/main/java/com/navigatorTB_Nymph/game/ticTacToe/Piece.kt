package com.navigatorTB_Nymph.game.ticTacToe

enum class Piece {
    /** 圈 先手 */
    Circle {
        override fun rival() = Cross
    },

    /** 叉 后手 */
    Cross {
        override fun rival() = Circle
    },

    /** 空 空白 */
    Blank {
        override fun rival() = throw Exception("Blank没有rival方法")
    };

    abstract fun rival(): Piece
}