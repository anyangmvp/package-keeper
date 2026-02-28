package anyang.mypackages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import anyang.mypackages.ui.components.WarmPackageList
import anyang.mypackages.ui.components.WarmSearchBar
import anyang.mypackages.ui.theme.*
import anyang.mypackages.viewmodel.FilterStatus
import anyang.mypackages.viewmodel.PackageViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by lazy { PackageViewModel(application) }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.syncSms() }

    private val notificationSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.syncSms() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermission()
        setContent {
            MyPackagesTheme {
                WarmPackageScreen(viewModel)
            }
        }
    }

    private fun checkAndRequestPermission() {
        val readSmsPermission = Manifest.permission.READ_SMS
        if (ContextCompat.checkSelfPermission(this, readSmsPermission) != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(readSmsPermission)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationSmsPermission = Manifest.permission.RECEIVE_SMS
            if (ContextCompat.checkSelfPermission(this, notificationSmsPermission) != PackageManager.PERMISSION_GRANTED) {
                notificationSmsPermissionLauncher.launch(notificationSmsPermission)
            }
        }
    }
}

@Composable
fun WarmPackageScreen(viewModel: PackageViewModel) {
    val packages by viewModel.packages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filterStatus by rememberUpdatedState(newValue = viewModel.filterStatus.value)
    val confirmBeforeSwipe by rememberUpdatedState(newValue = viewModel.confirmBeforeSwipe.value)
    val searchText by rememberUpdatedState(newValue = viewModel.searchText.value)

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.syncSms() }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncSms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pendingCount = packages.count { it.status == anyang.mypackages.data.PackageStatus.PENDING }
    val pickedUpCount = packages.count { it.status == anyang.mypackages.data.PackageStatus.PICKED_UP }
    val totalCount = packages.size

    val filteredPackages = packages.filter { pkg ->
        val matchesStatus = when (filterStatus) {
            FilterStatus.ALL -> true
            FilterStatus.PENDING -> pkg.status == anyang.mypackages.data.PackageStatus.PENDING
            FilterStatus.PICKED_UP -> pkg.status == anyang.mypackages.data.PackageStatus.PICKED_UP
        }
        val matchesSearch = searchText.isBlank() || 
            pkg.pickupCode.contains(searchText, ignoreCase = true) ||
            pkg.lockerNumber.contains(searchText, ignoreCase = true) ||
            pkg.location.contains(searchText, ignoreCase = true)
        matchesStatus && matchesSearch
    }.sortedByDescending { it.receiveTime }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 温馨头部
            WarmHeader(
                pendingCount = pendingCount,
                isLoading = isLoading,
                onRefresh = { viewModel.syncSms() }
            )

            // 搜索栏
            WarmSearchBar(
                searchText = searchText,
                onSearchChange = { viewModel.searchText.value = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 过滤标签
            WarmFilterTabs(
                currentFilter = filterStatus,
                onFilterChange = { viewModel.updateFilterStatus(it) },
                counts = Triple(totalCount, pendingCount, pickedUpCount),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 包裹列表
            WarmPackageList(
                packages = filteredPackages,
                isLoading = isLoading,
                confirmBeforeSwipe = confirmBeforeSwipe,
                onRefresh = { viewModel.syncSms() },
                onPickedUp = { viewModel.markAsPickedUp(it) },
                onResetToPending = { viewModel.resetToPending(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 设置按钮
        WarmSettingsButton(
            confirmBeforeSwipe = confirmBeforeSwipe,
            onToggleConfirm = { viewModel.toggleConfirmBeforeSwipe() },
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

/**
 * 辅助数据类
 */
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * 温馨头部 - 有惊喜感的问候
 */
@Composable
fun WarmHeader(
    pendingCount: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 根据待取件数量显示不同的问候语
    val greeting = when {
        pendingCount == 0 -> "今天没有包裹，轻松一天~ ☀️"
        pendingCount == 1 -> "有1个包裹在等你哦！🎁"
        pendingCount <= 3 -> "有${pendingCount}个包裹等你来取！🎉"
        else -> "哇！有${pendingCount}个包裹！📦"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SunrisePink.copy(alpha = 0.6f),
                        WarmCream
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${getTimeGreeting()}!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium
                    )
                }

                // 可爱的刷新按钮
                Surface(
                    onClick = onRefresh,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = HappyPink,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isLoading) rotation else 0f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 根据时间返回问候语
 */
fun getTimeGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 11 -> "早安"
        hour < 14 -> "午安"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }
}

/**
 * 温馨过滤标签
 */
@Composable
fun WarmFilterTabs(
    currentFilter: FilterStatus,
    onFilterChange: (FilterStatus) -> Unit,
    counts: Triple<Int, Int, Int>,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        Triple(FilterStatus.ALL, "全部", counts.first),
        Triple(FilterStatus.PENDING, "待取件", counts.second),
        Triple(FilterStatus.PICKED_UP, "已取件", counts.third)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        filters.forEach { (filter, label, count) ->
            val isSelected = currentFilter == filter
            WarmFilterChip(
                label = label,
                count = count,
                isSelected = isSelected,
                onClick = { onFilterChange(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 温馨过滤芯片 - 圆润可爱
 */
@Composable
fun WarmFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        HappyPink
    } else {
        Color.White
    }

    val contentColor = if (isSelected) {
        Color.White
    } else {
        TextDark
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (count > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color.White.copy(alpha = 0.3f) else HappyPink.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else HappyPink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 温馨设置按钮
 */
@Composable
fun WarmSettingsButton(
    confirmBeforeSwipe: Boolean,
    onToggleConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "确认提示",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark
                            )
                            Text(
                                text = "取件前弹出确认",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLight
                            )
                        }
                        WarmSwitch(
                            checked = confirmBeforeSwipe,
                            onCheckedChange = { onToggleConfirm() }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showMenu = !showMenu },
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = HappyPink,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 温馨开关
 */
@Composable
fun WarmSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(200),
        label = "position"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (checked) HappyPink else Color(0xFFE0E0E0)
            )
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(20.dp)
                .graphicsLayer {
                    translationX = thumbPosition * 22.dp.toPx()
                }
                .background(Color.White, CircleShape)
        )
    }
}
