package anyang.mypackages.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "packages", indices = [Index(value = ["pickupCode"], unique = true)])
data class PackageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pickupCode: String,
    val lockerNumber: String,
    val location: String,
    val messageContent: String,
    val receiveTime: Long,
    val status: PackageStatus = PackageStatus.PENDING,
    val pickupTime: Long? = null
)

enum class PackageStatus {
    PENDING,
    PICKED_UP
}
