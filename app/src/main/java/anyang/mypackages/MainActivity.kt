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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SystemGroupedBackground,
        topBar = {
            IosHeader(
                pendingCount = pendingCount,
                isLoading = isLoading,
                onRefresh = { viewModel.syncSms() },
                confirmBeforeSwipe = confirmBeforeSwipe,
                onToggleConfirm = { viewModel.toggleConfirmBeforeSwipe() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                searchText = searchText,
                onSearchChange = { viewModel.searchText.value = it },
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 6.dp)
            )

            IosSegmentedControl(
                currentFilter = filterStatus,
                onFilterChange = { viewModel.updateFilterStatus(it) },
                counts = Triple(totalCount, pendingCount, pickedUpCount),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            PackageList(
                packages = filteredPackages,
                isLoading = isLoading,
                confirmBeforeSwipe = confirmBeforeSwipe,
                onRefresh = { viewModel.syncSms() },
                onPickedUp = { viewModel.markAsPickedUp(it) },
                onResetToPending = { viewModel.resetToPending(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosHeader(
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
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val greeting = when {
        pendingCount == 0 -> "全部取完，轻松一天！"
        pendingCount == 1 -> "还有 1 个快递待取"
        else -> "还有 ${pendingCount} 个快递待取"
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "${getTimeGreeting()}，",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecondaryLabel
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Label
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Switch(
                    checked = confirmBeforeSwipe,
                    onCheckedChange = { onToggleConfirm() },
                    modifier = Modifier.scale(0.65f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SystemGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SystemGray4
                    )
                )

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = SystemBlue,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (isLoading) rotation else 0f)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SystemGroupedBackground,
            scrolledContainerColor = SystemGroupedBackground
        )
    )
}

@Composable
fun IosSegmentedControl(
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FillQuaternary)
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            filters.forEach { (filter, label, count) ->
                val isSelected = currentFilter == filter

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isSelected) SystemBackground else Color.Transparent
                        )
                        .then(
                            if (isSelected) Modifier else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onFilterChange(filter) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Label else SecondaryLabel,
                            fontSize = if (isSelected) 13.sp else 13.sp
                        )
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) SecondaryLabel else TertiaryLabel,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
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
