package org.listenbrainz.android.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/** Theme for the whole app. */
data class Theme(
    val background: Color,
    val onBackground: Color,
    val level1: Color,
    val level2: Color,
    val lbSignature: Color,
    val lbSignatureSecondary: Color,
    val lbSignatureInverse: Color,
    val onLbSignature: Color,
    val chipUnselected: Color,
    val chipSelected: Color,
    val dialogPositiveButtonEnabled: Color = Color(0xFF5DA855),
    val dialogPositiveButtonDisabled: Color = Color(0xFF9EB99C),
    val dialogNegativeButton: Color = Color(0xFF696658),
    val dialogNegativeButtonText: Color = Color.White,
    val text: Color,
    val listenText: Color,
    /** Used for stars.*/
    val golden: Color = Color(0xFFF9A825),
    val hint: Color
)

private val colorSchemeDark = Theme(
    background = app_bg_dark,
    onBackground = Color.White,
    level1 = app_bottom_nav_dark,
    level2 = Color(0xFF4E4E4E),
    lbSignature = Color(0xFF9AABD1),
    lbSignatureSecondary = lb_yellow,
    lbSignatureInverse = lb_orange,
    onLbSignature = Color.Black,
    chipUnselected = Color(0xFF1E1E1E),
    chipSelected = Color.Black,
    text = Color.White,
    listenText = Color.White,
    hint = Color(0xFF8C8C8C)
)

private val colorSchemeLight = Theme(
    background = app_bg_day,
    onBackground = Color.Black,
    level1 = app_bottom_nav_day,
    level2 = Color(0xFF1E1E1E),
    lbSignature = lb_purple,
    lbSignatureSecondary = lb_yellow,
    lbSignatureInverse = Color(0xFFE5743E),
    onLbSignature = Color.White,
    chipUnselected = Color.White,
    chipSelected = Color(0xFFB6B6B6),
    text = Color.Black,
    listenText = lb_purple,
    hint = Color(0xFF707070)
)

private lateinit var LocalColorScheme: ProvidableCompositionLocal<Theme>

private val DarkColorScheme = darkColorScheme(
    background = app_bg_dark,
    onBackground = app_bg_light,
    primary = app_bg_dark,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_dark,
    onTertiary = bp_color_primary_dark,
    inverseOnSurface = lb_orange,   // Reserved for progress indicators.

    surfaceTint = bp_lavender_dark,
    onSurface = Color.White,     // Text color (Which is ON surface/canvas)
)

private val LightColorScheme = lightColorScheme(
    background = app_bg_day,
    onBackground = app_bg_light,
    primary = app_bg_day,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_day,
    onTertiary = bp_color_primary_day,
    inverseOnSurface = lb_purple,   // Reserved for progress indicators.
    surfaceTint = bp_lavender_day,

    onSurface = Color.Black
)

private val RedColorScheme = darkColorScheme(
    background = yimRed,
    onBackground = yimYellow,
    surface = yimWhite,
    secondary = offWhite

)

private val YellowColorScheme = lightColorScheme(
    background = yimYellow,
    onBackground = yimRed,
    surface = yimWhite,
    secondary = offWhite
)

@Immutable
data class YimPaddings(
    val defaultPadding: Dp = 16.dp,
    val tinyPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val largePadding: Dp = 24.dp,
    val extraLargePadding: Dp = 32.dp
)
internal val LocalYimPaddings = staticCompositionLocalOf { YimPaddings() }


@Composable
fun YearInMusicTheme(
    redTheme: Boolean,
    systemUiController : SystemUiController = rememberSystemUiController(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (redTheme){
        true -> RedColorScheme
        else -> YellowColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
            val isDark = when (redTheme){
                true -> false
                else -> true
            }
            systemUiController.statusBarDarkContentEnabled = isDark
            systemUiController.navigationBarDarkContentEnabled = isDark
            systemUiController.setNavigationBarColor(color = colorScheme.background)
        }
    }
    CompositionLocalProvider {
        LocalYimPaddings provides YimPaddings()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@Immutable
data class Paddings(
    val defaultPadding: Dp = 16.dp,
    val tinyPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val largePadding: Dp = 24.dp,
    
    // New set
    val horizontal: Dp = 9.dp,
    val vertical: Dp = 8.dp,
    val lazyListAdjacent: Dp = 6.dp,
    val coverArtAndTextGap: Dp = 8.dp,
    val insideCard: Dp = 8.dp,
    /** Padding for text inside custom made buttons.*/
    val insideButton: Dp = 8.dp,
    val adjacentDialogButtons: Dp = 8.dp,
    val chipsHorizontal: Dp = 6.dp,
    val insideDialog: Dp = 14.dp,
    val dialogContent: Dp = 8.dp
)
private val LocalPaddings = staticCompositionLocalOf { Paddings() }

@Immutable
data class Sizes(
    val listenCardHeight: Dp = 60.dp,
    val listenCardCorner: Dp = 8.dp,
    val dropdownItem: Dp = 20.dp
)

private val LocalSizes = staticCompositionLocalOf { Sizes() }

@Immutable
data class Shapes(
    // Change size field when changing this.
    val listenCardSmall: Shape = RoundedCornerShape(8.dp),
    val dialogs: Shape = RoundedCornerShape(4.dp),
    val listenCard: Shape = RoundedCornerShape(16.dp),
    val chips: Shape = RoundedCornerShape(4.dp)
)

private val LocalShapes = staticCompositionLocalOf { Shapes() }

@Immutable
data class TextStyles(
    val feedBlurbContent: TextStyle = TextStyle(fontStyle = FontStyle.Italic, fontSize = 15.sp),
    val feedBlurbContentTitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 14.sp),
    val chips: TextStyle = TextStyle(fontWeight = FontWeight.Medium),
    val dropdownItem: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val listenTitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold),
    val listenSubtitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
    
    // Dialog
    val dialogTitle: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 16.sp),
    val dialogTitleBold: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    val dialogButtonText: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val dialogText: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val dialogTextField: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 15.sp),
    val dialogTextBold: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

