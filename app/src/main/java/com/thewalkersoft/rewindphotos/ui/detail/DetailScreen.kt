package com.thewalkersoft.rewindphotos.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.White

/**
 * Detail/Delete Screen composable
 *
 * Similar to MainScreen but with selection mode enabled:
 * - Shows photos with selection checkboxes
 * - Bottom action panel with Cancel, Share, and Delete buttons
 * - Allows users to select multiple photos for batch operations
 *
 * @param modifier Modifier for styling
 * @param onCancel Callback when cancel button is clicked
 * @param onShare Callback when share button is clicked
 * @param onDelete Callback when delete button is clicked
 * @param onSettingsClick Callback when settings icon is clicked
 */
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val selectedYear = remember { mutableStateOf(2025) }
    val selectedDate = remember { mutableStateOf("February 10") }
    val selectedPhotos = remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top Action Bar
        TopActionBarDetail(
            onSettingsClick = onSettingsClick
        )

        // Date Navigation and Filter
        DateNavigationBarDetail(
            currentDate = selectedDate.value,
            onPreviousDate = { selectedDate.value = "February 9" },
            onNextDate = { selectedDate.value = "February 11" }
        )

        // Year Filter Buttons
        YearFilterRowDetail(
            selectedYear = selectedYear.value,
            onYearSelected = { year -> selectedYear.value = year }
        )

        // Memory Cards List in Selection Mode
        MemoryCardsListDetail(
            selectedYear = selectedYear.value,
            selectedDate = selectedDate.value,
            selectedPhotos = selectedPhotos.value,
            onPhotoSelect = { photoId ->
                val updated = selectedPhotos.value.toMutableSet()
                if (updated.contains(photoId)) {
                    updated.remove(photoId)
                } else {
                    updated.add(photoId)
                }
                selectedPhotos.value = updated
            }
        )

        // Bottom Action Panel
        BottomActionPanel(
            itemCount = selectedPhotos.value.size,
            onCancel = onCancel,
            onShare = onShare,
            onDelete = onDelete
        )
    }
}

/**
 * Top action bar for detail screen with settings icon and close button
 *
 * @param onSettingsClick Callback when settings is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun TopActionBarDetail(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings Icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp),
                tint = TextDark
            )
        }

        // Close Button (X icon)
        IconButton(
            onClick = { /* Close selection mode */ },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                modifier = Modifier.size(24.dp),
                tint = Orange
            )
        }
    }
}

/**
 * Date navigation bar for detail screen
 *
 * @param currentDate Current selected date string
 * @param onPreviousDate Callback for previous date navigation
 * @param onNextDate Callback for next date navigation
 * @param modifier Modifier for styling
 */
@Composable
private fun DateNavigationBarDetail(
    currentDate: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Date Button
        IconButton(
            onClick = onPreviousDate,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous Date",
                modifier = Modifier.size(20.dp),
                tint = TextDark
            )
        }

        // Date Display
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(LightGray)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
        }

        // Next Date Button
        IconButton(
            onClick = onNextDate,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next Date",
                modifier = Modifier.size(20.dp),
                tint = TextDark
            )
        }

        // Calendar Icon (right side)
        IconButton(
            onClick = { /* Open calendar */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GridView,
                contentDescription = "Calendar",
                modifier = Modifier.size(20.dp),
                tint = Orange
            )
        }
    }
}

/**
 * Year filter buttons for detail screen
 *
 * @param selectedYear Currently selected year
 * @param onYearSelected Callback when a year is selected
 * @param modifier Modifier for styling
 */
@Composable
private fun YearFilterRowDetail(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val years = listOf(2025, 2024, 2019)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        years.forEach { year ->
            YearFilterButtonDetail(
                year = year,
                isSelected = selectedYear == year,
                onSelect = { onYearSelected(year) }
            )
        }
    }
}

