package com.example.projecthub.ui.theme

import androidx.compose.ui.graphics.Color

object ProjectHubColors {
    val Accent = Color(0xFF1E6571)
    val AccentLight = Color(0xFF6ACBC8)
    val AccentSoft = Color(0xFF48B765)

    val Success = Color(0xFF22C55E)
    val SuccessDark = Color(0xFF16A34A)
    val Warning = Color(0xFFF97316)
    val Danger = Color(0xFFEF4444)
    val DangerDark = Color(0xFFDC2626)
    val Info = Color(0xFF2563EB)
    val InfoLight = Color(0xFF3B82F6)

    private val LightBackgroundBase = Color(0xFFE8F9FC)
    private val LightSurfaceBase = Color(0xFFFFFFFF)
    private val LightInkBase = Color(0xFF102A35)
    private val InkBase = Color(0xFF111827)
    private val MutedBase = Color(0xFF6B7280)
    private val SlateBase = Color(0xFF334155)
    private val SlateMutedBase = Color(0xFF475569)
    private val BorderBase = Color(0xFFD1D5DB)
    private val BorderSoftBase = Color(0xFFCBD5E1)
    private val SurfaceSoftBase = Color(0xFFF8FAFC)
    private val SurfaceSubtleBase = Color(0xFFFAFBFC)
    private val DisabledBase = Color(0xFFE5E7EB)
    private val DisabledSoftBase = Color(0xFFF3F4F6)

    val DarkBackground = Color(0xFF071118)
    val DarkSurface = Color(0xFF101C25)
    val DarkSurfaceSoft = Color(0xFF162631)
    val DarkInk = Color(0xFFF4FAFB)
    val DarkMuted = Color(0xFFC5D5DC)
    val DarkBorder = Color(0xFF294251)

    var LightBackground = LightBackgroundBase
        private set
    var LightSurface = LightSurfaceBase
        private set
    var LightInk = LightInkBase
        private set
    var Ink = InkBase
        private set
    var Muted = MutedBase
        private set
    var Slate = SlateBase
        private set
    var SlateMuted = SlateMutedBase
        private set

    var SidebarBackground = Color(0xFF0F1724)
        private set
    var SidebarSelected = Color(0xFF1E293B)
        private set
    var SidebarMutedText = Color(0xFF94A3B8)
        private set
    var HeaderBackground = Accent
        private set
    var HeaderContent = Color.White
        private set

    var Border = BorderBase
        private set
    var BorderSoft = BorderSoftBase
        private set
    var SurfaceSoft = SurfaceSoftBase
        private set
    var SurfaceSubtle = SurfaceSubtleBase
        private set
    var Disabled = DisabledBase
        private set
    var DisabledSoft = DisabledSoftBase
        private set
    val Rating = Color(0xFFF59E0B)

    fun applyTheme(darkTheme: Boolean) {
        if (darkTheme) {
            LightBackground = DarkBackground
            LightSurface = DarkSurface
            LightInk = DarkInk
            Ink = DarkInk
            Muted = DarkMuted
            Slate = Color(0xFFE0EEF2)
            SlateMuted = DarkMuted
            SidebarBackground = Color(0xFF050B11)
            SidebarSelected = Color(0xFF123343)
            SidebarMutedText = Color(0xFFBDD0D8)
            HeaderBackground = SidebarBackground
            HeaderContent = Color.White
            Border = DarkBorder
            BorderSoft = Color(0xFF355566)
            SurfaceSoft = DarkSurfaceSoft
            SurfaceSubtle = Color(0xFF13222C)
            Disabled = Color(0xFF263945)
            DisabledSoft = Color(0xFF1B2B35)
        } else {
            LightBackground = LightBackgroundBase
            LightSurface = LightSurfaceBase
            LightInk = LightInkBase
            Ink = InkBase
            Muted = MutedBase
            Slate = SlateBase
            SlateMuted = SlateMutedBase
            SidebarBackground = Color(0xFF0F1724)
            SidebarSelected = Color(0xFF1E293B)
            SidebarMutedText = Color(0xFF94A3B8)
            HeaderBackground = Accent
            HeaderContent = Color.White
            Border = BorderBase
            BorderSoft = BorderSoftBase
            SurfaceSoft = SurfaceSoftBase
            SurfaceSubtle = SurfaceSubtleBase
            Disabled = DisabledBase
            DisabledSoft = DisabledSoftBase
        }
    }
}

val Teal80 = ProjectHubColors.AccentLight
val Mint80 = Color(0xFFA7F3D0)
val Cyan80 = Color(0xFFBAE6FD)
val DarkBackground = ProjectHubColors.DarkBackground
val DarkSurface = ProjectHubColors.DarkSurface
val DarkInk = ProjectHubColors.DarkInk

val Teal40 = ProjectHubColors.Accent
val Mint40 = ProjectHubColors.SuccessDark
val Cyan40 = Color(0xFF0E7490)
val LightBackground = ProjectHubColors.LightBackground
val LightSurface = ProjectHubColors.LightSurface
val LightInk = ProjectHubColors.LightInk
