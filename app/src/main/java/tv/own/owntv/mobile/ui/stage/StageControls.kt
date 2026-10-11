package tv.own.owntv.mobile.ui.stage

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.own.owntv.core.ui.stage.PlaylistMark
import tv.own.owntv.mobile.R
import tv.own.owntv.mobile.ui.components.MobileIcons
import tv.own.owntv.mobile.ui.theme.LocalMobileMotion

/*
 * Stage's small controls (`phone.css`): buttons, pills, tools, segmented, tags, the playlist mark, switch,
 * stepper, radio, the search field, progress, the LIVE badge and spec chips. Touch has no focus ladder:
 * idle = nothing, selected / on = accent, pressed = filled accent. Compose widens every tap target under
 * 48 dp by itself, so the drawn sizes stay the mockup's.
 */

/** A Stage glyph at a mockup icon size (`1em` of its element). */
@Composable
fun StageIcon(icon: ImageVector, size: Dp, tint: Color, modifier: Modifier = Modifier) =
    Icon(icon, contentDescription = null, tint = tint, modifier = modifier.size(size))

enum class StageButtonStyle { PRIMARY, SECONDARY, DANGER }

/**
 * `.btn`: 44 dp (34 [small]), the ink fill; PRIMARY is the accent with its glow, DANGER the danger text.
 * No [text] makes the round icon button (`.btn.round`). Pressed fills a secondary button with the accent.
 */
