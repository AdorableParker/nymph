package com.nymph_TB_DLC

import kotlin.math.roundToInt
import kotlin.math.sqrt

class Tool(sixD: Array<Int>) {
    private val _str = sixD[0] + 1  //力
    private val _men = sixD[1] + 1  //法
    private val _int = sixD[2] + 1  //智
    private val _vit = sixD[3] + 1  //体
    private val _agi = sixD[4] + 1  //敏
    private val _lck = sixD[5] + 1  //运

    fun draftHP(lv: Int = 1) =
        ((9 * _vit + _str + 45) / 8.0 * 5.4 + sqrt(lv * 235.7) * 5).roundToInt()

    fun draftMP(lv: Int = 1) =
        ((9 * _men + _vit + 45) / 8.0 * 7 + sqrt(lv * 75.3) * 5).roundToInt()

    fun draftATK(lv: Int = 1) =
        (sqrt(_str / 8.0 * 314 * lv) * 1.5).roundToInt()

    fun draftMAT(lv: Int = 1) =
        (sqrt(_men / 8.0 * 314 * lv) * 1.5).roundToInt()

    fun draftTPA(lv: Int = 1) =
        (sqrt(31.4 * lv) / (4.13 * _agi) + 10 - _agi).roundToInt()

    fun show(): String = """
    方案有效,生成角色属性预览如下
    等级: 1
    HP: ${draftHP()}	MP: ${draftMP()}
    ATK: ${draftATK()}	MAT: ${draftMAT()}
    TPA: ${draftTPA()}
    ------六维加点------
    力量:$_str	法力:$_men
    智力:$_int	体质:$_vit
    速度:$_agi	运气:$_lck
    """.trimIndent()
}