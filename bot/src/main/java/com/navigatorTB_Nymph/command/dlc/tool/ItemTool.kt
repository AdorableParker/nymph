package com.navigatorTB_Nymph.command.dlc.tool

import com.navigatorTB_Nymph.command.dlc.item.*
import kotlin.math.absoluteValue

object ItemTool {
    private val materialsList by lazy {
        arrayOf(
            Materials(-1000, "以太(殁)", "构成世间万物的最基本的元素"),
            Materials(-58, "赤化结晶", "黑-白-黄-赤,这是炼成贤者之石的必要原料"),
            Materials(-38, "日之盐", "黄化结晶的衍生品"),
            Materials(-26, "月之盐", "白化结晶的衍生品"),
            Materials(-21, "虚无之盐", "古典派炼金术认为这是炼出贤者之石需要的关键媒介"),
            Materials(1, "基质(风)", "元素基质"),
            Materials(3, "基质(水)", "元素基质"),
            Materials(4, "基质(盐)", "元素基质"),
            Materials(7, "基质(火)", "元素基质"),
            Materials(9, "基质(土)", "元素基质"),
            Materials(5, "硼", "殊剂"),
            Materials(6, "碳", "殊剂"),
            Materials(14, "硅", "殊剂"),
            Materials(15, "磷", "殊剂"),
            Materials(16, "硫", "殊剂"),
            Materials(20, "钙", "殊剂"),
            Materials(21, "生命之盐", "古典派炼金术认为这是炼出贤者之石需要的关键媒介"),
            Materials(22, "黑化结晶", "黑-白-黄-赤,你迈出了关键的第一步"),
            Materials(23, "黄化结晶", "黑-白-黄-赤,还差一点点"),
            Materials(26, "铁", "原体"),
            Materials(29, "铜", "原体"),
            Materials(31, "贤者之石", "古典派炼金术的终极目标,是世间终极奥义的钥匙"),
            Materials(33, "砷", "触媒"),
            Materials(34, "白化结晶", "黑-白-黄-赤,你感觉节奏没错,第二步已经实现"),
            Materials(41, "银", "闪耀着银色的光芒"),
            Materials(50, "锡", "原体"),
            Materials(79, "金", "是金子,可惜你没有铸币权(可以兑换成金币)"),
            Materials(80, "汞", "银种"),
            Materials(82, "铅", "原体"),
            Materials(1000, "以太(生)", "构成世间万物的最基本的元素"),
            Waste
        )
    }
    private val commonThingsList by lazy {
        arrayOf(
            CommonThings(-503, "炎椒果", "常被人作为香料使用"),
            CommonThings(-502, "影甲虫壳", "暗影甲虫的壳"),
            CommonThings(-97, "殁露菇", "吸收尸水长大的蘑菇"),
            CommonThings(-94, "地精菇", "因常见于地下洞穴而得名"),
            CommonThings(-89, "血红石", "血红色的石头"),
            CommonThings(-86, "硼辉石", "冶炼金属时常用的催化剂"),
            CommonThings(-83, "巫毒菌", "这种蘑菇的粉末常被用于制作巫毒娃娃"),
            CommonThings(-82, "冰晶蓝宝石", "生长在极地严寒的洞穴之中"),
            CommonThings(-79, "云晶", "雾蒙蒙的水晶,就像云一样"),
            CommonThings(-74, "芒草", "带有尖刺的野草"),
            CommonThings(-73, "火凰晶", "传说菲尼克斯的血液滴到了水晶上就有了这玩意"),
            CommonThings(-71, "疣腕章鱼", "它似乎和旧日有关,仅仅看见触须上疣突的纹路就感觉精神恍惚"),
            CommonThings(-67, "炎熔根果", "吃了之后会浑身发热"),
            CommonThings(-64, "硫铜矿", "这种铜矿产自硫磺泉"),
            CommonThings(-61, "旭日菊", "这种菊花总是朝着太阳"),
            CommonThings(-59, "血棘藤", "带刺的棘条,红色的表皮看上去就像是鲜血一样"),
            CommonThings(-53, "星芯藤", "切断藤曼就能看见星星一样的花纹"),
            CommonThings(-47, "紫苏云母", "高贵的紫色云母"),
            CommonThings(-43, "风铃花", "制作法力药剂的常见材料"),
            CommonThings(-41, "阳斑菇", "菌盖上的橙黄色斑点就像太阳一样"),
            CommonThings(-37, "水银藤", "顺着这种神奇植物,总是能找到银矿"),
            CommonThings(-32, "硫磺泉水", "硫磺泉的水,散发着刺鼻的气味"),
            CommonThings(37, "蓝色风信子", "制作药剂的常见材料"),
            CommonThings(43, "雷蓟花", "它生长的地方,周围肯定有雷蝎的巢"),
            CommonThings(47, "磷灵草", "燃烧时火焰是幽蓝色的,据说死灵法师用它作为施法材料"),
            CommonThings(53, "奇异菇", "吃了它的人都说听见了神谕,但谁都知道那不过是中毒产生的幻觉"),
            CommonThings(59, "黑曜石", "每次火山喷发后都能捡到一大坨"),
            CommonThings(61, "毒沼菇", "生长在沼泽地的蘑菇"),
            CommonThings(67, "希灵水晶", "诡异的蓝色的水晶,散发着危险的气息"),
            CommonThings(71, "雷蝎毒液", "雷蝎的毒液,这么一小瓶就值一堆金币"),
            CommonThings(73, "冰霜果", "据说冰龙喜欢吃这东西"),
            CommonThings(74, "暗影棘条", "这种荆棘生长在阳光永远照射不到的地方"),
            CommonThings(83, "氨香树菇", "这种树菇煮熟后就会散发一种奇怪的味道"),
            CommonThings(86, "蓝枫树果", "用它制作的药剂,喝了之后尿液都是蓝色的"),
            CommonThings(89, "金棘藤", "绿色的藤曼却有着金色的刺"),
            CommonThings(94, "墓穴鹅膏菌", "生长在墓地里的剧毒蘑菇"),
            CommonThings(97, "缠枝藤", "又湿又滑的藤曼"),
            CommonThings(502, "月见花", "制作治疗药剂的常见材料"),
            CommonThings(503, "黄铁矿", "愚人金")
        )
    }

