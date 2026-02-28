package anyang.mypackages.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import anyang.mypackages.data.PackageDatabase
import anyang.mypackages.data.PackageEntity
import anyang.mypackages.data.PackageStatus
import anyang.mypackages.sms.SmsReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PackageViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PackageDatabase.getDatabase(application)
    private val repository = anyang.mypackages.data.PackageRepository(database.packageDao())
    private val smsReader = SmsReader(application)

    val packages = repository.getAllPackages()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)

    val searchText = mutableStateOf("")
    val filterStatus = mutableStateOf<FilterStatus>(FilterStatus.PENDING)

    // 确认提示开关
    val confirmBeforeSwipe = mutableStateOf(true)

    init {
        syncSms()
    }

    fun syncSms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val smsPackages = smsReader.readSmsMessages()
                
                // 根据取件码去重，避免重复处理
                val uniquePackages = smsPackages.distinctBy { it.pickupCode }
                
                // 只添加新的取件码，跳过已存在的
                uniquePackages.forEach { pkg ->
                    if (!repository.existsByPickupCode(pkg.pickupCode)) {
                        repository.insertPackage(pkg)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "同步短信失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            _isLoading.value = false
        }
    }

    fun markAsPickedUp(packageItem: PackageEntity) {
        viewModelScope.launch {
            val updated = packageItem.copy(
                status = PackageStatus.PICKED_UP,
                pickupTime = System.currentTimeMillis()
            )
            repository.updatePackage(updated)
        }
    }

    fun resetToPending(id: Long) {
        viewModelScope.launch {
            val pkg = packages.value.find { it.id == id }
            if (pkg != null) {
                val reset = pkg.copy(
                    status = PackageStatus.PENDING,
                    pickupTime = null
                )
                repository.updatePackage(reset)
            }
        }
    }
    
    fun updateFilterStatus(status: FilterStatus) {
        filterStatus.value = status
    }
    
    fun toggleConfirmBeforeSwipe() {
        confirmBeforeSwipe.value = !confirmBeforeSwipe.value
    }
}

enum class FilterStatus {
    ALL, PENDING, PICKED_UP
}
