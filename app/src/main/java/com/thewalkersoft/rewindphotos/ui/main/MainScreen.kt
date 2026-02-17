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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White
import java.util.Calendar
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Main Gallery Screen composable
 *
 * Displays photo memories organized by date with:
 * - Top navigation bar with settings and grid view toggle
 * - Date selector with month/year navigation
 * - Year filter buttons (dynamically calculated from gallery photos)
 * - Memory cards showing photos from a specific date across multiple years
 * - Count of memories for each date
 *
 * @param modifier Modifier for styling
 * @param onSettingsClick Callback when settings icon is clicked
 * @param onPhotoClick Callback when a photo is clicked
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedYear = remember { mutableStateOf<Int?>(null) }
    val selectedMonth = remember { mutableStateOf(0) }
    val selectedDay = remember { mutableStateOf(1) }
    val showDatePicker = remember { mutableStateOf(false) }

    when (val state = uiState) {
        is MainUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MainUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        is MainUiState.Empty -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No photos available",
                    color = TextMedium,
                    fontSize = 16.sp
                )
            }
        }

        is MainUiState.Success -> {
            // Set initial selected year if not set
            if (selectedYear.value == null) {
                selectedYear.value = state.availableYears.firstOrNull()
            }
            if (selectedMonth.value == 0 && selectedDay.value == 1) {
                selectedMonth.value = state.currentMonth
                selectedDay.value = state.currentDay
            }

            if (showDatePicker.value) {
                val initialDateMillis = remember(
                    selectedYear.value,
                    selectedMonth.value,
                    selectedDay.value
                ) {
                    val calendar = Calendar.getInstance(Locale.getDefault()).apply {
                        set(
                            selectedYear.value ?: get(Calendar.YEAR),
                            selectedMonth.value,
                            selectedDay.value
                        )
                    }
                    calendar.timeInMillis
                }

                key(initialDateMillis) {
                    val datePickerState = androidx.compose.material3.rememberDatePickerState(
                        initialSelectedDateMillis = initialDateMillis
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker.value = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val selectedMillis = datePickerState.selectedDateMillis
                                    if (selectedMillis != null) {
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = selectedMillis
                                        }
                                        selectedYear.value = calendar.get(Calendar.YEAR)
                                        selectedMonth.value = calendar.get(Calendar.MONTH)
                                        selectedDay.value = calendar.get(Calendar.DAY_OF_MONTH)
                                    }
                                    showDatePicker.value = false
                                }
                            ) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker.value = false }) {
                                Text(text = stringResource(id = android.R.string.cancel))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

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
                    currentMonth = selectedMonth.value,
                    currentDay = selectedDay.value,
                    onPreviousDate = {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, selectedMonth.value)
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay.value)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        selectedMonth.value = calendar.get(Calendar.MONTH)
                        selectedDay.value = calendar.get(Calendar.DAY_OF_MONTH)
                    },
                    onNextDate = {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, selectedMonth.value)
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay.value)
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        selectedMonth.value = calendar.get(Calendar.MONTH)
                        selectedDay.value = calendar.get(Calendar.DAY_OF_MONTH)
                    },
                    onCalendarClick = { showDatePicker.value = true }
                )

                // Year Filter Buttons
                YearFilterRow(
                    selectedYear = selectedYear.value,
                    availableYears = state.availableYears,
                    onYearSelected = { year -> selectedYear.value = year }
                )

                // Memory Cards List
                MemoryCardsList(
                    selectedYear = selectedYear.value ?: state.availableYears.first(),
                    selectedMonth = selectedMonth.value,
                    selectedDay = selectedDay.value,
                    allPhotos = state.allPhotos,
                    onPhotoClick = onPhotoClick
                )
            }
        }
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

//        // Grid View Toggle
//        IconButton(
//            onClick = { /* Handle grid view toggle */ },
//            modifier = Modifier.size(40.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Filled.GridView,
//                contentDescription = "Grid View",
//                modifier = Modifier.size(24.dp),
//                tint = TextDark
//            )
//        }
    }
}

/**
 * Date navigation bar showing current date with previous/next navigation
 *
 * @param currentMonth Current selected month (0-11)
 * @param currentDay Current selected day (1-31)
 * @param onPreviousDate Callback for previous date navigation
 * @param onNextDate Callback for next date navigation
 * @param modifier Modifier for styling
 */
@Composable
private fun DateNavigationBar(
    currentMonth: Int,
    currentDay: Int,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentDate = "${monthNames[currentMonth]} $currentDay"

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
            onClick = onCalendarClick,
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
 * @param availableYears List of available years from gallery photos
 * @param onYearSelected Callback when a year is selected
 * @param modifier Modifier for styling
 */
@Composable
private fun YearFilterRow(
    selectedYear: Int?,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        availableYears.forEach { year ->
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
 * @param selectedMonth The currently selected month (0-11)
 * @param selectedDay The currently selected day (1-31)
 * @param allPhotos All photos from the gallery
 * @param onPhotoClick Callback when a photo is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun MemoryCardsList(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    allPhotos: List<Photo>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter photos for the selected year and date (month/day only, ignore year)
    val photosForDate = allPhotos.filter { photo ->
        val photoCalendar = Calendar.getInstance()
        photoCalendar.timeInMillis = photo.dateTaken
        val photoYear = photoCalendar.get(Calendar.YEAR)
        val photoMonth = photoCalendar.get(Calendar.MONTH)
        val photoDay = photoCalendar.get(Calendar.DAY_OF_MONTH)

        photoYear == selectedYear && photoMonth == selectedMonth && photoDay == selectedDay
    }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val dateString = "${monthNames[selectedMonth]} $selectedDay"

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
                        text = dateString,
                        fontSize = 14.sp,
                        color = TextMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "${photosForDate.size} memories",
                    fontSize = 14.sp,
                    color = TextMedium
                )
            }
        }

        // Photo Grid
        if (photosForDate.isNotEmpty()) {
            item {
                PhotoGrid(
                    photos = photosForDate,
                    onPhotoClick = onPhotoClick
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No photos found for this date",
                        color = TextMedium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Grid layout for displaying memory photos (2 columns)
 *
 * @param photos List of photo objects to display
 * @param onPhotoClick Callback when a photo is clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoGrid(
    photos: List<Photo>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        photos.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { photo ->
                    PhotoCard(
                        photo = photo,
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
 * @param photo Photo object to display
 * @param onPhotoClick Callback when clicked
 * @param modifier Modifier for styling
 */
@Composable
private fun PhotoCard(
    photo: Photo,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .clickable { onPhotoClick(photo.id.toString()) },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = "Photo",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            onError = {
                // If image fails to load, show placeholder
            }
        )
    }
}
