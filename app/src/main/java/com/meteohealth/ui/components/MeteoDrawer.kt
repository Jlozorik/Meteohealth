package com.meteohealth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meteohealth.BuildConfig
import com.meteohealth.ui.navigation.NavRoutes

private data class DrawerItem(val route: String, val label: String, val icon: @Composable () -> Unit)

private val items = listOf(
    DrawerItem(NavRoutes.HOME, "СЕГОДНЯ") { Icon(Icons.Outlined.WbSunny, null) },
    DrawerItem(NavRoutes.FORECAST, "ПРОГНОЗ") { Icon(Icons.Outlined.AutoGraph, null) },
    DrawerItem(NavRoutes.JOURNAL, "ЖУРНАЛ") { Icon(Icons.Outlined.BookmarkBorder, null) },
    DrawerItem(NavRoutes.SETTINGS, "НАСТРОЙКИ") { Icon(Icons.Outlined.Settings, null) },
)

@Composable
fun MeteoDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier.fillMaxHeight()) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "meteohealth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = item.icon,
                    label = {
                        Text(item.label, style = MaterialTheme.typography.labelLarge)
                    },
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(28.dp),
            )
        }
    }
}
