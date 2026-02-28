package anyang.mypackages.data

import kotlinx.coroutines.flow.Flow

class PackageRepository(private val packageDao: PackageDao) {
    fun getAllPackages(): Flow<List<PackageEntity>> = packageDao.getAllPackages()

    fun getPendingPackages(): Flow<List<PackageEntity>> = packageDao.getPendingPackages()

    suspend fun insertPackage(packageItem: PackageEntity): Long {
        try {
            return packageDao.insertPackage(packageItem)
        } catch (e: Exception) {
            // 如果因为唯一约束冲突导致插入失败，检查现有包裹并保留其状态
            val existing = packageDao.getPackageByPickupCode(packageItem.pickupCode)
            if (existing != null) {
                // 保留现有包裹的状态和取件时间，只更新其他字段
                val merged = packageItem.copy(
                    id = existing.id,
                    status = existing.status,
                    pickupTime = existing.pickupTime
                )
                packageDao.updatePackage(merged)
                return merged.id
            }
            return -1
        }
    }

    suspend fun insertPackages(packages: List<PackageEntity>) {
        packageDao.insertPackages(packages)
    }

    suspend fun updatePackage(packageItem: PackageEntity) {
        packageDao.updatePackage(packageItem)
    }

    suspend fun getPackageByPickupCode(code: String): PackageEntity? {
        return packageDao.getPackageByPickupCode(code)
    }

    suspend fun existsByPickupCode(code: String): Boolean {
        return packageDao.existsByPickupCode(code)
    }
}
