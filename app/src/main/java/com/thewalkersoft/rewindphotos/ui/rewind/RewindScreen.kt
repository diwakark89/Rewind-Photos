package com.thewalkersoft.rewindphotos.ui.rewind

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

/**
 * Rewind Screen composable displaying memories organized by date and year
 *
 * Features:
 * - Top navigation bar with settings and layers icons
 * - Date selector with dropdown and navigation arrows
 * - Calendar and layers quick actions
 * - Year filter chips (dynamically calculated from gallery photos)
 * - Scrollable rewind view showing memories grouped by year
 * - Memory count for each date
 * - Grid layout for memory photos
 *
 * @param modifier Modifier for styling
 * @param onSettingsClick Callback when settings icon is clicked
 * @param onPhotoClick Callback when a photo is clicked
 * @param onPhotosLongPress Callback for entering selection mode
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onPhotosLongPress: () -> Unit = {},
    viewModel: RewindViewModel = hiltViewModel()
) {
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedDay by remember { mutableIntStateOf(-1) }
    val showDatePicker = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is RewindUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is RewindUiState.Error -> {
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

        is RewindUiState.Empty -> {
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

        is RewindUiState.Success -> {
            if (selectedYear == null) {
                selectedYear = state.availableYears.firstOrNull()
            }
            if (selectedMonth == -1 && selectedDay == -1) {
                selectedMonth = state.currentMonth
                selectedDay = state.currentDay
            }

            if (showDatePicker.value) {
                val initialDateMillis = remember(selectedYear, selectedMonth, selectedDay) {
                    val calendar = Calendar.getInstance(Locale.getDefault()).apply {
                        val safeMonth = if (selectedMonth >= 0) selectedMonth else get(Calendar.MONTH)
                        val safeDay = if (selectedDay >= 1) selectedDay else get(Calendar.DAY_OF_MONTH)
                        set(selectedYear ?: get(Calendar.YEAR), safeMonth, safeDay)
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
                                        selectedYear = calendar.get(Calendar.YEAR)
                                        selectedMonth = calendar.get(Calendar.MONTH)
                                        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
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
                // Date Navigation Bar
                RewindDateNavigationBar(
                    currentMonth = selectedMonth,
                    currentDay = selectedDay,
                    onSettingsClick = onSettingsClick,
                    onPreviousClick = {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, selectedMonth)
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        selectedMonth = calendar.get(Calendar.MONTH)
                        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                    },
                    onNextClick = {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, selectedMonth)
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        selectedMonth = calendar.get(Calendar.MONTH)
                        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                    },
                    onCalendarClick = { showDatePicker.value = true }
                )

                // Year Filter Chips
                RewindYearFilterRow(
                    years = state.availableYears,
                    selectedYear = selectedYear ?: state.availableYears.first(),
                    onYearSelected = { year -> selectedYear = year }
                )

                // Rewind Content
                RewindContent(
                    selectedYear = selectedYear ?: state.availableYears.first(),
                    selectedMonth = selectedMonth,
                    selectedDay = selectedDay,
                    allPhotos = state.allPhotos,
                    onPhotoClick = onPhotoClick,
                    onPhotosLongPress = onPhotosLongPress
                )
            }
        }
    }
}

/**
 * Rewind date navigation bar with date selector and action buttons
 */
@Composable
private fun RewindDateNavigationBar(
    currentMonth: Int,
    currentDay: Int,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCalendarClick: () -> Unit
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
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF5F5F5))
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
    }
}

/**
 * Rewind year filter chips row with dynamic years
 */
@Composable
private fun RewindYearFilterRow(
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
            RewindYearChip(
                year = year,
                isSelected = year == selectedYear,
                onClick = { onYearSelected(year) }
            )
        }
    }
}

/**
 * Individual year filter chip for rewind
 */
@Composable
private fun RewindYearChip(
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
 * Rewind content showing memories grouped by year
 */
@Composable
private fun RewindContent(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    allPhotos: List<com.thewalkersoft.rewindphotos.domain.model.Photo>,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val selectedDate = remember(selectedYear, selectedMonth, selectedDay, zoneId) {
        LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
    }
    val photosForDate = remember(allPhotos, selectedDate, zoneId) {
        allPhotos.filter { photo ->
            if (photo.dateTaken <= 0L) return@filter false
            val photoDate = Instant.ofEpochMilli(photo.dateTaken).atZone(zoneId).toLocalDate()
            photoDate == selectedDate
        }
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
            // Rewind and Memory Count Header
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
                RewindPhotoGrid(
                    photos = photosForDate,
                    onPhotoClick = onPhotoClick,
                    onPhotosLongPress = onPhotosLongPress
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
 * Rewind photo grid
 */
@Composable
private fun RewindPhotoGrid(
    photos: List<com.thewalkersoft.rewindphotos.domain.model.Photo>,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        photos.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { photo ->
                    RewindPhotoItem(
                        photo = photo,
                        onPhotoClick = onPhotoClick,
                        onPhotosLongPress = onPhotosLongPress,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Rewind photo item
 */
@Composable
private fun RewindPhotoItem(
    photo: com.thewalkersoft.rewindphotos.domain.model.Photo,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPhotoClick(photo.id.toString()) },
                    onLongPress = { onPhotosLongPress() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RewindScreenPreview() {
    RewindPhotosTheme {
        Surface {
            RewindScreen()
        }
    }
}
