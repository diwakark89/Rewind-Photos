package com.thewalkersoft.rewindphotos.ui.main

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.White
import androidx.compose.material.icons.filled.Image

/**
 * Main Gallery Screen composable
 *
 * Displays photo memories organized by date with:
 * - Top navigation bar with settings and grid view toggle
 * - Date selector (February 10) with month/year navigation
 * - Year filter buttons (2025, 2024, 2019)
 * - Memory cards showing photos from a specific date
 * - Count of memories for each date
 *
 * @param modifier Modifier for styling
 * @param onSettingsClick Callback when settings icon is clicked
 * @param onPhotoClick Callback when a photo is clicked
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {}
) {
    val selectedYear = remember { mutableStateOf(2025) }
    val selectedDate = remember { mutableStateOf("February 10") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top Action Bar
        TopActionBar(
            onSettingsClick = onSettingsClick
        )

        // Date Navigation and Filter
        DateNavigationBar(
            currentDate = selectedDate.value,
            onPreviousDate = { selectedDate.value = "February 9" },
            onNextDate = { selectedDate.value = "February 11" }
        )

        // Year Filter Buttons
        YearFilterRow(
            selectedYear = selectedYear.value,
            onYearSelected = { year -> selectedYear.value = year }
        )

        // Memory Cards List
        MemoryCardsList(
            selectedYear = selectedYear.value,
            selectedDate = selectedDate.value,
            onPhotoClick = onPhotoClick
        )
    }
}

/**
 * Top action bar with settings icon and grid view toggle
 *
 * @param onSettingsClick Callback when settings is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun TopActionBar(
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

        // Grid View Toggle
        IconButton(
            onClick = { /* Handle grid view toggle */ },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GridView,
                contentDescription = "Grid View",
                modifier = Modifier.size(24.dp),
                tint = TextDark
            )
        }
    }
}

/**
 * Date navigation bar showing current date with previous/next navigation
 *
 * @param currentDate Current selected date string
 * @param onPreviousDate Callback for previous date navigation
 * @param onNextDate Callback for next date navigation
 * @param modifier Modifier for styling
 */
@Composable
private fun DateNavigationBar(
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
 * Year filter buttons allowing selection between different years
 *
 * @param selectedYear Currently selected year
 * @param onYearSelected Callback when a year is selected
 * @param modifier Modifier for styling
 */
@Composable
private fun YearFilterRow(
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
            YearFilterButton(
                year = year,
                isSelected = selectedYear == year,
                onSelect = { onYearSelected(year) }
            )
        }
    }
}

/**
 * Individual year filter button
 *
 * @param year Year value
 * @param isSelected Whether this year is currently selected
 * @param onSelect Callback when selected
 * @param modifier Modifier for styling
 */
@Composable
private fun YearFilterButton(
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
 * Memory cards list showing photos for the selected date
 *
 * @param selectedYear The currently selected year
 * @param selectedDate The currently selected date
 * @param onPhotoClick Callback when a photo is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun MemoryCardsList(
    selectedYear: Int,
    selectedDate: String,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data for demonstration
    val memoriesByDate = mapOf(
        2025 to mapOf(
            "February 10" to listOf(
                MemoryItem("memory_1", "memory_1.jpg"),
                MemoryItem("memory_2", "memory_2.jpg"),
                MemoryItem("memory_3", "memory_3.jpg"),
                MemoryItem("memory_4", "memory_4.jpg"),
                MemoryItem("memory_5", "memory_5.jpg"),
                MemoryItem("memory_6", "memory_6.jpg")
            )
        ),
        2024 to mapOf(
            "February 10" to listOf(
                MemoryItem("memory_7", "memory_7.jpg"),
                MemoryItem("memory_8", "memory_8.jpg"),
                MemoryItem("memory_9", "memory_9.jpg")
            )
        ),
        2019 to mapOf(
            "February 10" to listOf(
                MemoryItem("memory_10", "memory_10.jpg"),
                MemoryItem("memory_11", "memory_11.jpg")
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
                Text(
                    text = "${memories.size} memories",
                    fontSize = 14.sp,
                    color = TextMedium
                )
            }
        }

        // Photo Grid
        item {
            PhotoGrid(
                memories = memories,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

/**
 * Grid layout for displaying memory photos (2 columns)
 *
 * @param memories List of memory items to display
 * @param onPhotoClick Callback when a photo is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoGrid(
    memories: List<MemoryItem>,
    onPhotoClick: (String) -> Unit,
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
                    PhotoCard(
                        memory = memory,
                        onPhotoClick = onPhotoClick,
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
 * Individual photo card in the memory grid
 *
 * @param memory Memory item to display
 * @param onPhotoClick Callback when clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoCard(
    memory: MemoryItem,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .clickable { onPhotoClick(memory.id) },
        contentAlignment = Alignment.Center
    ) {
        // Placeholder for image
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "Photo",
            modifier = Modifier.size(48.dp),
            tint = TextMedium.copy(alpha = 0.5f)
        )
    }
}

/**
 * Data class representing a memory item
 *
 * @param id Unique identifier
 * @param filename Filename of the photo
 */
data class MemoryItem(
    val id: String,
    val filename: String
)

