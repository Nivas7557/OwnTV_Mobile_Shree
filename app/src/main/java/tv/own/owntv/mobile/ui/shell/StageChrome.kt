package tv.own.owntv.mobile.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.own.owntv.core.database.entity.ProfileEntity
import tv.own.owntv.core.ui.stage.PlaylistMark
import tv.own.owntv.mobile.R
import tv.own.owntv.mobile.ui.components.MobileIcons
import tv.own.owntv.mobile.ui.nav.MobileDestination
import tv.own.owntv.mobile.ui.profiles.ProfileAvatar
import tv.own.owntv.mobile.ui.stage.LocalStagePalette
import tv.own.owntv.mobile.ui.stage.StageIcon
import tv.own.owntv.mobile.ui.stage.StagePill
import tv.own.owntv.mobile.ui.stage.StageRadii
import tv.own.owntv.mobile.ui.stage.stageAccent
import tv.own.owntv.mobile.ui.stage.stageClickable
import tv.own.owntv.mobile.ui.stage.stageGlass
import tv.own.owntv.mobile.ui.stage.stageShadow
import tv.own.owntv.mobile.ui.stage.stageText

/**
 * How far the header and the bottom chrome have slid away. The header always rides the scroll (the
 * title lives on the page and leaves with it; back on the way up); the bottom chrome only when the user
 * has Hide while scrolling on. Read in the layout phase, so sliding never recomposes a screen.
 */
@Stable
class ShellChromeState {
    /** 0 = header fully shown … -[headerHeight] = gone. */
    var headerOffset by mutableFloatStateOf(0f)
    var headerHeight by mutableFloatStateOf(0f)

    /** 0 = bottom chrome fully shown … [bottomHeight] = gone. */
    var bottomOffset by mutableFloatStateOf(0f)
    var bottomHeight by mutableFloatStateOf(0f)
    var hideBottomOnScroll = true

    fun reset() {
        headerOffset = 0f
        bottomOffset = 0f
    }

    /**
     * The header's travel is consumed, because the content's top moves with it and would otherwise travel
     * twice; the bottom chrome's is not, since the content's bottom edge moving does not move the list.
     */
    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            val header = (headerOffset + dy).coerceIn(-headerHeight, 0f)
            val consumed = header - headerOffset
            headerOffset = header
            if (hideBottomOnScroll) bottomOffset = (bottomOffset - dy).coerceIn(0f, bottomHeight)
            return Offset(0f, consumed)
        }
    }
}

/**
 * The bottom bar (`.ph .nav`): a floating glass capsule, 64 dp (56 icons only), its items 64 × 52 on an
 * 18 dp radius. The current place is the accent with a 14% tile and a glowing 5 dp dot under it. A tap on
 * the current place sends its list to the top, as does a long press on any place.
 */
