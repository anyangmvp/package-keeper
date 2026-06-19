package anyang.mypackages.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// Apple iOS 风格色彩系统
// 基于 iOS Human Interface Guidelines
// 简洁、优雅、清新的视觉体验
// ============================================

// iOS 系统色彩
val SystemBlue = Color(0xFF007AFF)
val SystemGreen = Color(0xFF34C759)
val SystemOrange = Color(0xFFFF9500)
val SystemRed = Color(0xFFFF3B30)
val SystemPink = Color(0xFFFF2D55)
val SystemPurple = Color(0xFFAF52DE)
val SystemTeal = Color(0xFF5AC8FA)
val SystemIndigo = Color(0xFF5856D6)

// iOS 系统灰色体系
val SystemGray = Color(0xFF8E8E93)        // 次要文字
val SystemGray2 = Color(0xFFAEAEB2)      // 三级文字
val SystemGray3 = Color(0xFFC7C7CC)      // 四级文字
val SystemGray4 = Color(0xFFD1D1D6)      // 占位文字
val SystemGray5 = Color(0xFFE5E5EA)      // 浅色分隔线
val SystemGray6 = Color(0xFFF2F2F7)      // 分组背景

// iOS 背景色
val SystemBackground = Color(0xFFFFFFFF)
val SystemGroupedBackground = Color(0xFFF2F2F7)
val SecondarySystemBackground = Color(0xFFF2F2F7)
val TertiarySystemBackground = Color(0xFFFFFFFF)

// iOS 文字颜色
val Label = Color(0xFF000000)            // 主要文字
val SecondaryLabel = Color(0xFF3C3C43).copy(alpha = 0.6f)
val TertiaryLabel = Color(0xFF3C3C43).copy(alpha = 0.3f)
val QuaternaryLabel = Color(0xFF3C3C43).copy(alpha = 0.18f)
val PlaceholderText = Color(0xFF3C3C43).copy(alpha = 0.3f)

// iOS 分隔线
val Separator = Color(0xFF3C3C43).copy(alpha = 0.29f)
val OpaqueSeparator = Color(0xFFC6C6C8)

// 卡片与表面
val CardSurface = Color(0xFFFFFFFF)
val CardShadow = Color(0x0A000000)

// 填充色（浅色背景容器）
val FillPrimary = Color(0xFF787880).copy(alpha = 0.2f)
val FillSecondary = Color(0xFF787880).copy(alpha = 0.16f)
val FillTertiary = Color(0xFF767680).copy(alpha = 0.12f)
val FillQuaternary = Color(0xFF747480).copy(alpha = 0.08f)

// 状态色浅底
val BlueLight = Color(0xFFE3F2FF)
val GreenLight = Color(0xFFE8F8ED)
val OrangeLight = Color(0xFFFFF3E0)
val RedLight = Color(0xFFFFEBEE)

// 兼容旧代码的别名（逐步迁移）
val CoolWhite = SystemGroupedBackground
val SoftGray = FillSecondary
val CardBackground = CardSurface
val CardBorder = OpaqueSeparator
val TextPrimary = Label
val TextSecondary = SecondaryLabel
val TextTertiary = TertiaryLabel
val TextInverse = Color.White

val ProfessionalBlue = SystemBlue
val DeepBlue = Color(0xFF0051D5)
val LightBlue = BlueLight
val VibrantOrange = SystemOrange
val SuccessGreen = SystemGreen
val ErrorRed = SystemRed
val SuccessLight = GreenLight
val ModernTeal = SystemTeal
val TealLight = Color(0xFFE0F7FF)

val StatusPending = SystemOrange
val StatusPickedUp = SystemGreen

val WarningYellow = SystemOrange
val WarningLight = OrangeLight
val ErrorLight = RedLight

val ShadowLight = Color(0x05000000)
val ShadowMedium = Color(0x0A000000)

val HeaderGradientStart = SystemBlue
val HeaderGradientEnd = SystemBlue
val CardGradientTeal = SystemTeal
