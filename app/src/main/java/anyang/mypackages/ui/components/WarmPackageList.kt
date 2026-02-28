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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anyang.mypackages.R
import anyang.mypackages.data.PackageEntity
import anyang.mypackages.data.PackageStatus
import anyang.mypackages.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 温馨包裹列表
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarmPackageList(
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
            WarmEmptyState(isLoading = isLoading)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = packages,
                    key = { it.id }
                ) { packageItem ->
                    WarmPackageItem(
                        packageItem = packageItem,
                        confirmBeforeSwipe = confirmBeforeSwipe,
                        onPickedUp = onPickedUp,
                        onResetToPending = onResetToPending
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = HappyPink
        )
    }
}

/**
 * 温馨空状态 - 可爱的表情和提示
 */
@Composable
fun WarmEmptyState(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 可爱的动画表情
            val infiniteTransition = rememberInfiniteTransition(label = "bounce")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                // 柔和的背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = SunrisePink.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )

                Text(
                    text = if (isLoading) "📦" else "📭",
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isLoading) "正在查找..." else "还没有包裹呢",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextDark,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isLoading) "稍等一下下~" else "收到取件短信后会自动显示哦",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 温馨包裹卡片 - 像礼物一样
 */
@Composable
fun WarmPackageItem(
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
        delay(50)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + 
                expandVertically(animationSpec = tween(300)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isPickedUp) 0.7f else 1f)
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
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isPickedUp) 1.dp else 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 顶部装饰条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            if (isPickedUp) HappyGreen else getWarmLockerColor(packageItem.lockerNumber),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 4.dp)
                ) {
                    // 第一行：取件码 + 状态
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 柜号圆形徽章
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isPickedUp) HappyGreen.copy(alpha = 0.2f) 
                                        else getWarmLockerColor(packageItem.lockerNumber).copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = packageItem.lockerNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isPickedUp) HappyGreen else getWarmLockerColor(packageItem.lockerNumber),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "取件码",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLight
                                )
                                Text(
                                    text = packageItem.pickupCode,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if (isPickedUp) TextLight else TextDark,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (isPickedUp) TextDecoration.LineThrough else null
                                )
                            }
                        }

                        // 状态标签
                        WarmStatusBadge(isPickedUp = isPickedUp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 位置信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = packageItem.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 时间和操作提示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formatWarmDate(packageItem.receiveTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLight
                            )
                        }

                        if (!isPickedUp) {
                            // 取件提示
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HappyPink.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (confirmBeforeSwipe) "点击取件 🎁" else "点击领取 ✨",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HappyPink,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            // 已取件时间
                            if (packageItem.pickupTime != null) {
                                Text(
                                    text = "✓ 已取 ${formatWarmTime(packageItem.pickupTime!!)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HappyGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 确认对话框 - 温馨风格
    if (showConfirmDialog) {
        WarmAlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "🎉 确认取件",
            message = "柜号 ${packageItem.lockerNumber}，取件码 ${packageItem.pickupCode}",
            confirmText = "确认领取",
            dismissText = "再等等",
            onConfirm = {
                onPickedUp(packageItem)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false },
            accentColor = HappyPink,
            emoji = "🎁"
        )
    }

    // 重置对话框
    if (showResetDialog) {
        WarmAlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = "🤔 恢复未取件",
            message = "确定要恢复柜号 ${packageItem.lockerNumber} 的包裹吗？",
            confirmText = "恢复",
            dismissText = "取消",
            onConfirm = {
                onResetToPending(packageItem.id)
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false },
            accentColor = WarmOrange,
            isWarning = true,
            emoji = "↩️"
        )
    }
}

/**
 * 状态信息数据类
 */
data class StatusInfo(
    val backgroundColor: Color,
    val textColor: Color,
    val text: String,
    val emoji: String
)

/**
 * 温馨状态徽章
 */
@Composable
fun WarmStatusBadge(isPickedUp: Boolean) {
    val statusInfo = if (isPickedUp) {
        StatusInfo(HappyGreen.copy(alpha = 0.15f), HappyGreen, "已取件", "✓")
    } else {
        StatusInfo(HappyPink.copy(alpha = 0.15f), HappyPink, "待取件", "!")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = statusInfo.backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = statusInfo.emoji,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = statusInfo.text,
                style = MaterialTheme.typography.labelMedium,
                color = statusInfo.textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 温馨对话框
 */
@Composable
fun WarmAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color,
    emoji: String = "✨",
    isWarning: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextLight
                )
            ) {
                Text(dismissText)
            }
        }
    )
}

/**
 * 获取柜号对应颜色
 */
fun getWarmLockerColor(lockerNumber: String): Color {
    val number = lockerNumber.filter { it.isDigit() }.toIntOrNull() ?: 1
    return when (number % 6) {
        0 -> HappyPink
        1 -> HappyBlue
        2 -> HappyGreen
        3 -> HappyYellow
        4 -> HappyPurple
        5 -> WarmOrange
        else -> HappyPink
    }
}

/**
 * 格式化日期
 */
fun formatWarmDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    val diffDays = now.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR)
    
    return when {
        diffDays == 0 -> "今天"
        diffDays == 1 -> "昨天"
        diffDays < 7 -> {
            val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
            weekdays[date.get(Calendar.DAY_OF_WEEK) - 1]
        }
        else -> {
            val sdf = SimpleDateFormat("MM月dd日", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * 格式化时间
 */
fun formatWarmTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
