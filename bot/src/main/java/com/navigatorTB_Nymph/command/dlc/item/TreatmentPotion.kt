package com.navigatorTB_Nymph.command.dlc.item

import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole

/** 生性药水 */
class TreatmentPotion(override val itemID: Int, override val itemName: String, override val itemInfo: String) :
    Potion() {

    private val potionAmount by lazy {
        when (itemID % 100 / 10) {
            1 -> 15
            3 -> 40
            else -> 90
        }
    }

    /**
     * ### 药水等级
     * 0. 初级药水:定量恢复
     * 1. 中级药水:损伤值百分比恢复
     * 2. 高级药水:最大值百分比恢复
     * 3. 特级药水:回满
     * ### 药水恢复量
     * 1. 小瓶药水:15 15%
     * 2. 中瓶药水:40 40%
     * 3. 大瓶药水:90 90%
     * ### 药水恢复类型
     * 1. HP药水: 恢复HP
     * 2. MP药水: 恢复MP
     * 3. 全能药水: 恢复HP和MP
     */
    override fun useItem(gr: GameRole, s: Int): String {
        for (und in 1..s) {
            gr.consumeItem(itemName) // 消耗一个物品
            if (itemID % 10 != 1) drinkMPPotion(gr)
            if (itemID % 10 != 4) drinkHPPotion(gr)
        }
        return "使用了${s}瓶$itemName"
    }

    private fun drinkHPPotion(gr: GameRole) {
        when (itemID / 100) {
            1 -> potionAmount
            2 -> potionAmount * gr.hp.current / 100
            3 -> gr.hp.max * potionAmount / 100
            else -> gr.hp.max
        }.let { gr.hp.treatment(it) }
    }

    private fun drinkMPPotion(gr: GameRole) {
        when (itemID / 100) {
            1 -> potionAmount
            2 -> potionAmount * gr.mp.current / 100
            3 -> gr.mp.max * potionAmount / 100
            else -> gr.mp.max
        }.let { gr.mp.treatment(it) }
    }
}