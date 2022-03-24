package com.navigatorTB_Nymph.game.minesweeper

data class MinesweeperMapProperties(val width: Int, val height: Int, val mines: Int) {
    val size = width * height

    fun decoding(x: Int, y: Int): Int = (y - 1) * width + x - 1
    fun decoding(index: Int): Pair<Int, Int> = Pair(index % width + 1, index / width + 1)
    fun conclusion(excavationArea: Int, flagCount: Int) = excavationArea - flagCount == size - mines

    fun mapKey(index: Int): Array<Int> {
        val top by lazy { index - width }
        val bottom by lazy { index + width }
        return when (index + 1) {
            1 -> arrayOf(index + 1, bottom, bottom + 1)
            width -> arrayOf(index - 1, bottom, bottom - 1)
            size - width + 1 -> arrayOf(top, top + 1, index + 1)
            size -> arrayOf(top, top - 1, index - 1)
            in 1..width -> arrayOf(index - 1, index + 1, bottom, bottom - 1, bottom + 1)
            in (1..size step width) -> arrayOf(top, top + 1, index + 1, bottom, bottom + 1)
            in (width..size step width) -> arrayOf(top, top - 1, index - 1, bottom, bottom - 1)
            in (size - width)..size -> arrayOf(top, top - 1, top + 1, index - 1, index + 1)
            else -> arrayOf(top, top - 1, top + 1, index - 1, index + 1, bottom, bottom - 1, bottom + 1)
        }
    }
}