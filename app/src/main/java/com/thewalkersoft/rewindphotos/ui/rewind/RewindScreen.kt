package com.thewalkersoft.rewindphotos.ui.rewind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White
import java.util.Calendar
import java.util.Locale

/**
 * Rewind Screen composable displaying all memories organized by month sections
 *
 * Features:
 * - Top navigation bar with settings icon and date picker
 * - Date selector with Previous/Next navigation buttons
 * - Year filter chips (dynamically calculated from gallery photos)
 * - Scrollable rewind view showing memories grouped by month
 * - Each month section displays location info and memory count
 * - Grid layout for memory photos (2-column)
 * - Auto-scroll to current date on app load
 * - Auto-scroll to selected date when date changes
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
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedDay by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var isScrolling by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollRequest by viewModel.scrollRequest.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

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
            // Initialize defaults only once
            if (selectedYear == -1) {
                selectedYear = state.currentYear
                selectedMonth = state.currentMonth
                selectedDay = state.currentDay
            }

            val context = LocalContext.current
            val imageLoader = context.imageLoader
            val prefetchSizePx = with(LocalDensity.current) { 200.dp.roundToPx() }

            LaunchedEffect(selectedYear, state.groupedPhotosData.sections) {
                val firstSection = state.groupedPhotosData.sections.firstOrNull { it.year == selectedYear }
                firstSection?.photos?.take(12)?.forEach { photo ->
                    val imageRequest = ImageRequest.Builder(context)
                        .data(photo.uri)
                        .memoryCacheKey("photo_${photo.id}")
                        .diskCacheKey("photo_${photo.id}")
                        .size(prefetchSizePx, prefetchSizePx)
                        .build()
                    imageLoader.enqueue(imageRequest)
                }
            }

            // Auto-scroll to target section with safety check and loading indicator
            LaunchedEffect(scrollRequest) {
                val index = scrollRequest.index
                if (index >= 0 && index < state.groupedPhotosData.sections.size) {
                    if (scrollRequest.animate) {
                        isScrolling = true
                        try {
                            lazyListState.animateScrollToItem(index)
                        } catch (e: Exception) {
                            android.util.Log.e("RewindScreen", "Error scrolling to index: $index", e)
                        } finally {
                            isScrolling = false
                        }
                    } else {
                        isScrolling = false
                        lazyListState.scrollToItem(index)
                    }
                }
            }

            // Show date picker if requested
            if (showDatePicker.value) {
                val initialDateMillis = remember(selectedYear, selectedMonth, selectedDay) {
                    val calendar = Calendar.getInstance(Locale.getDefault()).apply {
                        set(selectedYear, selectedMonth, selectedDay)
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
                                        // Trigger scroll to new date
                                        viewModel.onDateSelected(selectedYear, selectedMonth, selectedDay)
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

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Date Navigation Bar
                    RewindDateNavigationBar(
                        currentMonth = selectedMonth,
                        currentDay = selectedDay,
                        onSettingsClick = onSettingsClick,
                        onPreviousClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.YEAR, selectedYear)
                            calendar.set(Calendar.MONTH, selectedMonth)
                            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                            calendar.add(Calendar.DAY_OF_MONTH, -1)
                            selectedYear = calendar.get(Calendar.YEAR)
                            selectedMonth = calendar.get(Calendar.MONTH)
                            selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                            viewModel.onDateSelected(selectedYear, selectedMonth, selectedDay)
                        },
                        onNextClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.YEAR, selectedYear)
                            calendar.set(Calendar.MONTH, selectedMonth)
                            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            selectedYear = calendar.get(Calendar.YEAR)
                            selectedMonth = calendar.get(Calendar.MONTH)
                            selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                            viewModel.onDateSelected(selectedYear, selectedMonth, selectedDay)
                        },
                        onCalendarClick = { showDatePicker.value = true }
                    )

                    // Year Filter Chips
                    RewindYearFilterRow(
                        years = state.availableYears,
                        selectedYear = selectedYear,
                        onYearSelected = { year ->
                            selectedYear = year
                            viewModel.onYearSelected(year)
                        }
                    )

                    // Month-grouped Content
                    RewindContent(
                        groupedPhotosData = state.groupedPhotosData,
                        lazyListState = lazyListState,
                        onPhotoClick = onPhotoClick,
                        onPhotosLongPress = onPhotosLongPress
                    )
                }

                // Loading overlay during scroll transitions
                if (isScrolling) {
                    ScrollingLoadingOverlay(selectedYear, selectedMonth, selectedDay)
                }
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
                .clickable { }
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
 * Rewind content showing memories grouped by month with location info
 */
