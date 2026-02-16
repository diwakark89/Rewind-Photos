package com.thewalkersoft.rewindphotos.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White

/**
 * Timeline Screen composable displaying memories organized by date and year
 *
 * Features:
 * - Top navigation bar with settings and layers icons
 * - Date selector with dropdown and navigation arrows
 * - Calendar and layers quick actions
 * - Year filter chips (2025, 2024, 2019)
 * - Scrollable timeline showing memories grouped by year
 * - Memory count for each date
 * - Grid layout for memory photos
 *
 * @param modifier Modifier for styling
 * @param onSettingsClick Callback when settings icon is clicked
 * @param onPhotoClick Callback when a photo is clicked
 * @param onPhotosLongPress Callback for entering selection mode
 */
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onPhotosLongPress: () -> Unit = {}
) {
    val selectedYear = remember { mutableStateOf(2025) }
    val selectedDate = remember { mutableStateOf("February 10") }
    val years = listOf(2025, 2024, 2019)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Date Navigation Bar
        DateNavigationBar(
            currentDate = selectedDate.value,
            onDateClick = { /* Handle date picker */ },
            onSettingsClick = onSettingsClick,
            onPreviousClick = { /* Navigate to previous date */ },
            onNextClick = { /* Navigate to next date */ },
            onCalendarClick = { /* Open calendar */ },
            onLayersClick = { /* Open layers/filters */ }
        )

        // Year Filter Chips
        YearFilterRow(
            years = years,
            selectedYear = selectedYear.value,
            onYearSelected = { year -> selectedYear.value = year }
        )

        // Timeline Content
        TimelineContent(
            selectedYear = selectedYear.value,
            selectedDate = selectedDate.value,
            onPhotoClick = onPhotoClick,
            onPhotosLongPress = onPhotosLongPress
        )
    }
}

/**
 * Date navigation bar with date selector and action buttons
 */
@Composable
private fun DateNavigationBar(
    currentDate: String,
    modifier: Modifier = Modifier,
    onDateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onLayersClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp),
                tint = TextDark
            )
        }
        // Previous Date Button
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous Date",
                modifier = Modifier.size(28.dp),
                tint = TextDark
            )
        }

        // Date Selector with dropdown
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { onDateClick() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        )


        {
            Text(
                text = currentDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select Date",
                modifier = Modifier.size(20.dp),
                tint = TextDark
            )
        }

        // Next Date Button
        IconButton(
            onClick = onNextClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next Date",
                modifier = Modifier.size(28.dp),
                tint = TextDark
            )
        }

        // Calendar Button
        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Orange)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Calendar",
                modifier = Modifier.size(20.dp),
                tint = White
            )
        }

        // Layers Button
        IconButton(
            onClick = onLayersClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            Icon(
                imageVector = Icons.Filled.Layers,
                contentDescription = "Layers",
                modifier = Modifier.size(20.dp),
                tint = TextDark
            )
        }
    }
}

/**
 * Year filter chips row
 */
@Composable
private fun YearFilterRow(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(years) { year ->
            YearChip(
                year = year,
                isSelected = year == selectedYear,
                onClick = { onYearSelected(year) }
            )
        }
    }
}

/**
 * Individual year filter chip
 */
@Composable
private fun YearChip(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Orange else Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = year.toString(),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) White else TextDark
        )
    }
}

/**
 * Timeline content showing memories grouped by year
 */
@Composable
private fun TimelineContent(
    selectedYear: Int,
    selectedDate: String,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(White),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            YearSection(
                year = selectedYear,
                date = selectedDate,
                memoryCount = 11,
                onPhotoClick = onPhotoClick,
                onPhotosLongPress = onPhotosLongPress
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            YearSection(
                year = 2024,
                date = "February 10, 2024",
                memoryCount = 8,
                onPhotoClick = onPhotoClick,
                onPhotosLongPress = onPhotosLongPress
            )
        }
    }
}

/**
 * Year section with header and memory grid
 */
@Composable
private fun YearSection(
    year: Int,
    date: String,
    memoryCount: Int,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Year Header
        Text(
            text = year.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Date and memory count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextMedium
            )
            Text(
                text = "$memoryCount memories",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Memory Grid - showing placeholder screenshots
        MemoryGrid(
            memoryCount = memoryCount,
            onPhotoClick = onPhotoClick,
            onPhotosLongPress = onPhotosLongPress
        )
    }
}

/**
 * Grid of memory photos/screenshots
 */
@Composable
private fun MemoryGrid(
    memoryCount: Int,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create a grid layout with 2 columns
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Split memories into rows of 2
        val rows = (memoryCount + 1) / 2
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First item in row
                MemoryItem(
                    index = rowIndex * 2,
                    onPhotoClick = onPhotoClick,
                    onPhotosLongPress = onPhotosLongPress,
                    modifier = Modifier.weight(1f)
                )

                // Second item in row (if exists)
                if (rowIndex * 2 + 1 < memoryCount) {
                    MemoryItem(
                        index = rowIndex * 2 + 1,
                        onPhotoClick = onPhotoClick,
                        onPhotosLongPress = onPhotosLongPress,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Empty space to maintain grid alignment
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual memory item card
 */
@Composable
private fun MemoryItem(
    index: Int,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPhotoClick("photo_$index") },
                    onLongPress = { onPhotosLongPress() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for actual photo
            // In real implementation, use AsyncImage with photo URI
            Text(
                text = "Photo ${index + 1}",
                fontSize = 14.sp,
                color = TextMedium
            )
        }
    }
}
