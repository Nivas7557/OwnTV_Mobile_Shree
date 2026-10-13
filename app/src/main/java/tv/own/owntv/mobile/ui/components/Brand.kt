package tv.own.owntv.mobile.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import tv.own.owntv.core.brand.AppIcon
import tv.own.owntv.core.brand.AppIconSwitcher
import tv.own.owntv.mobile.R
import tv.own.owntv.mobile.ui.theme.glassDialogWindow

/**
 * The icon colour the launcher shows right now. The in-app logo follows it, not the saved choice, so
 * logo and home-screen icon change together after the restart.
 */
@Composable
fun rememberAppliedIcon(): AppIcon {
    val context = LocalContext.current
    return remember(context) { AppIconSwitcher.applied(context) }
}

/**
 * The icon's flat mark (no shadow, no next card); at 32 dp and below the simpler small drawing. With Settings ›
 * Accent-colored logo on (the default) the play triangle takes the accent, as on the TV: Pixel draws its dots
 * with an accent triangle, a card gets a tinted triangle over its own. [followAccent] false draws the icon as
 * the launcher shows it (the icon picker).
 */
@Composable
fun BrandMark(icon: AppIcon, size: Dp, modifier: Modifier = Modifier, followAccent: Boolean = true) {
    val small = size <= 32.dp
    val settings: tv.own.owntv.core.settings.SettingsRepository = org.koin.compose.koinInject()
    val on by settings.brandAccentTriangle.collectAsStateWithLifecycle(initialValue = true)
    val accent = tv.own.owntv.mobile.ui.stage.stageAccent.accent.takeIf { followAccent && on }
    if (icon == AppIcon.PIXEL && !small && accent != null) {
        PixelMark(accent, modifier.size(size))
        return
    }
    Box(modifier.size(size)) {
        Image(painterResource(if (small) icon.markSmall else icon.mark), contentDescription = null, modifier = Modifier.fillMaxSize())
        if (accent != null && icon.isCard(small)) {
            Image(
                painterResource(if (small) R.drawable.owntv_mark_play_small else R.drawable.owntv_mark_play),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(accent),
            )
        }
    }
}

/** "Pixel": core's dot-matrix TV set, the play triangle's dots in [triangle]. */
@Composable
private fun PixelMark(triangle: Color, modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val u = size.minDimension / 100f
        val m = tv.own.owntv.core.brand.PixelMark
        m.dots.forEach { d ->
            val color = when (d.kind) {
                tv.own.owntv.core.brand.PixelMark.Kind.BODY -> Color(m.BODY)
                tv.own.owntv.core.brand.PixelMark.Kind.TRIANGLE -> triangle
                tv.own.owntv.core.brand.PixelMark.Kind.SIDE -> Color(m.SIDE)
            }
            drawCircle(color, m.DOT_RADIUS * u, androidx.compose.ui.geometry.Offset(d.x * u, d.y * u))
        }
        drawCircle(Color(m.POWER), m.POWER_RADIUS * u, androidx.compose.ui.geometry.Offset(m.POWER_X * u, m.POWER_Y * u))
    }
}

/**
 * The "OwnTV" lockup of the pass-6 mockup: the mark, then the wordmark — "Own" in the text colour,
 * "TV" in the icon's own accent (its darker on-light accent on a light theme), extra bold with
 * -0.025 em tracking. Side by side the gap is 0.26 × the mark; [stacked] puts the mark above with a
 * 0.2 × gap. The wordmark is live text in the user's main font, so it follows a font change.
 */
