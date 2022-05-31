package com.navigatorTB_Nymph.command.dlc.mirrorWorld

import com.navigatorTB_Nymph.command.dlc.data.Shelf
import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole
import com.navigatorTB_Nymph.pluginData.FleaMarket
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser

class Shop(private val user: GameRole) {
    /** 采购
     * @param[shelfName]物品名
     * @param[demand]购买数量
     */
    fun buy(shelfName: String, demand: Int): String {
        val productList = FleaMarket.shelfData[shelfName]
        if (productList.isNullOrEmpty()) return "该物品暂时无货"
        if (user.checkBackpackSpace(shelfName)) return "你的背包空间不足"
        val bill = StringBuilder()
        var purchase = demand
        var price = 0

        for (shelf in productList) {
//            val (seller, unitPrice, inventory) = productList[0]
            if (shelf.unitPrice == 0) return "该物品暂时无货"
            var available = user.showGold() / shelf.unitPrice                         // 剩余预算能买的数量
            if (available <= 0) break                                           // 一个都买不起
            if (available > shelf.quantity) available = shelf.quantity                    // 能买的大于库存
            when {
                purchase < available -> {                                       // 要买的小于能买的
                    val receivable = purchase * shelf.unitPrice                       // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[shelf.merchant]?.getPaid(receivable)       // 收款
                    productList[0] = Shelf(shelf.merchant, shelf.unitPrice, available - purchase)    // 拿走要买的数量
                    bill.append("以${shelf.unitPrice}/个的价格购买${purchase}个\n")
                    break
                }
                purchase > available -> {                                       // 要买的大于能买的
                    val receivable = available * shelf.unitPrice                      // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[shelf.merchant]?.getPaid(receivable)       // 收款
                    purchase -= available
                    productList.removeFirst()                                   // 清空货架
                    bill.append("以${shelf.unitPrice}/个的价格购买${available}个\n")
                }
                else -> {                                                       // 要买的等于能买的
                    val receivable = available * shelf.unitPrice                      // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[shelf.merchant]?.getPaid(receivable)       // 收款
                    purchase = 0
                    productList.removeFirst()                                   // 清空货架
                    bill.append("以${shelf.unitPrice}/个的价格购买${available}个\n")
                    break
                }
            }
        }

        bill.append("---------\n共计花费货款${price}枚金币")
        user.receiveItem(shelfName, demand - purchase)                        // 要买的 减 没买的
        return bill.toString()
    }

    /** 挂售
     * @param[shelfName]物品名
     * @param[uid]用户ID
     * @param[unitPrice]出售单价
     * @param[inventory]出售数量
     * */
    fun sell(shelfName: String, uid: Long, unitPrice: Int, inventory: Int): String {
        if (user.consumeItems(shelfName, inventory).not()) return "物品拥有数量不足"
        val productList = FleaMarket.shelfData.getOrPut(shelfName) { mutableListOf() }

        val k = productList.find { it.unitPrice == unitPrice }
        if (k == null) productList.add(Shelf(uid, unitPrice, inventory))                // 无同人同价位 上架
        else productList.add(Shelf(uid, unitPrice, inventory + k.quantity))     // 有同人同价位 补货
        FleaMarket.shelfData[shelfName] = productList                                   // 更新到商店
        productList.sortByDescending { it.unitPrice }                                   // 整理货架
        return "挂售成功,物品已上架"
    }
}