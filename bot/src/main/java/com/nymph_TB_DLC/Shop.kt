package com.nymph_TB_DLC

class Shop(private val user: GameRole) {
    /** 查看货柜 */
    fun viewShelf(): String {
        val productList = StringBuilder()
        FleaMarket.shelfData.forEach { (productID, sellerList) ->
            productList.append("${ItemTable.productList.find { it.itemID == productID }}:\n")
            sellerList.forEach { (_, unitPrice, quantity) ->
                productList.append("\t${unitPrice}金币/个 - 剩余:${quantity}个")
            }
        }
        return productList.toString()
    }

    /** 采购
     * @param[shelfID]物品ID
     * @param[demand]购买数量
     */
    fun buy(shelfID: Int, demand: Int): String {
        val productList = FleaMarket.shelfData[shelfID]
        if (productList.isNullOrEmpty()) return "该物品暂时无货"

        val bill = StringBuilder()
        var purchase = demand
        var price = 0

        while (productList.isNotEmpty()) {
            val (seller, unitPrice, inventory) = productList[0]
            var available = user.showGold() / unitPrice                         // 剩余预算能买的数量
            if (available <= 0) break                                           // 一个都买不起
            if (available > inventory) available = inventory                    // 能买的大于库存
            when {
                purchase < available -> {                                       // 要买的小于能买的
                    val receivable = purchase * unitPrice                       // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[seller]?.getPaid(receivable)       // 收款
                    productList[0] = Triple(seller, unitPrice, available - purchase)    // 拿走要买的数量
                    bill.append("以$unitPrice/个的价格购买${purchase}个\n")
                    break
                }
                purchase > available -> {                                       // 要买的大于能买的
                    val receivable = available * unitPrice                      // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[seller]?.getPaid(receivable)       // 收款
                    purchase -= available
                    productList.removeFirst()                                   // 清空货架
                    bill.append("以$unitPrice/个的价格购买${available}个\n")
                }
                purchase == available -> {                                       // 要买的等于能买的
                    val receivable = available * unitPrice                      // 应收
                    val actualPayment = user.payFine(receivable)                // 实付
                    if (actualPayment != -1) price += actualPayment else break  // 付款失败退出

                    MirrorWorldUser.userRole[seller]?.getPaid(receivable)       // 收款
                    productList.removeFirst()                                   // 清空货架
                    bill.append("以$unitPrice/个的价格购买${available}个\n")
                    break
                }
            }
        }
        bill.append("---------\n共计花费货款${price}枚金币")
        user.receiveItem(shelfID, demand - purchase)             // 要买的 减 没买的
        return bill.toString()
    }

    /** 挂售
     * @param[shelfID]物品ID
     * @param[uid]用户ID
     * @param[unitPrice]出售单价
     * @param[inventory]出售数量
     * */
    fun sell(shelfID: Int, uid: Long, unitPrice: Int, inventory: Int): String {
        if (user.consumeItems(shelfID, inventory).not()) return "物品拥有数量不足"
        val productList = FleaMarket.shelfData.getOrPut(shelfID) { mutableListOf() }
        val k = productList.find { it.first == uid && it.second == unitPrice }
        if (k == null) productList.add(Triple(uid, unitPrice, inventory))            // 无同人同价位 上架
        else productList.add(Triple(uid, unitPrice, inventory + k.third))    // 有同人同价位 补货
        productList.sortBy { it.second }                                          // 整理货架
        return "挂售成功,物品已上架"
    }
}