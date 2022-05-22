package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.command.dlc.data.Shelf
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object FleaMarket : AutoSavePluginData("DLC_ShopData") { // "name" 是保存的文件名 (不带后缀)
    /** 货架数据
     *
     *  物品名:(卖家id,单价,数量)
     */
    @ValueDescription("货架数据")
    val shelfData: MutableMap<String, MutableList<Shelf>> by value(
        mutableMapOf()
    )

    /** 查看货柜 */
    fun viewShelf(): String {
        val productList = StringBuilder()
        shelfData.forEach { (productName, sellerList) ->
            if (sellerList.size != 0) {
                productList.append("$productName:\n")
                sellerList.forEach {
                    productList.append("\t${it.unitPrice}金币/个 - 剩余:${it.quantity}个\n")
                }
            }
        }
        return productList.toString()
    }
}