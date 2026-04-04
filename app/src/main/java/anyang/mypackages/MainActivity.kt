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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import anyang.mypackages.ui.components.PackageList
import anyang.mypackages.ui.components.SearchBar
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
                PackageScreen(viewModel)
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
fun PackageScreen(viewModel: PackageViewModel) {
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
            .background(CoolWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Header(
                pendingCount = pendingCount,
                isLoading = isLoading,
                onRefresh = { viewModel.syncSms() },
                confirmBeforeSwipe = confirmBeforeSwipe,
                onToggleConfirm = { viewModel.toggleConfirmBeforeSwipe() }
            )

            SearchBar(
                searchText = searchText,
                onSearchChange = { viewModel.searchText.value = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            FilterTabs(
                currentFilter = filterStatus,
                onFilterChange = { viewModel.updateFilterStatus(it) },
                counts = Triple(totalCount, pendingCount, pickedUpCount),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            PackageList(
                packages = filteredPackages,
                isLoading = isLoading,
                confirmBeforeSwipe = confirmBeforeSwipe,
                onRefresh = { viewModel.syncSms() },
                onPickedUp = { viewModel.markAsPickedUp(it) },
                onResetToPending = { viewModel.resetToPending(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun Header(
    pendingCount: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    confirmBeforeSwipe: Boolean,
    onToggleConfirm: () -> Unit
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

    val greeting = when {
        pendingCount == 0 -> "全部取完，轻松一天！"
        pendingCount == 1 -> "还有1个快递待取"
        pendingCount <= 3 -> "有${pendingCount}个快递待取"
        else -> "有${pendingCount}个快递待取"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HeaderGradientStart, HeaderGradientEnd)
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inbox,
                        contentDescription = null,
                        tint = TextInverse.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${getTimeGreeting()}！",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextInverse,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextInverse.copy(alpha = 0.85f)
                )
            }

            var showSettingsMenu by remember { mutableStateOf(false) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    FilledIconButton(
                        onClick = { showSettingsMenu = !showSettingsMenu },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showSettingsMenu) ProfessionalBlue else Color.White,
                            contentColor = if (showSettingsMenu) TextInverse else ProfessionalBlue
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("确认提示")
                                    Switch(
                                        checked = confirmBeforeSwipe,
                                        onCheckedChange = { onToggleConfirm() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = TextInverse,
                                            checkedTrackColor = ProfessionalBlue,
                                            uncheckedThumbColor = TextTertiary,
                                            uncheckedTrackColor = LightGray
                                        )
                                    )
                                }
                            },
                            onClick = { },
                            enabled = false
                        )
                    }
                }

                FilledIconButton(
                    onClick = onRefresh,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = ProfessionalBlue
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (isLoading) rotation else 0f)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTabs(
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label, count) ->
            val isSelected = currentFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(filter) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) TextInverse else TextSecondary
                        )
                        if (count > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TextInverse.copy(alpha = 0.25f) else ProfessionalBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) TextInverse else ProfessionalBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ProfessionalBlue,
                    selectedLabelColor = TextInverse,
                    containerColor = CardBackground,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = CardBorder,
                    selectedBorderColor = ProfessionalBlue,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SettingsFab(
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "设置选项",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "确认提示",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = confirmBeforeSwipe,
                            onCheckedChange = { onToggleConfirm() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextInverse,
                                checkedTrackColor = ProfessionalBlue,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = LightGray
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "取件前弹出确认对话框",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showMenu = !showMenu },
            shape = CircleShape,
            containerColor = if (showMenu) ProfessionalBlue else CardBackground,
            contentColor = if (showMenu) TextInverse else ProfessionalBlue,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
        ) {
            Icon(
                imageVector = if (showMenu) Icons.Default.CheckCircle else Icons.Default.Settings,
                contentDescription = "设置",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun getTimeGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 6 -> "凌晨好"
        hour < 11 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        hour < 22 -> "晚上好"
        else -> "夜深了"
    }
}
