package anyang.mypackages.sms

import anyang.mypackages.data.PackageEntity
import anyang.mypackages.data.PackageStatus
import java.util.regex.Pattern

class SmsParser {
    companion object {
        // 通用取件码：取件码/提货码/取货码 + 5位以上连续数字
        private val PICKUP_CODE_PATTERN = Pattern.compile("(?:取件码|提货码|取货码)\\s*(\\d{5,})")
        // 柜号：N号柜 或 N号快递柜
        private val LOCKER_PATTERN = Pattern.compile("(\\d+)号(快递柜|柜)")
        // 位置：已到/到 ... ，/。/请
        private val LOCATION_PATTERN = Pattern.compile("(?:已到|到)([^，。]+?)(?:，|。|请)")
        // 发件方：【XXX】
        private val SENDER_PATTERN = Pattern.compile("【([^】]+)】")

        // 递管家专用：提货码后可能含 em-dash（—）的数字串，如 "XXXX—XXXX"
        private val DIGUANJIA_CODE_PATTERN = Pattern.compile("提货码\\s*([\\d—]+)")

        // 菜鸟驿站专用：凭 X-X-XXXX 到 ... 取件
        private val CAINIAO_CODE_PATTERN = Pattern.compile("凭\\s*([\\d-]+)\\s*到")
    }

    fun parseSms(message: String, timestamp: Long): PackageEntity? {
        val platform = identifyPlatform(message)

        return when (platform) {
            "菜鸟驿站" -> parseCainiao(message, timestamp)
            else -> parseStandard(message, timestamp, platform)
        }
    }

    /**
     * 识别快递平台
     * 优先从【发件方】提取，其次通过关键词匹配
     */
    fun identifyPlatform(message: String): String? {
        val matcher = SENDER_PATTERN.matcher(message)
        if (matcher.find()) {
            val sender = matcher.group(1)?.trim() ?: ""
            if (sender.contains("菜鸟") || sender.contains("驿站")) return "菜鸟驿站"
            if (sender.contains("顺丰")) return "顺丰速运"
            if (sender.contains("递管家")) return "递管家"
            if (sender.isNotBlank()) return sender
        }
        // 降级：关键词检测（部分短信可能没有【】包裹的发件方）
        if (message.contains("菜鸟驿站") || message.contains("菜鸟")) return "菜鸟驿站"
        if (message.contains("递管家")) return "递管家"
        if (message.contains("顺丰")) return "顺丰速运"
        return null
    }

    /**
     * 解析菜鸟驿站短信
     * 格式示例：【菜鸟驿站】您的包裹已到站，凭6-4-1007到XX市XX区XX店取件。
     */
    private fun parseCainiao(message: String, timestamp: Long): PackageEntity? {
        val codeMatcher = CAINIAO_CODE_PATTERN.matcher(message)
        val code = if (codeMatcher.find()) codeMatcher.group(1) else return null

        val location = extractCainiaoLocation(message) ?: "未知位置"

        return PackageEntity(
            pickupCode = code,
            lockerNumber = "驿站",
            location = location,
            messageContent = message,
            receiveTime = timestamp,
            status = PackageStatus.PENDING,
            platform = "菜鸟驿站"
        )
    }

    /**
     * 解析通用/递管家/顺丰快递柜短信
     */
    private fun parseStandard(message: String, timestamp: Long, platform: String?): PackageEntity? {
        // 递管家使用专用正则（支持 em-dash 取件码）
        val pickupCode = if (platform == "递管家") {
            extractDiguanjiaCode(message)
        } else {
            extractPickupCode(message)
        } ?: return null

        val lockerNumber = extractLockerNumber(message) ?: return null
        val location = extractLocation(message) ?: "未知位置"

        return PackageEntity(
            pickupCode = pickupCode,
            lockerNumber = lockerNumber,
            location = location,
            messageContent = message,
            receiveTime = timestamp,
            status = PackageStatus.PENDING,
            platform = platform
        )
    }

    // --- 提取方法 ---

    private fun extractPickupCode(message: String): String? {
        val matcher = PICKUP_CODE_PATTERN.matcher(message)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * 递管家取件码：提货码XXXX—XXXX -> "XXXX-XXXX"（em-dash 标准化为短横线）
     */
    private fun extractDiguanjiaCode(message: String): String? {
        val matcher = DIGUANJIA_CODE_PATTERN.matcher(message)
        return if (matcher.find()) {
            matcher.group(1)?.replace('—', '-')
        } else null
    }

    private fun extractLockerNumber(message: String): String? {
        val matcher = LOCKER_PATTERN.matcher(message)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractLocation(message: String): String? {
        val matcher = LOCATION_PATTERN.matcher(message)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * 菜鸟驿站位置提取：
     * 取件码已在 "凭...到" 中匹配，afterCodeIdx 之后就是位置文本（如 "XX市XX区XX店取件。"）
     * 截取"取件"之前的部分即可，不必再匹配 "到" 关键字
     */
    private fun extractCainiaoLocation(message: String): String? {
        val codeMatcher = CAINIAO_CODE_PATTERN.matcher(message)
        if (!codeMatcher.find()) return null
        val afterCodeIdx = codeMatcher.end()

        // 从取件码后面截取到 "取件"（不含）之前的内容
        val remaining = message.substring(afterCodeIdx)
        val location = remaining.replace(Regex("取件.*$"), "").trim()
        return location.ifBlank { null }
    }

    /**
     * 判断是否是快递相关短信
     */
    fun isPackageSms(message: String): Boolean {
        return PICKUP_CODE_PATTERN.matcher(message).find() ||
                DIGUANJIA_CODE_PATTERN.matcher(message).find() ||
                CAINIAO_CODE_PATTERN.matcher(message).find() ||
                message.contains("递管家") || message.contains("顺丰") ||
                message.contains("菜鸟") || message.contains("驿站") ||
                message.contains("取件码") || message.contains("提货码") ||
                message.contains("取货码") ||
                (message.contains("快递") && message.contains("到"))
    }
}
