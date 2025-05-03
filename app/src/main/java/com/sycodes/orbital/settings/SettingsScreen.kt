package com.sycodes.orbital.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Switch



@Preview(showBackground = true)
@Composable
fun SettingsScreen(){
    val navController = rememberNavController()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp).padding(WindowInsets.statusBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally) {

        item { SectionHeader("General") }
        items(generalSettings) { setting ->
            SettingItem(setting, navController)
        }

        item { SectionHeader("Privacy and security") }
        items(privacySettings) { setting ->
            SettingItem(setting, navController)
        }

        item { SectionHeader("Advanced") }
        items(advancedSettings) { setting ->
            SettingItem(setting, navController)
        }

        item { SectionHeader("About") }
        items(aboutSettings) { setting ->
            SettingItem(setting, navController)
        }
    }
}
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF6200EE),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 7.dp).fillMaxWidth()
    )
}

@Composable
fun SettingItem(item: SettingOption, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.isClickable, onClick = { item.onClick?.invoke() })
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = item.title, fontSize = 16.sp)
            if (item.subtitle != null) {
                Text(text = item.subtitle, fontSize = 12.sp)
            }
        }

        when (item.type) {
            SettingType.Toggle -> Switch(checked = item.toggleState == true, onCheckedChange = item.onToggleChange)
            else -> {

            }
        }
    }
}
data class SettingOption(
    val title: String,
    val subtitle: String? = null,
    val type: SettingType = SettingType.Navigation,
    val isClickable: Boolean = true,
    val toggleState: Boolean? = null,
    val onToggleChange: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)

enum class SettingType {
    Navigation,
    Toggle
}

val generalSettings = listOf(
    SettingOption("Search"),
    SettingOption("Tabs"),
    SettingOption("Homepage"),
)

val privacySettings = listOf(
    SettingOption("Private browsing"),
    SettingOption("HTTPS-Only Mode"),
    SettingOption("Enhanced Tracking Protection"),
    SettingOption("Site Settings"),
    SettingOption("Clear Browsing Data"),
    SettingOption("Notifications"),
)

val advancedSettings = listOf(
    SettingOption("External download manager", type = SettingType.Toggle)
)

val aboutSettings = listOf(
    SettingOption("Rate on Google Play"),
    SettingOption("About Orbital")
)


