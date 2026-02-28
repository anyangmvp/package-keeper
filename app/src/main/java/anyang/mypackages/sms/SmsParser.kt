package anyang.mypackages.sms

import anyang.mypackages.data.PackageEntity
import anyang.mypackages.data.PackageStatus
import java.util.regex.Pattern

class SmsParser {
    companion object {
        private val PICKUP_CODE_PATTERN = Pattern.compile("取件码\\s*(\\d{5,})")
        private val LOCKER_PATTERN = Pattern.compile("(\\d+)号(快递柜|柜)")
        private val LOCATION_PATTERN = Pattern.compile("(?:已到|在)([^，。]+?)(?:，|。|请)")
        private val SENDER_PATTERN = Pattern.compile("【([^】]+)】")
    }

    fun parseSms(message: String, timestamp: Long): PackageEntity? {
        val pickupCode = extractPickupCode(message) ?: return null
        val lockerNumber = extractLockerNumber(message) ?: return null
        val location = extractLocation(message) ?: "未知位置"

        return PackageEntity(
            pickupCode = pickupCode,
            lockerNumber = lockerNumber,
            location = location,
            messageContent = message,
            receiveTime = timestamp,
            status = PackageStatus.PENDING
        )
    }

    private fun extractPickupCode(message: String): String? {
        val matcher = PICKUP_CODE_PATTERN.matcher(message)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    private fun extractLockerNumber(message: String): String? {
        val matcher = LOCKER_PATTERN.matcher(message)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    private fun extractLocation(message: String): String? {
        val matcher = LOCATION_PATTERN.matcher(message)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    fun isPackageSms(message: String): Boolean {
        return PICKUP_CODE_PATTERN.matcher(message).find() ||
                message.contains("递管家") || message.contains("取件码") ||
               (message.contains("快递") && message.contains("到"))
    }
}