    fun randomCommonThings(): String {
        return commonThingsList.random().itemName
    }

    /** 解析物品ID */
    fun find(itemID: Int): Item {
        return when {                                                                                           // -Inf..Inf
            itemID in -499..-100 -> potionParsingDie(itemID)                                              // -499..-100
            itemID in 100..499 -> potionParsing(itemID)                                                   // 100..499
            itemID >= 1000 -> Materials(1000, "以太(生)", "构成世间万物的最基本的元素")
            itemID <= -1000 -> Materials(-1000, "以太(殁)", "构成世间万物的最基本的元素")
            else -> potionMaterials(itemID)                                                                     // -999..-499 or -99..99 or 500..999
        }
    }

    /** 解析物品名字 */
    fun find(itemName: String): Item? {
        return materialsList.find { it.itemName == itemName } ?: commonThingsList.find { it.itemName == itemName }
        ?: deName(itemName)
    }

    /** 生性药水命名
     * ### 共计30款
     * | Level |/| Size |/| HP |/| MP |/| ALL |
     * |:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
     * |      |/| 小 |*| 100 |*| 104 |*| 108 |
     * | 初级 |-| 标 |*| 130 |*| 134 |*| 138 |
     * |      |\| 大 |*| 160 |*| 164 |*| 168 |
     * |      |/| 小 |*| 200 |*| 204 |*| 208 |
     * | 中级 |-| 标 |*| 230 |*| 234 |*| 238 |
     * |      |\| 大 |*| 260 |*| 264 |*| 268 |
     * |      |/| 小 |*| 300 |*| 304 |*| 308 |
     * | 高级 |-| 标 |*| 330 |*| 334 |*| 338 |
     * |      |\| 大 |*| 360 |*| 364 |*| 368 |
     * | 特级 |-| - |*| 430 |*| 434 |*| 438 |
     * */
    private fun potionParsing(itemID: Int): Potion {
        var potionID = 0
        val potionType = when (itemID % 10 / 4) { //取个位 0..9
            0 -> {
                "HP药水"
            }
            1 -> {
                potionID += 4;"MP药水"
            }
            else -> {
                potionID += 8;"全能药水"
            }
        }
        val c = when (itemID % 100 / 30) { //取十位 0..9
            0 -> {
                "(小)"
            }
            1 -> {
                potionID += 30;""
            }
            else -> {
                potionID += 60;"(大)"
            }
        }
        return when (itemID / 100) { //取百位 0..9
            1 -> {
                potionID += 100;"初级$potionType$c"
            }
            2 -> {
                potionID += 200;"中级$potionType$c"
            }
            3 -> {
                potionID += 300;"高级$potionType$c"
            }
            else -> {
                potionID = 430 + potionID % 10;"特级$potionType"
            }
        }.let { name -> TreatmentPotion(potionID, name, "可以喝的药水,散发着生命的气息") }
    }