@Composable
fun StageNavBar(
    destinations: List<MobileDestination>,
    current: MobileDestination?,
    labels: Boolean,
    onClick: (MobileDestination) -> Unit,
    onLongClick: (MobileDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalStagePalette.current
    val accent = stageAccent.accent
    val shape = RoundedCornerShape(StageRadii.BottomBar)
    Row(
        modifier
            .fillMaxWidth()
            .height(if (labels) 64.dp else 56.dp)
            .stageGlass(shape)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        destinations.forEach { destination ->
            val on = destination == current
            val label = stringResource(destination.labelRes)
            val interaction = remember { MutableInteractionSource() }
            Box(contentAlignment = Alignment.Center) {
                Column(
                    Modifier
                        .size(64.dp, 52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .then(if (on) Modifier.background(accent.copy(alpha = 0.14f)) else Modifier)
                        .stageClickable(interaction, { onClick(destination) }, { onLongClick(destination) }, Role.Tab)
                        .semantics {
                            selected = on
                            if (!labels) contentDescription = label
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                ) {
                    val tint = if (on) accent else palette.navText
                    StageIcon(destination.icon, 22.dp, tint)
                    if (labels) {
                        Text(label, style = stageText(10.5f, 700, lineHeight = 1f), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                // `.it.on::after`: 5 dp, 6 dp below the item, glowing.
                if (on) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 52.dp + 1.dp)
                            .stageShadow(accent, 8.dp, shape = CircleShape)
                            .size(5.dp)
                            .background(accent, CircleShape),
                    )
                }
            }
        }
    }
}

/**
 * The page header (`.hdr`): the page's own title (28 / 800) with the top-right [cluster] beside it, and
 * under it the crumb — "‹ where you came from" on a pushed page, which goes back when tapped (F24: the
 * title names the page, the crumb says where from). Home has no title; a detail page has only the crumb.
 */
@Composable
fun StagePageHeader(
    title: String?,
    backLabel: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    cluster: @Composable () -> Unit,
) {
    val palette = LocalStagePalette.current
    Column(modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                if (title != null) {
                    Text(
                        title,
                        style = stageText(28f, 800, (-0.6f / 28f).em, lineHeight = 1.05f),
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // The cluster is 36 dp tall; a one-line title sits on its centre line.
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            cluster()
        }
        if (backLabel != null) {
            val interaction = remember { MutableInteractionSource() }
            Row(
                Modifier
                    .padding(top = 5.dp)
                    .stageClickable(interaction, onBack, null)
                    .semantics { contentDescription = backLabel },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StageIcon(MobileIcons.ArrowBack, 13.dp, palette.muted)
                Text(backLabel, style = stageText(13f, 700), color = stageAccent.accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * The top-right cluster (`.cl`): the playlist pill, search and the avatar that opens More. [playlist] is
 * the pill's state: null hides it (settings and More pages), [ClusterPlaylist.iconOnly] is Home's layers
 * pill. [extra] takes anything that must stay reachable from every page (the cast button).
 */
@Composable
fun ShellCluster(
    playlist: ClusterPlaylist?,
    profile: ProfileEntity?,
    showSearch: Boolean,
    onPlaylist: () -> Unit,
    onSearch: () -> Unit,
    onAvatar: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        extra()
        playlist?.let { pl ->
            if (pl.iconOnly) {
                StagePill(onClick = onPlaylist, icon = MobileIcons.Layers, contentDescription = pl.label)
            } else {
                StagePill(
                    onClick = onPlaylist,
                    text = pl.label,
                    icon = MobileIcons.Layers.takeIf { pl.mark == null },
                    mark = pl.mark,
                    trailingIcon = MobileIcons.KeyboardArrowDown.takeIf { pl.switchable },
                    modifier = Modifier.widthIn(max = 190.dp),
                )
            }
        }
        if (showSearch) {
            StagePill(onClick = onSearch, icon = MobileIcons.Search, contentDescription = stringResource(R.string.common_nav_search))
        }
        ShellAvatar(profile, onAvatar)
    }
}

/** What the playlist pill says: the label, the chosen playlist's mark (none for All playlists), and whether it opens the sheet. */
data class ClusterPlaylist(val label: String, val mark: PlaylistMark?, val switchable: Boolean, val iconOnly: Boolean)

/**
 * `.av`: the profile as a 32 dp circle — its own picture (the drawn tile or a photo), or with none its
 * initial on the mockup's warm gradient. A tap opens More.
 */
@Composable
private fun ShellAvatar(profile: ProfileEntity?, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val more = stringResource(R.string.common_nav_more)
    val modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .stageClickable(interaction, onClick, null)
        .semantics { contentDescription = more }
    if (profile != null && (profile.avatarId >= 0 || !profile.avatarPath.isNullOrBlank())) {
        ProfileAvatar(profile.avatarId, modifier, profile.avatarPath.orEmpty())
    } else {
        Box(
            modifier.drawBehind {
                drawRect(Brush.linearGradient(listOf(Color(0xFFFFB35C), Color(0xFFFF5F7A)), Offset.Zero, Offset(size.width, size.height)))
            },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                profile?.name?.firstOrNull()?.uppercase().orEmpty(),
                style = stageText(14f, 800, lineHeight = 1f),
                color = Color(0xFF2A0D12),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
