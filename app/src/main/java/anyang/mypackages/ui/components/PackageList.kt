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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = CoolWhite,
            contentColor = ProfessionalBlue
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "float")
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = floatOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = LightBlue.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {}

                Icon(
                    imageVector = if (isLoading) Icons.Default.CheckCircle else Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = ProfessionalBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isLoading) "正在同步..." else "暂无快递",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isLoading) "正在获取最新数据" else "收到取件短信后会自动显示",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
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
                .alpha(if (isPickedUp) 0.75f else 1f)
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
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isPickedUp) 0.5.dp else 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isPickedUp) SuccessLight.copy(alpha = 0.4f) else LightBlue.copy(alpha = 0.25f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPickedUp) SuccessGreen.copy(alpha = 0.12f) else ProfessionalBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${packageItem.lockerNumber}号柜",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPickedUp) SuccessGreen else ProfessionalBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = packageItem.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    StatusBadge(isPickedUp = isPickedUp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "取件码",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = packageItem.pickupCode,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isPickedUp) TextTertiary else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (isPickedUp) TextDecoration.LineThrough else null
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "送达",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatDate(packageItem.receiveTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPickedUp && packageItem.pickupTime != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "已取 ${formatPickupTime(packageItem.pickupTime!!)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen
                                )
                            }
                        } else {
                            Text(
                                text = "长按或点击确认取件",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
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
                                    containerColor = ProfessionalBlue,
                                    contentColor = TextInverse
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "确认取件",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextInverse
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = ProfessionalBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "确认取件",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        text = "柜号：${packageItem.lockerNumber}号",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "取件码：${packageItem.pickupCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "位置：${packageItem.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPickedUp(packageItem)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfessionalBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("确认取件")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("取消", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardBackground
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            VibrantOrange.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VibrantOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "恢复未取件",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "确定要将柜号 ${packageItem.lockerNumber} 的快递恢复为未取件状态吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToPending(packageItem.id)
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantOrange,
                        contentColor = TextInverse
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "恢复",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "取消",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = CardBackground
        )
    }
}

@Composable
fun StatusBadge(isPickedUp: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isPickedUp) SuccessLight else OrangeLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isPickedUp) Icons.Default.CheckCircle else Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = if (isPickedUp) SuccessGreen else VibrantOrange,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isPickedUp) "已取" else "待取",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPickedUp) SuccessGreen else VibrantOrange,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }

    // 重置时分秒，只比较日期
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