@Composable
private fun RewindContent(
    groupedPhotosData: com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData,
    lazyListState: LazyListState,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = groupedPhotosData.sections

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        items(
            count = sections.size,
            key = { index -> "${sections[index].year}_${sections[index].month}" }
        ) { sectionIndex ->
            val section = sections[sectionIndex]
            RewindMonthSection(
                monthSection = section,
                onPhotoClick = onPhotoClick,
                onPhotosLongPress = onPhotosLongPress
            )
        }
    }
}

/**
 * Month section composable showing month header with location and photo grid
 * Includes intelligent image preloading for better performance
 */
@Composable
private fun RewindMonthSection(
    monthSection: com.thewalkersoft.rewindphotos.domain.model.MonthSection,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames[monthSection.month]
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // Preload first few photos from this month section when it becomes visible
    // This ensures smooth rendering and faster navigation back to this section
    LaunchedEffect(monthSection.year, monthSection.month) {
        // Preload first 6 photos in background using the singleton ImageLoader
        monthSection.photos.take(6).forEach { photo ->
            try {
                val imageRequest = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .memoryCacheKey("photo_${photo.id}")
                    .diskCacheKey("photo_${photo.id}")
                    .size(200, 200)
                    .build()

                // Enqueue for caching using the singleton ImageLoader
                imageLoader.enqueue(imageRequest)
            } catch (e: Exception) {
                // Silent fail - preloading is non-critical
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month Header with Location and Memory Count
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "$monthName ${monthSection.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            // Location subtitle (if available)
            if (!monthSection.location.isNullOrEmpty()) {
                Text(
                    text = monthSection.location,
                    fontSize = 12.sp,
                    color = TextMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Memory count
            Text(
                text = "${monthSection.photoCount} memories",
                fontSize = 12.sp,
                color = TextMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Photo Grid
        RewindPhotoGrid(
            photos = monthSection.photos,
            onPhotoClick = onPhotoClick,
            onPhotosLongPress = onPhotosLongPress
        )
    }
}

/**
 * Rewind photo grid - 3 column layout with optimized rendering
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
        // Chunked rendering for efficient three-column layout
        photos.chunked(3).forEachIndexed { _, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { _, photo ->
                    // Use unique key for efficient recomposition
                    key(photo.id) {
                        RewindPhotoItem(
                            photo = photo,
                            onPhotoClick = onPhotoClick,
                            onPhotosLongPress = onPhotosLongPress,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Spacers for rows with less than 3 items
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Rewind photo item card - optimized for performance with lazy loading and aggressive caching
 * Uses adaptive sizing with aspectRatio for responsive 3x3 grid
 */
@Composable
private fun RewindPhotoItem(
    photo: com.thewalkersoft.rewindphotos.domain.model.Photo,
    onPhotoClick: (String) -> Unit,
    onPhotosLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPhotoClick(photo.id.toString()) },
                    onLongPress = { onPhotosLongPress() }
                )
            }
    ) {
        val sizePx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val imageRequest = remember(photo.id, sizePx) {
            ImageRequest.Builder(context)
                .data(photo.uri)
                .crossfade(durationMillis = 150)
                .size(sizePx, sizePx)
                .precision(Precision.INEXACT)
                .memoryCacheKey("photo_${photo.id}")
                .diskCacheKey("photo_${photo.id}")
                .build()
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * Loading overlay composable displayed during date transitions.
 * Shows a loading indicator and message when jumping to past or future dates.
 */
@Composable
private fun ScrollingLoadingOverlay(
    year: Int,
    month: Int,
    day: Int,
    modifier: Modifier = Modifier
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames[month]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    // Determine if we're jumping to past or future
    val timeDirection = when {
        year < currentYear -> "Past"
        year > currentYear -> "Future"
        month < currentMonth -> "Earlier in the year"
        month > currentMonth -> "Later in the year"
        else -> "This date"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 24.dp),
                color = Orange,
                strokeWidth = 4.dp
            )

            Text(
                text = "Jumping to the $timeDirection",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "$monthName $day, $year",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Loading your memories...",
                fontSize = 14.sp,
                color = TextMedium,
                textAlign = TextAlign.Center
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
