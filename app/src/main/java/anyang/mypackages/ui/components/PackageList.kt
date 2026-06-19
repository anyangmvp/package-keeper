package anyang.mypackages.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import anyang.mypackages.data.PackageEntity
import anyang.mypackages.data.PackageStatus
import anyang.mypackages.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PackageList(
    packages: List<PackageEntity>,
    isLoading: Boolean,
    confirmBeforeSwipe: Boolean,
    onRefresh: () -> Unit,
    onPickedUp: (PackageEntity) -> Unit,
    onResetToPending: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        if (packages.isEmpty()) {
            EmptyState(isLoading = isLoading)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = packages,
                    key = { it.id }
                ) { packageItem ->
                    PackageItem(
                        packageItem = packageItem,
                        confirmBeforeSwipe = confirmBeforeSwipe,
                        onPickedUp = onPickedUp,
                        onResetToPending = onResetToPending
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = SystemBackground,
            contentColor = SystemBlue
        )
    }
}

@Composable
fun EmptyState(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "float")
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float"
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .offset(y = floatOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = BlueLight,
                    modifier = Modifier.fillMaxSize(),
                    shadowElevation = 0.dp
                ) {}

                Icon(
                    imageVector = if (isLoading) Icons.Default.CheckCircle else Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = SystemBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isLoading) "正在同步..." else "暂无快递",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Label,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isLoading) "正在获取最新数据" else "收到取件短信后会自动显示在此",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryLabel,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PackageItem(
    packageItem: PackageEntity,
    confirmBeforeSwipe: Boolean,
    onPickedUp: (PackageEntity) -> Unit,
    onResetToPending: (Long) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val isPickedUp = packageItem.status == PackageStatus.PICKED_UP

    LaunchedEffect(Unit) {
        delay(60)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(350)) +
                slideInVertically(tween(350)) { it / 4 },
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isPickedUp) 0.72f else 1f)
                .pointerInput(packageItem.status) {
                    detectTapGestures(
                        onLongPress = {
                            if (isPickedUp) {
                                showResetDialog = true
                            }
                        },
                        onTap = {
                            if (!isPickedUp) {
                                if (confirmBeforeSwipe) {
                                    showConfirmDialog = true
                                } else {
                                    onPickedUp(packageItem)
                                }
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SystemBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 顶部：平台 + 柜号 + 状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val platformLabel = packageItem.platform
                        if (!platformLabel.isNullOrBlank()) {
                            IosPlatformBadge(
                                platform = platformLabel,
                                isPickedUp = isPickedUp
                            )
                        }

                        val lockerLabel = if (packageItem.lockerNumber == "驿站") {
                            "驿站取件"
                        } else {
                            "${packageItem.lockerNumber}号柜"
                        }
                        Text(
                            text = lockerLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPickedUp) TertiaryLabel else SecondaryLabel,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    IosStatusBadge(isPickedUp = isPickedUp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 分隔线
                HorizontalDivider(
                    color = Separator,
                    thickness = 0.5.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 取件码
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "取件码",
                            style = MaterialTheme.typography.bodySmall,
                            color = TertiaryLabel,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = packageItem.pickupCode,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPickedUp) TertiaryLabel else Label,
                            textDecoration = if (isPickedUp) TextDecoration.LineThrough else null,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // 送达时间
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "送达",
                            style = MaterialTheme.typography.bodySmall,
                            color = TertiaryLabel,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formatDate(packageItem.receiveTime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryLabel,
                            fontSize = 12.sp
                        )
                    }
                }

                // 位置信息
                if (packageItem.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = packageItem.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 底部操作区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPickedUp && packageItem.pickupTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SystemGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "已取 ${formatPickupTime(packageItem.pickupTime!!)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SystemGreen,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Text(
                            text = "轻点取件，长按已取可恢复",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TertiaryLabel,
                            fontSize = 12.sp
                        )
                    }

                    if (!isPickedUp) {
                        Button(
                            onClick = {
                                if (confirmBeforeSwipe) {
                                    showConfirmDialog = true
                                } else {
                                    onPickedUp(packageItem)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SystemBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 7.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "确认取件",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // iOS 风格确认对话框
    if (showConfirmDialog) {
        val lockerText = if (packageItem.lockerNumber == "驿站") "驿站取件" else "${packageItem.lockerNumber}号柜"
        IosAlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "确认取件",
            message = buildString {
                if (!packageItem.platform.isNullOrBlank()) {
                    append("来源：${packageItem.platform}\n")
                }
                append("柜号：$lockerText\n")
                append("取件码：${packageItem.pickupCode}\n")
                append("位置：${packageItem.location}")
            },
            confirmText = "确认取件",
            onConfirm = {
                onPickedUp(packageItem)
                showConfirmDialog = false
            },
            confirmColor = SystemBlue,
            dismissText = "取消"
        )
    }

    // iOS 风格恢复对话框
    if (showResetDialog) {
        val lockerDesc = if (packageItem.lockerNumber == "驿站") "驿站" else "${packageItem.lockerNumber}号柜"
        IosAlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = "恢复未取件",
            message = "确定要将 ${lockerDesc} 的快递\n恢复为「未取件」状态吗？",
            confirmText = "恢复",
            onConfirm = {
                onResetToPending(packageItem.id)
                showResetDialog = false
            },
            confirmColor = SystemOrange,
            dismissText = "取消"
        )
    }
}

@Composable
private fun IosAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    confirmColor: Color,
    dismissText: String
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SystemBackground,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                // 内容
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryLabel,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))

                // 分隔线
                HorizontalDivider(color = Separator, thickness = 0.5.dp)

                // 按钮区域 - iOS 风格水平并排
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = dismissText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SystemBlue
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(Separator)
                    )

                    TextButton(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = confirmColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IosPlatformBadge(platform: String, isPickedUp: Boolean) {
    val (bgColor, textColor, dotColor) = when {
        platform.contains("菜鸟") -> Triple(OrangeLight, SystemOrange, SystemOrange)
        platform.contains("递管家") -> Triple(BlueLight, SystemBlue, SystemBlue)
        platform.contains("顺丰") -> Triple(GreenLight, SystemGreen, SystemGreen)
        else -> Triple(BlueLight, SystemBlue, SystemBlue)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isPickedUp) dotColor.copy(alpha = 0.4f) else dotColor)
        )
        Text(
            text = platform.replace("速运", ""),
            style = MaterialTheme.typography.bodySmall,
            color = if (isPickedUp) textColor.copy(alpha = 0.4f) else textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun IosStatusBadge(isPickedUp: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isPickedUp) Icons.Default.CheckCircle else Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = if (isPickedUp) SystemGreen else SystemOrange,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = if (isPickedUp) "已取" else "待取",
            style = MaterialTheme.typography.bodySmall,
            color = if (isPickedUp) SystemGreen else SystemOrange,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

fun formatDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }

    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)
    now.set(Calendar.MILLISECOND, 0)

    val targetDate = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = now.timeInMillis - targetDate.timeInMillis
    val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

    return when {
        diffDays == 0 -> "今天 $timeStr"
        diffDays == 1 -> "昨天 $timeStr"
        diffDays < 7 -> {
            val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
            "${weekdays[date.get(Calendar.DAY_OF_WEEK) - 1]} $timeStr"
        }
        else -> {
            val sdf = SimpleDateFormat("MM月dd日", Locale.getDefault())
            "${sdf.format(Date(timestamp))} $timeStr"
        }
    }
}

fun formatPickupTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
