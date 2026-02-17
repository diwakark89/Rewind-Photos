package com.thewalkersoft.rewindphotos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.White

/**
 * Settings Screen composable matching the Ayer app UI
 *
 * Displays app settings organized into categories:
 * - Appearance (Theme)
 * - Language
 * - Notifications (Daily reminder, Reminder time)
 * - Content (Show screenshots)
 * - Support (Rate Ayer, Share Ayer, Ayer Website)
 *
 * Features Material 3 design with proper spacing, icons, and interactions
 *
 * @param modifier Modifier for styling
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val dailyReminderEnabled = remember { mutableStateOf(true) }
    val showScreenshotsEnabled = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = TextDark
                )
            )
        },
        containerColor = LightGray
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Appearance Section
            SettingsSectionHeader("APPEARANCE")
            SettingsItem(
                icon = Icons.Filled.LightMode,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Theme",
                subtitle = "Automatic",
                onItemClick = { /* Handle theme click */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language Section
            SettingsSectionHeader("LANGUAGE")
            SettingsItem(
                icon = Icons.Filled.Language,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Language",
                subtitle = "English",
                onItemClick = { /* Handle language click */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Section
            SettingsSectionHeader("NOTIFICATIONS")
            SettingsSwitchItem(
                icon = Icons.Filled.Notifications,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Daily reminder",
                isEnabled = dailyReminderEnabled.value,
                onToggle = { dailyReminderEnabled.value = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItem(
                icon = Icons.Filled.AccessTime,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Reminder time",
                subtitle = "11:30 AM",
                onItemClick = { /* Handle time click */ },
                isEnabled = dailyReminderEnabled.value
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Section
            SettingsSectionHeader("CONTENT")
            SettingsSwitchItem(
                icon = Icons.Filled.Image,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Show screenshots",
                isEnabled = showScreenshotsEnabled.value,
                onToggle = { showScreenshotsEnabled.value = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Support Section
            SettingsSectionHeader("SUPPORT")
            SettingsItem(
                icon = Icons.Filled.Star,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Rate Ayer",
                onItemClick = { /* Handle rate click */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItem(
                icon = Icons.Filled.Share,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Share Ayer",
                onItemClick = { /* Handle share click */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItem(
                icon = Icons.Filled.Public,
                iconBackgroundColor = Color(0xFFFFE8CC),
                iconTint = Orange,
                title = "Ayer Website",
                onItemClick = { /* Handle website click */ }
            )

            // Version footer
            Text(
                text = "Ayer v0.0.65",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                fontSize = 14.sp,
                color = TextMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Settings section header composable
 *
 * @param title Title text for the section
 * @param modifier Modifier for styling
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextMedium.copy(alpha = 0.7f)
    )
}

/**
 * Settings item with icon, title, subtitle, and chevron
 *
 * @param icon Icon to display
 * @param iconBackgroundColor Background color for icon
 * @param iconTint Tint color for icon
 * @param title Title text
 * @param subtitle Optional subtitle text
 * @param isEnabled Whether the item is enabled (affects opacity)
 * @param onItemClick Callback when item is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    isEnabled: Boolean = true,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(enabled = isEnabled) { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = TextMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Navigate",
            modifier = Modifier.size(20.dp),
            tint = TextMedium
        )
    }
}

/**
 * Settings switch item with icon, title, and toggle switch
 *
 * @param icon Icon to display
 * @param iconBackgroundColor Background color for icon
 * @param iconTint Tint color for icon
 * @param title Title text
 * @param isEnabled Current state of the switch
 * @param onToggle Callback when switch is toggled
 * @param modifier Modifier for styling
 */
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextDark
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Orange,
                uncheckedThumbColor = White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}