/**
 * Individual year filter button for detail screen
 *
 * @param year Year value
 * @param isSelected Whether this year is currently selected
 * @param onSelect Callback when selected
 * @param modifier Modifier for styling
 */
@Composable
private fun YearFilterButtonDetail(
    year: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Orange else LightGray)
            .clickable { onSelect() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = year.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) White else TextDark
        )
    }
}

/**
 * Memory cards list in selection mode
 *
 * @param selectedYear The currently selected year
 * @param selectedDate The currently selected date
 * @param selectedPhotos Set of selected photo IDs
 * @param onPhotoSelect Callback when a photo is selected/deselected
 * @param modifier Modifier for styling
 */
@Composable
private fun MemoryCardsListDetail(
    selectedYear: Int,
    selectedDate: String,
    selectedPhotos: Set<String>,
    onPhotoSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data
    val memoriesByDate = mapOf(
        2025 to mapOf(
            "February 10" to listOf(
                MemoryItemDetail("memory_1", "memory_1.jpg"),
                MemoryItemDetail("memory_2", "memory_2.jpg"),
                MemoryItemDetail("memory_3", "memory_3.jpg"),
                MemoryItemDetail("memory_4", "memory_4.jpg"),
                MemoryItemDetail("memory_5", "memory_5.jpg"),
                MemoryItemDetail("memory_6", "memory_6.jpg")
            )
        ),
        2024 to mapOf(
            "February 10" to listOf(
                MemoryItemDetail("memory_7", "memory_7.jpg"),
                MemoryItemDetail("memory_8", "memory_8.jpg"),
                MemoryItemDetail("memory_9", "memory_9.jpg")
            )
        ),
        2019 to mapOf(
            "February 10" to listOf(
                MemoryItemDetail("memory_10", "memory_10.jpg"),
                MemoryItemDetail("memory_11", "memory_11.jpg")
            )
        )
    )

    val memories = memoriesByDate[selectedYear]?.get(selectedDate) ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Year and Memory Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedYear.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = selectedDate,
                        fontSize = 14.sp,
                        color = TextMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Photo Grid in Selection Mode
        item {
            PhotoGridDetail(
                memories = memories,
                selectedPhotos = selectedPhotos,
                onPhotoSelect = onPhotoSelect
            )
        }

        // Bottom spacing for action panel
        item {
            Box(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Grid layout for displaying memory photos with selection checkboxes
 *
 * @param memories List of memory items to display
 * @param selectedPhotos Set of selected photo IDs
 * @param onPhotoSelect Callback when a photo is selected
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoGridDetail(
    memories: List<MemoryItemDetail>,
    selectedPhotos: Set<String>,
    onPhotoSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        memories.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { memory ->
                    PhotoCardDetail(
                        isSelected = selectedPhotos.contains(memory.id),
                        onSelect = { onPhotoSelect(memory.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Empty space if odd number of items
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual photo card with selection checkbox overlay
 *
 * @param isSelected Whether this photo is selected
 * @param onSelect Callback when clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoCardDetail(
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .clickable { onSelect() }
    ) {
        // Placeholder for image
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "Photo",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            tint = TextMedium.copy(alpha = 0.5f)
        )

        // Selection Checkbox Overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )
            }
        }
    }
}

/**
 * Bottom action panel with Cancel, Share, and Delete buttons
 *
 * @param itemCount Number of selected items
 * @param onCancel Callback when cancel is clicked
 * @param onShare Callback when share is clicked
 * @param onDelete Callback when delete is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun BottomActionPanel(
    itemCount: Int,
    onCancel: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
    ) {
        // Selection info
        if (itemCount > 0) {
            Text(
                text = "Select items",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp,
                color = TextMedium
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGray,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✕ Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Share Button
            Button(
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGray,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⤴ Share",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Delete Button
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGray,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗑 Delete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Data class representing a memory item for detail screen
 *
 * @param id Unique identifier
 * @param filename Filename of the photo
 */
data class MemoryItemDetail(
    val id: String,
    val filename: String
)

