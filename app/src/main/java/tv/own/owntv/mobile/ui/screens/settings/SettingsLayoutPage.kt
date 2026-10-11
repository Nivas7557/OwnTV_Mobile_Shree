package tv.own.owntv.mobile.ui.screens.settings

import tv.own.owntv.mobile.ui.components.MobileIcons
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.koinViewModel
import tv.own.owntv.core.menu.applyMenuOrder
import tv.own.owntv.core.menu.catalogue
import tv.own.owntv.core.model.ContentMenu
import tv.own.owntv.mobile.R
import tv.own.owntv.mobile.ui.components.MobileBottomSheet
import tv.own.owntv.mobile.ui.components.MobileListRow
import tv.own.owntv.mobile.ui.components.SettingRow

private const val MIN_DENSITY = 70
private const val MAX_DENSITY = 130

/**
 * Where things sit: the navigation bar, what a browse section returns to, the order of the long-press
 * menus and how tall a guide row is. Home has enough of its own to be a screen rather than a block.
 *
 * Every one of these is stored where the TV app stores it, so a user who hides Downloads or moves
 * "Download" to the top of the movie menu here finds the television already agreeing with them.
 */
@Composable
fun SettingsLayoutPage(
    onOpenLeaf: (SettingsLeaf) -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = koinViewModel(),
) {
    var navigationSheet by remember { mutableStateOf(false) }

    var menuSheet by remember { mutableStateOf<ContentMenu?>(null) }

    // Collected out here rather than in the rows: a lazy list's builder is not a composable scope,
    // so a flow read inside it would not recompose the row that shows it.
    val categoryLive = vm.settings.rememberCategoryLive.pref(true)
    val categoryMovies = vm.settings.rememberCategoryMovies.pref(true)
    val categorySeries = vm.settings.rememberCategorySeries.pref(true)
    val lastLive = vm.settings.rememberLastLive.pref(false)
    val lastMovies = vm.settings.rememberLastMovies.pref(false)
    val lastSeries = vm.settings.rememberLastSeries.pref(false)
    val guideDensity = vm.settings.guideDensityPct.pref(100)

    SettingsPage(modifier) {
        settingsLeafRows(SettingsGroup.LAYOUT, onOpenLeaf)

        // --- Navigation: the bottom bar and the sheets, in one sheet (M1-03) ---
        settingsGroup(key = "nav") {
            SettingRow(
                title = stringResource(R.string.settings_navigation),
                showChevron = true,
                onClick = { navigationSheet = true },
            )
        }

        // --- Browsing & lists ---
        settingsSection(R.string.settings_browsing_title)
        settingsNote(R.string.settings_browsing_description_full)
        settingsGroup(key = "browsing") {
            BrowsingRows(
                sectionRes = R.string.common_nav_live_tv,
                category = categoryLive,
                lastItem = lastLive,
                onCategory = { on -> vm.edit { setRememberCategoryLive(on) } },
                onLastItem = { on -> vm.edit { setRememberLastLive(on) } },
            )
            BrowsingRows(
                sectionRes = R.string.common_nav_movies,
                category = categoryMovies,
                lastItem = lastMovies,
                onCategory = { on -> vm.edit { setRememberCategoryMovies(on) } },
                onLastItem = { on -> vm.edit { setRememberLastMovies(on) } },
            )
            BrowsingRows(
                sectionRes = R.string.common_nav_series,
                category = categorySeries,
                lastItem = lastSeries,
                onCategory = { on -> vm.edit { setRememberCategorySeries(on) } },
                onLastItem = { on -> vm.edit { setRememberLastSeries(on) } },
            )
        }

        // --- Long-press menus ---
        settingsSection(R.string.settings_content_menus_title)
        settingsNote(R.string.settings_content_menus_description)
        settingsGroup(key = "menus") {
            ContentMenu.entries.forEach { menu ->
                SettingRow(
                    title = stringResource(menu.titleRes()),
                    showChevron = true,
                    onClick = { menuSheet = menu },
                )
            }
        }

        // --- Guide ---
        settingsSection(R.string.content_epg_title) {
            SettingsSlider(
                title = stringResource(R.string.settings_size),
                value = guideDensity,
                range = MIN_DENSITY..MAX_DENSITY,
                onValueChange = { pct -> vm.edit { setGuideDensityPct(pct) } },
            )
        }
    }

    if (navigationSheet) {
        NavigationSheet(vm = vm, onDismiss = { navigationSheet = false })
    }

    menuSheet?.let { menu ->
        MenuOrderSheet(vm = vm, menu = menu, onDismiss = { menuSheet = null })
    }
}

/** The two "come back to where I was" switches a browse section has, under its own name. */
@Composable
private fun BrowsingRows(
    @StringRes sectionRes: Int,
    category: Boolean,
    lastItem: Boolean,
    onCategory: (Boolean) -> Unit,
    onLastItem: (Boolean) -> Unit,
) {
    SettingRow(
        title = stringResource(R.string.settings_browsing_last_category),
        subtitle = stringResource(sectionRes),
        checked = category,
        onCheckedChange = onCategory,
    )
    SettingRow(
        title = stringResource(R.string.settings_browsing_last_item),
        subtitle = stringResource(sectionRes),
        checked = lastItem,
        onCheckedChange = onLastItem,
    )
}

@Composable
internal fun MoveButton(up: Boolean, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = if (up) MobileIcons.KeyboardArrowUp else MobileIcons.KeyboardArrowDown,
            contentDescription = stringResource(
                if (up) R.string.settings_row_menu_move_up else R.string.settings_row_menu_move_down,
            ),
            // Without a tint these inherit a content colour meant for a filled surface and come out
            // almost black on the row, which reads as two stray marks rather than two buttons.
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f),
        )
    }
}

/** One long-press menu's actions, in the order they will appear, with arrows to rearrange them. */
@Composable
private fun MenuOrderSheet(vm: SettingsViewModel, menu: ContentMenu, onDismiss: () -> Unit) {
    val key = menu.name.lowercase()
    val saved = remember(menu) { vm.settings.menuOrder(key) }.pref(emptyList())
    val actions = applyMenuOrder(catalogue(menu), saved) { it.key }

    MobileBottomSheet(onDismissRequest = onDismiss, title = stringResource(menu.titleRes())) {
        actions.forEachIndexed { index, action ->
            MobileListRow(
                title = stringResource(action.labelRes),
                trailing = {
                    Row {
                        MoveButton(up = true, enabled = index > 0) {
                            vm.edit { setMenuOrder(key, actions.movedKeys(index, -1)) }
                        }
                        MoveButton(up = false, enabled = index < actions.lastIndex) {
                            vm.edit { setMenuOrder(key, actions.movedKeys(index, 1)) }
                        }
                    }
                },
            )
        }
    }
}

private fun List<tv.own.owntv.core.menu.MenuActionRef>.movedKeys(index: Int, delta: Int): List<String> =
    map { it.key }.toMutableList().apply { add(index + delta, removeAt(index)) }


private fun ContentMenu.titleRes() = when (this) {
    ContentMenu.LIVE -> R.string.common_nav_live_tv
    ContentMenu.MOVIE -> R.string.common_nav_movies
    ContentMenu.SERIES -> R.string.common_nav_series
    ContentMenu.EPISODE -> R.string.content_episodes
}
