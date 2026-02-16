package com.thewalkersoft.rewindphotos.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White

/**
 * Selection Screen composable for selecting and managing photos
 *
 * Features:
 * - Top navigation bar with cancel button (X icon in orange circle)
 * - Date selector with dropdown and navigation arrows
 * - Calendar and layers quick actions
 * - Year filter chips (2025, 2024, 2019)
 * - Scrollable timeline showing memories with selection checkboxes
 * - Bottom action bar with Cancel, Share, and Delete options
 * - Multi-select functionality with visual feedback
 *
 * @param modifier Modifier for styling
 * @param onExitSelectionMode Callback to exit selection mode
 * @param onShareSelected Callback when share is clicked
 * @param onDeleteSelected Callback when delete is clicked
 */
@Composable
fun SelectionScreen(
    modifier: Modifier = Modifier,
    onExitSelectionMode: () -> Unit = {},
    onShareSelected: (List<String>) -> Unit = {},
    onDeleteSelected: (List<String>) -> Unit = {}
) {
    val selectedPhotos = remember { mutableStateListOf<String>() }
    val selectedYear = remember { mutableStateOf(2025) }
    val selectedDate = remember { mutableStateOf("February 10") }
    val years = listOf(2025, 2024, 2019)

    Scaffold(
        bottomBar = {
            SelectionBottomBar(
                selectedCount = selectedPhotos.size,
                onCancel = onExitSelectionMode,
                onShare = { onShareSelected(selectedPhotos.toList()) },
                onDelete = { onDeleteSelected(selectedPhotos.toList()) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(White)
                .padding(paddingValues)
        ) {
            // Top Action Bar with Cancel button
            SelectionTopBar(
                onCancelClick = onExitSelectionMode
            )

            // Date Navigation Bar
            DateNavigationBar(
                currentDate = selectedDate.value,
                onDateClick = { /* Handle date picker */ },
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

            // Timeline Content with Selection
            SelectionTimelineContent(
                selectedYear = selectedYear.value,
                selectedDate = selectedDate.value,
                selectedPhotos = selectedPhotos,
                onPhotoToggle = { photoId ->
                    if (selectedPhotos.contains(photoId)) {
                        selectedPhotos.remove(photoId)
                    } else {
                        selectedPhotos.add(photoId)
                    }
                }
            )
        }
    }
}

/**
 * Top bar with cancel button in selection mode
 */
@Composable
private fun SelectionTopBar(
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cancel Button - Orange circle with X
        IconButton(
            onClick = onCancelClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Orange)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cancel Selection",
                modifier = Modifier.size(24.dp),
                tint = White
            )
        }

        // Empty space for symmetry (or could add select all)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

/**
 * Date navigation bar with date selector and action buttons
 */
@Composable
private fun DateNavigationBar(
    currentDate: String,
    onDateClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onLayersClick: () -> Unit,
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
        ) {
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
 * Timeline content in selection mode with checkboxes
 */
@Composable
private fun SelectionTimelineContent(
    selectedYear: Int,
    selectedDate: String,
    selectedPhotos: List<String>,
    onPhotoToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(White),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Year 2025 Section
        item {
            SelectionYearSection(
                year = 2025,
                date = "February 10, 2025",
                memoryCount = 11,
                selectedPhotos = selectedPhotos,
                onPhotoToggle = onPhotoToggle
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Year 2024 Section
        item {
            SelectionYearSection(
                year = 2024,
                date = "February 10, 2024",
                memoryCount = 8,
                selectedPhotos = selectedPhotos,
                onPhotoToggle = onPhotoToggle
            )
        }
    }
}

/**
 * Year section in selection mode
 */
@Composable
private fun SelectionYearSection(
    year: Int,
    date: String,
    memoryCount: Int,
    selectedPhotos: List<String>,
    onPhotoToggle: (String) -> Unit,
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
        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Memory Grid with Selection
        SelectionMemoryGrid(
            memoryCount = memoryCount,
            selectedPhotos = selectedPhotos,
            onPhotoToggle = onPhotoToggle
        )
    }
}

/**
 * Grid of memory photos with selection checkboxes
 */
@Composable
private fun SelectionMemoryGrid(
    memoryCount: Int,
    selectedPhotos: List<String>,
    onPhotoToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = (memoryCount + 1) / 2
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First item in row
                SelectionMemoryItem(
                    photoId = "photo_${rowIndex * 2}",
                    isSelected = selectedPhotos.contains("photo_${rowIndex * 2}"),
                    onToggle = onPhotoToggle,
                    modifier = Modifier.weight(1f)
                )

                // Second item in row (if exists)
                if (rowIndex * 2 + 1 < memoryCount) {
                    SelectionMemoryItem(
                        photoId = "photo_${rowIndex * 2 + 1}",
                        isSelected = selectedPhotos.contains("photo_${rowIndex * 2 + 1}"),
                        onToggle = onPhotoToggle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual memory item with selection checkbox
 */
@Composable
private fun SelectionMemoryItem(
    photoId: String,
    isSelected: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onToggle(photoId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Placeholder for actual photo
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = photoId,
                    fontSize = 14.sp,
                    color = TextMedium
                )
            }

            // Selection overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x40000000)) // Semi-transparent dark overlay
                )
            }

            // Selection checkbox (circle with checkmark or empty circle)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) White else Color(0x80FFFFFF))
                    .border(
                        width = 2.dp,
                        color = if (isSelected) White else Color(0x80FFFFFF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        modifier = Modifier.size(32.dp),
                        tint = Orange
                    )
                }
            }
        }
    }
}

/**
 * Bottom action bar for selection mode
 */
@Composable
private fun SelectionBottomBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Button
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancel",
                    modifier = Modifier.size(20.dp),
                    tint = TextDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = TextDark
                )
            }

            // Share Button
            TextButton(
                onClick = onShare,
                enabled = selectedCount > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(20.dp),
                    tint = if (selectedCount > 0) TextDark else TextMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share",
                    fontSize = 16.sp,
                    color = if (selectedCount > 0) TextDark else TextMedium
                )
            }

            // Delete Button
            TextButton(
                onClick = onDelete,
                enabled = selectedCount > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = if (selectedCount > 0) Color(0xFFE53935) else TextMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete",
                    fontSize = 16.sp,
                    color = if (selectedCount > 0) Color(0xFFE53935) else TextMedium
                )
            }
        }
    }
}

/**
 * Helper text showing selected count
 */
@Composable
private fun SelectionInfo(
    selectedCount: Int,
    modifier: Modifier = Modifier
) {
    if (selectedCount > 0) {
        Text(
            text = "Select items",
            fontSize = 14.sp,
            color = TextMedium,
            modifier = modifier
        )
    }
}

