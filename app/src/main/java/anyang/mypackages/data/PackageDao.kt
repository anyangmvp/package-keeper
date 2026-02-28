package anyang.mypackages.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages ORDER BY receiveTime DESC")
    fun getAllPackages(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE status = 'PENDING' ORDER BY receiveTime DESC")
    fun getPendingPackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(packageItem: PackageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)

    @Update
    suspend fun updatePackage(packageItem: PackageEntity)

    @Query("SELECT * FROM packages WHERE pickupCode = :code LIMIT 1")
    suspend fun getPackageByPickupCode(code: String): PackageEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM packages WHERE pickupCode = :code)")
    suspend fun existsByPickupCode(code: String): Boolean
}