    /** 殁性药水命名
     * ### 共计30款
     * | Level |/| Size |/| HP |/| MP |/| ALL |
     * |:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
     * |      |/| 小 |*| 100 |*| 104 |*| 108 |
     * | 初级 |-| 标 |*| 130 |*| 134 |*| 138 |
     * |      |\| 大 |*| 160 |*| 164 |*| 168 |
     * |      |/| 小 |*| 200 |*| 204 |*| 208 |
     * | 中级 |-| 标 |*| 230 |*| 234 |*| 238 |
     * |      |\| 大 |*| 260 |*| 264 |*| 268 |
     * |      |/| 小 |*| 300 |*| 304 |*| 308 |
     * | 高级 |-| 标 |*| 330 |*| 334 |*| 338 |
     * |      |\| 大 |*| 360 |*| 364 |*| 368 |
     * | 特级 |-| - |*| 430 |*| 434 |*| 438 |
     * */
    private fun potionParsingDie(itemID: Int): Potion {
        var potionID = 0
        val potionType = when (itemID % 10 / 4) { //取个位 0..9
            0 -> {
                "HP药水"
            }
            1 -> {
                potionID += 4;"MP药水"
            }
            else -> {
                potionID += 8;"全能药水"
            }
        }
        val c = when (itemID % 100 / 30) { //取十位 0..9
            0 -> {
                "(小)"
            }
            1 -> {
                potionID += 30;""
            }
            else -> {
                potionID += 60;"(大)"
            }
        }
        return when (itemID / 100) { //取百位 0..9
            1 -> {
                potionID += 100;"殁性初级$potionType$c"
            }
            2 -> {
                potionID += 200;"殁性中级$potionType$c"
            }
            3 -> {
                potionID += 300;"殁性高级$potionType$c"
            }
            else -> {
                potionID = 430 + potionID % 10;"殁性特级$potionType"
            }
        }.let { name -> HarmPotion(-potionID, name, "可以喝的药水,散发着死亡的气息") }
    }

    /** 炼金原料命名 */
    private fun potionMaterials(itemID: Int): Sundries { // -999..-500 or -99..-21 or 21..99 or 500..999
        return when (itemID) {
            in -20..20 -> when (itemID.absoluteValue) { // -20..20
                0 -> Cube // 0
                1, 2, 11, 12 -> Materials(1, "基质(风)", "元素基质")
                3, 4, 13, 14 -> Materials(3, "基质(水)", "元素基质")
                5, 6, 15, 16 -> Materials(4, "基质(盐)", "元素基质")
                7, 8, 17, 18 -> Materials(7, "基质(火)", "元素基质")
                else -> Materials(9, "基质(土)", "元素基质")
            }
            else -> when (itemID % 100) { // -999..-500 or -99..-21 or 21..99 or 500..999
                5, 10, 25, 35, 55, 65, 85, 95 -> Materials(5, "硼", "殊剂")
                6, 12, 18, 24, 36, 54, 66, 72 -> Materials(6, "碳", "殊剂")
                14, 28, 56, 98 -> Materials(14, "硅", "殊剂")
                16, 32, 64 -> Materials(16, "硫", "殊剂")
                29, 58, 87 -> Materials(29, "铜", "原体")
                26, 52 -> Materials(26, "铁", "原体")
                33, 99 -> Materials(33, "砷", "触媒")
                15 -> Materials(15, "磷", "殊剂")
                20 -> Materials(20, "钙", "殊剂")
                50 -> Materials(50, "锡", "原体")
                82 -> Materials(82, "铅", "原体")
                80 -> Materials(80, "汞", "银种")
                41 -> Materials(41, "银", "闪耀着银色的光芒")
                79 -> Materials(79, "金", "是金子,可惜你没有铸币权(可以兑换成金币)")
                else -> otherPotionMaterials(itemID)
            }
        }
    }