@Composable
fun BrandLockup(markSize: Int, textSize: Int, modifier: Modifier = Modifier, stacked: Boolean = false) {
    val icon = rememberAppliedIcon()
    val scheme = MaterialTheme.colorScheme
    val tvColor = Color(if (scheme.background.luminance() < 0.5f) icon.accent else icon.accentOnLight)
    val parts = @Composable {
        BrandMark(icon, markSize.dp)
        Text(
            text = buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(color = scheme.onSurface)) {
                    append(stringResource(R.string.brand_own))
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = tvColor)) {
                    append(stringResource(R.string.brand_tv))
                }
            },
            fontSize = textSize.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.025).em,
        )
    }
    if (stacked) {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((markSize * 0.2f).dp),
        ) { parts() }
    } else {
        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((markSize * 0.26f).dp),
        ) { parts() }
    }
}

/**
 * The #227 wordmark by @m3th0d93 (D5, the TV's `Wordmark`): lowercase "own" in cream on a dark page (the
 * text colour on a light one), "tv" in the accent, drawn from core's two vectors at [width]; its height
 * follows the 988 × 182 drawing.
 */
@Composable
fun Wordmark(width: Dp, modifier: Modifier = Modifier) {
    val palette = tv.own.owntv.mobile.ui.stage.LocalStagePalette.current
    val own = if (palette.isLight) palette.text else WordmarkCream
    val tv = tv.own.owntv.mobile.ui.stage.stageAccent.accent
    androidx.compose.foundation.layout.Box(modifier.size(width, width * 182f / 988f)) {
        Image(painterResource(R.drawable.owntv_wordmark_own), null, Modifier.matchParentSize(), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(own))
        Image(painterResource(R.drawable.owntv_wordmark_tv), null, Modifier.matchParentSize(), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tv))
    }
}

private val WordmarkCream = Color(0xFFFFF6EE)

/**
 * The icon colours as two rows of four tiles, each showing its own flat mark, with the chosen
 * colour's name underneath. Used by the setup step and by Settings.
 */
@Composable
fun AppIconPicker(selected: AppIcon, onPick: (AppIcon) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AppIcon.entries.chunked(4).forEachIndexed { row, icons ->
            if (row > 0) Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                icons.forEach { icon ->
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(shape)
                            .border(
                                if (icon == selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape,
                            )
                            .clickable { onPick(icon) },
                        contentAlignment = Alignment.Center,
                    ) { BrandMark(icon, 44.dp, followAccent = false) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(selected.label),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Settings → App icon: one dialog. It shows the tiles, and after a pick that differs from the
 * launcher's icon the same dialog turns into the restart question, so dialogs never stack.
 */
@Composable
fun AppIconSettingsDialog(chosen: AppIcon, onPick: (AppIcon) -> Unit, onDismiss: () -> Unit) {
    val applied = rememberAppliedIcon()
    var picked by remember { mutableStateOf<AppIcon?>(null) }
    val restartFor = picked
    if (restartFor != null) {
        AppIconRestartDialog(restartFor, onDismiss)
        return
    }
    AlertDialog(
        modifier = Modifier.glassDialogWindow(),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_app_icon)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.settings_app_icon_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                AppIconPicker(
                    selected = chosen,
                    onPick = { icon ->
                        onPick(icon)
                        if (icon != applied) picked = icon else onDismiss()
                    },
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_done)) } },
    )
}

/**
 * "Restart OwnTV?" after a new icon was saved (from Settings, or brought in by a restore). Restart now
 * switches the launcher icon and reopens the app through it; Later leaves it to [AppIconSwitcher],
 * which applies it the next time the app is in the background.
 */
@Composable
fun AppIconRestartDialog(icon: AppIcon, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        modifier = Modifier.glassDialogWindow(),
        onDismissRequest = onDismiss,
        icon = { BrandMark(icon, 56.dp, followAccent = false) },
        title = { Text(stringResource(R.string.app_icon_restart_title), textAlign = TextAlign.Center) },
        text = { Text(stringResource(R.string.app_icon_restart_message), textAlign = TextAlign.Center) },
        confirmButton = {
            TextButton(onClick = { context.findActivity()?.let { AppIconSwitcher.restartWith(it, icon) } }) {
                Text(stringResource(R.string.app_icon_restart_now))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_later)) } },
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