@Composable
fun StageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    style: StageButtonStyle = StageButtonStyle.SECONDARY,
    small: Boolean = false,
    enabled: Boolean = true,
) {
    val palette = LocalStagePalette.current
    val accent = stageAccent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val filled = style == StageButtonStyle.PRIMARY || pressed
    val radius = if (small) 12.dp else StageRadii.Button
    val shape = RoundedCornerShape(radius)
    val fg = when {
        filled -> accent.onAccent
        style == StageButtonStyle.DANGER -> palette.danger
        else -> palette.text
    }
    Row(
        modifier
            .then(
                if (style == StageButtonStyle.PRIMARY) {
                    Modifier.stageShadow(
                        accent.accent.copy(alpha = if (palette.isLight) 0.3f else 0.4f),
                        if (palette.isLight) 16.dp else 24.dp,
                        if (palette.isLight) 6.dp else 8.dp,
                        shape,
                    )
                } else Modifier,
            )
            .height(if (small) 34.dp else 44.dp)
            .then(if (text == null) Modifier.width(if (small) 34.dp else 44.dp) else Modifier)
            .clip(shape)
            .background(if (filled) accent.accent else palette.ink.copy(alpha = 0.08f))
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (text == null) 0.dp else if (small) 12.dp else 18.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { StageIcon(it, 16.dp, fg) }
        text?.let {
            Text(it, style = stageText(if (small) 12.5f else 14f, 700), color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * `.pill`: a 36 dp glass pill of the top cluster — a leading accent icon, the text, an optional trailing
 * accent icon (the chevron of "All playlists ⌄") and an optional playlist [mark]. No [text] makes the
 * 36 dp icon pill, its glyph in the text colour.
 */
@Composable
fun StagePill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    mark: PlaylistMark? = null,
    contentDescription: String? = null,
) {
    val palette = LocalStagePalette.current
    val accent = stageAccent
    val shape = RoundedCornerShape(StageRadii.Pill)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier
            .height(36.dp)
            .then(if (text == null) Modifier.width(36.dp) else Modifier)
            .then(if (pressed) Modifier.clip(shape).background(accent.accent) else Modifier.stageGlass(shape))
            .clip(shape)
            .clickable(interaction, indication = null, role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .padding(horizontal = if (text == null) 0.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconTint = if (pressed) accent.onAccent else if (text == null) palette.text else accent.accent
        icon?.let { StageIcon(it, if (text == null) 17.dp else 15.dp, iconTint) }
        mark?.let { StagePlaylistMark(it) }
        text?.let {
            Text(it, style = stageText(13f, 700), color = if (pressed) accent.onAccent else palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailingIcon?.let { StageIcon(it, 15.dp, iconTint) }
    }
}

/**
 * `.tool`: a quiet 34 dp text button of the tool row under a title — muted [label], then the [value] in
 * the text colour ("Sort **Provider**"). [on] is the accent tint with its ring.
 */
@Composable
fun StageTool(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    value: String? = null,
    icon: ImageVector? = null,
    on: Boolean = false,
) {
    val palette = LocalStagePalette.current
    val accent = stageAccent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(StageRadii.Tool)
    val fill = when {
        pressed -> accent.accent
        on -> accent.accent.copy(alpha = 0.18f)
        else -> palette.controlFill
    }
    val fg = if (pressed) accent.onAccent else if (on) accent.accent else palette.muted
    val strong = if (pressed) accent.onAccent else if (on) accent.accent else palette.text
    Row(
        modifier
            .height(34.dp)
            .clip(shape)
            .background(fill)
            .then(
                if (on && !pressed) {
                    Modifier.drawBehind { drawInnerRing(accent.accent.copy(alpha = 0.55f), 1.2.dp.toPx(), StageRadii.Tool.toPx()) }
                } else Modifier,
            )
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { StageIcon(it, 14.dp, fg) }
        label?.let { Text(it, style = stageText(12.5f, 700), color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        value?.let { Text(it, style = stageText(12.5f, 700), color = strong, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

/** `.seg`: options on a 3 dp-padded ink track; the chosen one a lighter chip (white with a shadow in light). */
@Composable
fun StageSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector?> = emptyList(),
) {
    val palette = LocalStagePalette.current
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.controlFill)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            val chip = RoundedCornerShape(9.dp)
            Row(
                Modifier
                    .height(28.dp)
                    .then(
                        when {
                            !on -> Modifier
                            palette.isLight -> Modifier.stageShadow(palette.shadow.copy(alpha = 0.16f), 3.dp, 1.dp, chip).background(Color.White, chip)
                            else -> Modifier.background(palette.ink.copy(alpha = 0.14f), chip)
                        },
                    )
                    .clip(chip)
                    .clickable(role = Role.Tab, onClick = { onSelect(i) })
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val fg = if (on) palette.text else palette.muted
                icons.getOrNull(i)?.let { StageIcon(it, 14.dp, fg) }
                Text(label, style = stageText(12f, 700), color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

enum class StageTagTone { PLAIN, ACCENT, WARN }

/** `.tag`: 9.5 / 800 caps-sized text on a 5 dp chip — ink, accent, or warn. */
@Composable
fun StageTag(text: String, modifier: Modifier = Modifier, tone: StageTagTone = StageTagTone.PLAIN) {
    val palette = LocalStagePalette.current
    val accent = stageAccent.accent
    val (fill, fg) = when (tone) {
        StageTagTone.PLAIN -> palette.ink.copy(alpha = 0.08f) to palette.tagText
        StageTagTone.ACCENT -> accent.copy(alpha = 0.2f) to accent
        StageTagTone.WARN -> (if (palette.isLight) Color(184, 100, 10).copy(alpha = 0.12f) else Color(255, 179, 92).copy(alpha = 0.16f)) to palette.warn
    }
    Text(
        text,
        style = stageText(9.5f, 800, 0.05.em),
        color = fg,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = modifier.clip(RoundedCornerShape(StageRadii.Tag)).background(fill).padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

/** `.rec`: the RECOMMENDED tag of a choice sheet. */
@Composable
fun StageRecommended(modifier: Modifier = Modifier) {
    val accent = stageAccent.accent
    Text(
        stringResource(R.string.settings_panel_recommended).uppercase(),
        style = stageText(9f, 800, 0.06.em),
        color = accent,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = modifier.clip(RoundedCornerShape(StageRadii.Tag)).background(accent.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** `.pm`: which playlist a row comes from, outlined in its colour; darkened in light to read on white. */
@Composable
fun StagePlaylistMark(mark: PlaylistMark, modifier: Modifier = Modifier) {
    val base = Color(mark.argb)
    val color = if (LocalStagePalette.current.isLight) Color(base.red * 0.72f, base.green * 0.72f, base.blue * 0.72f) else base
    Box(
        modifier
            .height(17.dp)
            .defaultMinSize(minWidth = 24.dp)
            .drawBehind { drawInnerRing(color, 1.2.dp.toPx(), StageRadii.Tag.toPx()) }
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(mark.text, style = stageText(9.5f, 800, 0.04.em, lineHeight = 1f), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** `.sw`: 40 × 24, the knob sliding with the phone's spring (snaps with Animations off). A picture: the row toggles. */
@Composable
fun StageSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val palette = LocalStagePalette.current
    val accent = stageAccent.accent
    val knob by animateDpAsState(if (checked) 19.dp else 3.dp, LocalMobileMotion.current.fast(), label = "stageSwitch")
    val knobColor = if (checked || palette.isLight) Color.White else Color(0xFFCFD8D4)
    Box(
        modifier
            .size(40.dp, 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) accent else palette.ink.copy(alpha = 0.16f)),
    ) {
        Box(
            Modifier
                .offset { IntOffset(knob.roundToPx(), 3.dp.roundToPx()) }
                .stageShadow(Color.Black.copy(alpha = 0.25f), 3.dp, 1.dp, CircleShape)
                .size(18.dp)
                .background(knobColor, CircleShape),
        )
    }
}

/** `.step`: − value +, two 28 dp ink keys either side of a 44 dp value. */
@Composable
fun StageStepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    minusEnabled: Boolean = true,
    plusEnabled: Boolean = true,
) {
    val palette = LocalStagePalette.current
    @Composable
    fun Key(text: String, enabled: Boolean, onClick: () -> Unit) = Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(palette.ink.copy(alpha = 0.08f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = stageText(15f, 700, lineHeight = 1f), color = if (enabled) palette.text else palette.dim)
    }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Key(stringResource(R.string.common_stepper_minus), minusEnabled, onMinus)
        Text(
            value,
            style = stageText(13f, 800),
            color = palette.text,
            textAlign = TextAlign.Center,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(min = 44.dp),
        )
        Key(stringResource(R.string.common_stepper_plus), plusEnabled, onPlus)
    }
}

/** `.radio`: an 18 dp ring; chosen = accent ring, a page-coloured gap and an accent dot. */
@Composable
fun StageRadio(selected: Boolean, modifier: Modifier = Modifier) {
    val palette = LocalStagePalette.current
    val accent = stageAccent.accent
    Box(
        modifier.size(18.dp).drawBehind {
            val r = size.minDimension / 2f
            if (selected) {
                drawCircle(accent, r)
                drawCircle(palette.page, r - 1.8.dp.toPx())
                drawCircle(accent, r - 5.dp.toPx())
            } else {
                drawCircle(palette.muted, r - 0.9.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Stroke(1.8.dp.toPx()))
            }
        },
    )
}

/**
 * `.srch`: the 42 dp search field (36 [inline]); the dim placeholder, the text colour once typing, and the
 * focus ring while it has the keyboard.
 */
@Composable
fun StageSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    inline: Boolean = false,
    onSearch: (() -> Unit)? = null,
) {
    val palette = LocalStagePalette.current
    val accent = stageAccent
    var focused by remember { mutableStateOf(false) }
    val style = stageText(13.5f, 500)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = style.copy(color = palette.text),
        cursorBrush = SolidColor(accent.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearch?.invoke() }),
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        decorationBox = { field ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(if (inline) 36.dp else 42.dp)
                    .clip(RoundedCornerShape(StageRadii.Search))
                    .background(palette.controlFill)
                    .then(
                        if (focused) {
                            Modifier.drawBehind { drawInnerRing(accent.focus, 1.5.dp.toPx(), StageRadii.Search.toPx()) }
                        } else Modifier,
                    )
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StageIcon(MobileIcons.Search, 16.dp, if (focused) palette.text else palette.dim)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = style, color = palette.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    field()
                }
            }
        },
    )
}

/**
 * Progress. `.prog` (default): 4 dp on ink 12%, the fill an accent → focus sweep (plain accent in light).
 * [thin] is the channel row's `.pb`: 3 dp on ink 10%, a plain accent fill.
 */
@Composable
fun StageProgress(fraction: Float, modifier: Modifier = Modifier, thin: Boolean = false) {
    val palette = LocalStagePalette.current
    val accent = stageAccent
    val sweep = !thin && !palette.isLight
    Box(
        modifier
            .fillMaxWidth()
            .height(if (thin) 3.dp else 4.dp)
            .drawBehind {
                val r = CornerRadius(2.dp.toPx())
                drawRoundRect(palette.ink.copy(alpha = if (thin) 0.10f else 0.12f), cornerRadius = r)
                val w = size.width * fraction.coerceIn(0f, 1f)
                if (w <= 0f) return@drawBehind
                val fill = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, 0f, w, size.height, r)) }
                if (sweep) {
                    drawPath(fill, androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(accent.accent, accent.focus), endX = w))
                } else {
                    drawPath(fill, accent.accent)
                }
            },
    )
}

/** `.live`: the LIVE badge over video — a glowing red dot and [text] on black 55%, dark in both themes. */
@Composable
fun StageLiveBadge(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(7.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val red = Color(0xFFFF4D4D)
        Box(Modifier.stageShadow(red, 8.dp, shape = CircleShape).size(6.dp).background(red, CircleShape))
        Text(text, style = stageText(10.5f, 800, 0.08.em, lineHeight = 1f), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** `.spec`: the stream's spec chips over video (4K, HDR, 5.1), dark in both themes. */
@Composable
fun StageSpecChips(specs: List<String>, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        specs.forEach {
            Text(
                it,
                style = stageText(9.5f, 800, 0.05.em, lineHeight = 1f),
                color = Color(0xFFE7EFEC),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
    }
}

/** `.label`: a small upper-case heading in dim (`.grp` in sheets and `.shd` on pages build on it). */
@Composable
fun StageLabel(text: String, modifier: Modifier = Modifier, color: Color = LocalStagePalette.current.dim) =
    Text(text.uppercase(), style = stageLabel(), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
