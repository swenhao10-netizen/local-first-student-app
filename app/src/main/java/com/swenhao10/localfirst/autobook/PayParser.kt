package com.swenhao10.localfirst.autobook

/** 一笔从付款通知里认出来的支出。 */
data class Payment(val amountCents: Long, val merchant: String?, val source: String, val category: String)

/**
 * 从微信、支付宝、云闪付的付款通知里认金额和商家。
 * 只认「付出去的钱」：退款、收款、到账、红包、转账一律不记，免得乱账。
 * 通知文案会随 App 版本变，认不出就什么都不做。
 */
object PayParser {
    val SOURCES = mapOf(
        "com.tencent.mm" to "微信支付",
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.unionpay" to "云闪付"
    )

    private val PAY_WORDS = Regex("支付|付款|消费|支出|扣款|已付")
    private val SKIP_WORDS = Regex("退款|收款(?!方|商家|商户)|到账|红包|转账|收入|入账|还款|充值成功|待支付|未支付|支付失败")
    /** 广告尾巴（「点击领取 88 元红包」之类）先剪掉，免得把真付款当成红包跳过、或把 88 元当金额。 */
    private val PROMO = Regex("领取|立减|优惠券|积分|抽奖|点击|活动")
    private val AMOUNT = listOf(
        Regex("[¥￥]\\s*([0-9]{1,7}(?:\\.[0-9]{1,2})?)"),
        Regex("([0-9]{1,7}(?:\\.[0-9]{1,2})?)\\s*元")
    )
    private val MERCHANT = listOf(
        Regex("(?:收款方|商户名称|商户|商家|收款商家)[:：\\s]*([^\\s,，。;；¥￥:：]{2,24})"),
        Regex("(?:在|向)([^\\s,，。;；¥￥]{2,20}?)(?:消费|付款|支付)")
    )

    private val CATEGORY_WORDS = linkedMapOf(
        "餐饮" to listOf("餐", "饭", "面", "粉", "咖啡", "瑞幸", "星巴克", "奶茶", "蜜雪", "喜茶", "茶百道", "古茗", "肯德基", "麦当劳", "汉堡", "美团", "饿了么", "食堂", "烧烤", "火锅", "小吃", "面包", "外卖", "便利店", "全家", "罗森", "7-11", "饮"),
        "交通" to listOf("地铁", "公交", "滴滴", "高德", "打车", "出行", "铁路", "12306", "航空", "机场", "加油", "停车", "单车", "哈啰", "青桔", "地铁"),
        "学习" to listOf("书店", "教育", "课程", "文具", "打印", "图书", "网课", "学习"),
        "娱乐" to listOf("影城", "电影", "游戏", "网吧", "KTV", "音乐", "视频会员", "腾讯视频", "爱奇艺", "B站", "哔哩哔哩", "网易云", "Steam"),
        "购物" to listOf("淘宝", "天猫", "京东", "拼多多", "超市", "商城", "旗舰店", "专卖", "优衣库", "名创")
    )

    fun categoryFor(merchant: String?, text: String): String {
        val hay = (merchant ?: "") + " " + text
        for ((cat, words) in CATEGORY_WORDS) if (words.any { hay.contains(it, ignoreCase = true) }) return cat
        return "其他"
    }

    fun parse(pkg: String, title: String?, text: String?): Payment? {
        val source = SOURCES[pkg] ?: return null
        val all = listOfNotNull(title, text).joinToString(" ").replace('\n', ' ')
            .split('，', ',', '。', '；', ';', '！', '!')
            .filterNot { PROMO.containsMatchIn(it) }
            .joinToString(" ")
        if (all.isBlank()) return null
        if (SKIP_WORDS.containsMatchIn(all)) return null
        if (!PAY_WORDS.containsMatchIn(all)) return null
        // 微信里聊天消息也会带「支付」二字：要求标题本身是支付类服务号，或正文有明确金额符号
        if (pkg == "com.tencent.mm" && title?.contains("微信支付") != true && !all.contains("¥") && !all.contains("￥")) return null
        val amount = AMOUNT.firstNotNullOfOrNull { it.find(all)?.groupValues?.get(1) }?.toBigDecimalOrNull() ?: return null
        val cents = amount.movePointRight(2).toLong()
        if (cents <= 0 || cents > 10_000_000) return null
        val merchant = MERCHANT.firstNotNullOfOrNull { it.find(all)?.groupValues?.get(1)?.trim() }
        return Payment(cents, merchant, source, categoryFor(merchant, all))
    }
}