private val LocalTextStyles = staticCompositionLocalOf { TextStyles() }

/**
 * This variable defines the ui mode of the system.
 *
 * If Value is
 *
 *            TRUE -> Selected Ui Mode is Dark
 *
 *            FALSE -> Selected Ui Mode is Light
 *
 *            NULL -> Selected Ui Mode is System Theme
 *
 * This variable is public because it is used in system settings
 * @exception UninitializedPropertyAccessException Every **test** that is theme dependent should initialize this variable
 * before executing instrumented tests.*/
lateinit var isUiModeIsDark : MutableState<Boolean?>


/** This function determines if the absolute UI mode of the app is dark (True) or not, irrespective of
 * what theme the device is using. Different from [isSystemInDarkTheme].*/
@Composable
fun onScreenUiModeIsDark() : Boolean {
    return when (isUiModeIsDark.value){
        true -> true
        false -> false
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun ListenBrainzTheme(
    systemTheme: Boolean = isSystemInDarkTheme(),
    systemUiController: SystemUiController = rememberSystemUiController(),
    context: Context = LocalContext.current,
    // Dynamic color is available on Android 12+
    //dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    // dynamicColor: Boolean = false,//Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    isUiModeIsDark = remember { mutableStateOf(userSelectedThemeIsNight(context)) }
    // With Dynamic Color
    /*val colorScheme = if (dynamicColor){
            when(isUiModeIsDark.value){
                true -> dynamicDarkColorScheme(context)
                false -> dynamicLightColorScheme(context)
                else -> if (systemTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            }else{
                when (isUiModeIsDark.value) {
                    true -> DarkColorScheme
                    false -> LightColorScheme
                    else -> if (systemTheme) DarkColorScheme else LightColorScheme
            }
    }*/
    // Without Dynamic Color
    val colorScheme = when (isUiModeIsDark.value) {
        true -> DarkColorScheme
        false -> LightColorScheme
        else -> if (systemTheme) DarkColorScheme else LightColorScheme
    }
    
    // Custom ColorScheme
    val localColorScheme =
        when (isUiModeIsDark.value) {
            true -> colorSchemeDark
            false -> colorSchemeLight
            else -> if (systemTheme) colorSchemeDark else colorSchemeLight
        }
    
    LocalColorScheme = staticCompositionLocalOf { localColorScheme }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
            val isDark = when (isUiModeIsDark.value){
                true -> false
                false -> true
                else -> !systemTheme
            }
            systemUiController.statusBarDarkContentEnabled = isDark
            systemUiController.navigationBarDarkContentEnabled = isDark
            systemUiController.setNavigationBarColor(color = colorScheme.tertiaryContainer)
        }
    }
    CompositionLocalProvider {
        LocalPaddings provides Paddings()
        LocalShapes provides Shapes()
        LocalSizes provides Sizes()
        LocalTextStyles provides TextStyles()
        LocalColorScheme provides localColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


object ListenBrainzTheme {
    val colorScheme: Theme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current
    
    val paddings: Paddings
        @Composable
        @ReadOnlyComposable
        get() = LocalPaddings.current
    
    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
    
    val sizes: Sizes
        @Composable
        @ReadOnlyComposable
        get() = LocalSizes.current
    
    val textStyles: TextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalTextStyles.current
}


fun userSelectedThemeIsNight(context: Context) : Boolean? {
    return when (PreferenceManager.getDefaultSharedPreferences(context)
        .getString("app_theme", "Use device theme")){   // R.string.settings_device_theme_use_device_theme
        "Dark" -> true
        "Light" -> false
        else -> null
    }
}
