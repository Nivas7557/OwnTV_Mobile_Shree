package tv.own.owntv.mobile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import tv.own.owntv.core.theme.GlassSurface
import tv.own.owntv.mobile.ui.theme.MobileDimens
import tv.own.owntv.mobile.ui.theme.MobileGroupShape
import tv.own.owntv.mobile.ui.theme.glassSurface

/**
 * The same plate, for a list too long to hold in one block — a channel list, a guide, a library in
 * list mode. The pane fills the page and the rows scroll inside it, so the list still reads as one
 * block instead of as lines ruled straight across the wallpaper.
 */
@Composable
fun Modifier.mobileGroupPlate(): Modifier = this
    .padding(horizontal = MobileDimens.PagePaddingH)
    .glassSurface(GlassSurface.CARDS, MobileGroupShape)
    .clip(MobileGroupShape)