    /** 炼金原料命名 */
    private fun otherPotionMaterials(itemID: Int): Sundries {
        return when {
            itemID % 3 == 0 || itemID % 5 == 0 || itemID % 7 == 0 ->
                if (itemID > 0)
                    Materials(21, "生命之盐", "古典派炼金术认为这是炼出贤者之石需要的关键媒介")
                else
                    Materials(-21, "虚无之盐", "古典派炼金术认为这是炼出贤者之石需要的关键媒介")
            itemID % 11 == 0 -> Materials(22, "黑化结晶", "黑-白-黄-赤,你迈出了关键的第一步")
            itemID % 13 == 0 -> Materials(-26, "月之盐", "白化结晶的衍生品")
            itemID % 17 == 0 -> Materials(34, "白化结晶", "黑-白-黄-赤,你感觉节奏没错,第二步已经实现")
            itemID % 19 == 0 -> Materials(-38, "日之盐", "黄化结晶的衍生品")
            itemID % 23 == 0 -> Materials(23, "黄化结晶", "黑-白-黄-赤,还差一点点")
            itemID % 29 == 0 -> Materials(-58, "赤化结晶", "黑-白-黄-赤,这是炼成贤者之石的必要原料")
            itemID % 31 == 0 -> Materials(31, "贤者之石", "古典派炼金术的终极目标,是世间终极奥义的钥匙")
            itemID in 600..700 -> Materials(itemID, "炼金药剂$itemID", "不知名的混合液体")
            itemID in -700..-600 -> Materials(itemID, "炼金药剂$itemID", "不知名的混合液体")
            else -> return Waste
        }
    }

    private fun deName(itemName: String): Item? {
        return when {
            itemName.contains("药水") -> dePotionName(itemName)
            itemName.contains("药剂") -> deMaterialsName(itemName.split("", limit = 4)[3])
            else -> null
        }
    }

    /** 药水名字解析 */
    private fun dePotionName(itemName: String, flag: Boolean = false): Potion? {
        val charL = itemName.split("")
        if (charL.size <= 2) return null
        var potionID = when (charL.slice(0..2).fold("") { str, v -> str + v }) {
            "初级" -> 100
            "中级" -> 200
            "高级" -> 300
            "特级" -> 400
            "殁性" -> return dePotionName(charL.slice(3 until charL.size).fold("") { str, v -> str + v }, true)
            else -> 0
        }
        if (charL.size <= 7) return null
        potionID += when (charL.slice(3..6).fold("") { str, v -> str + v }) {
            "HP药水" -> 0
            "MP药水" -> 4
            "全能药水" -> 8
            else -> return null
        }

        potionID += when (charL.slice(7 until charL.size).fold("") { str, v -> str + v }) {
            "(小)" -> 0
            "" -> 30
            "(大)" -> 60
            else -> 0
        }
        when (potionID) {
            0 -> return null
            400, 404, 408,
            460, 464, 468 -> return null
        }
        return if (flag) HarmPotion(-potionID, "殁性$itemName", "可以喝的药水,散发着死亡的气息")
        else TreatmentPotion(potionID, itemName, "可以喝的药水,散发着生命的气息")
    }

    /** 药剂名字解析 */
    private fun deMaterialsName(materialsID: String): Materials? {
        val itemID = materialsID.toIntOrNull() ?: return null
        return Materials(itemID, "药剂$itemID", "不知名的混合液体")
    }
}