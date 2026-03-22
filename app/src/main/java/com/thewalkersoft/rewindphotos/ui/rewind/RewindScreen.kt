package com.thewalkersoft.rewindphotos.ui.rewind

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import com.thewalkersoft.rewindphotos.domain.model.GroupedPhotosData
import com.thewalkersoft.rewindphotos.domain.model.MediaType
import com.thewalkersoft.rewindphotos.domain.model.MonthSection
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.ui.preview.PreviewData
import com.thewalkersoft.rewindphotos.ui.theme.LightGray
import com.thewalkersoft.rewindphotos.ui.theme.Orange
import com.thewalkersoft.rewindphotos.ui.theme.RewindPhotosTheme
import com.thewalkersoft.rewindphotos.ui.theme.TextDark
import com.thewalkersoft.rewindphotos.ui.theme.TextMedium
import com.thewalkersoft.rewindphotos.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
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
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    viewModel: RewindViewModel = hiltViewModel()
) {
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedDay by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var isScrolling by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollRequest by viewModel.scrollRequest.collectAsStateWithLifecycle()
    val selectedPhotos by viewModel.selectedPhotos.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val deleteIntentSender by viewModel.deleteIntentSender.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    // Handle back button - clear selection if in selection mode, otherwise exit app
    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    // Activity result launcher for delete permission (Android 13+)
    val deletePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            android.util.Log.d("RewindScreen", "User granted delete permission")
            viewModel.onDeletePermissionGranted()
        } else {
            android.util.Log.w("RewindScreen", "User denied delete permission")
            viewModel.onDeletePermissionDenied()
        }
    }

    // Launch delete permission dialog when intentSender is available
    LaunchedEffect(deleteIntentSender) {
        deleteIntentSender?.let { sender ->
            try {
                android.util.Log.d("RewindScreen", "Launching delete permission dialog")
                val request = IntentSenderRequest.Builder(sender).build()
                deletePermissionLauncher.launch(request)
                viewModel.clearDeleteIntent()
            } catch (e: Exception) {
                android.util.Log.e("RewindScreen", "Error launching delete dialog: ${e.message}", e)
                viewModel.clearDeleteIntent()
            }
        }
    }

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
                    delay(6)
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
                        yield()
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

                    // Selection Action Bar (shown when in selection mode) - NOW BELOW YEAR FILTER
                    if (isSelectionMode) {
                        RewindSelectionActionBar(
                            selectedCount = selectedPhotos.size,
                            totalPhotos = state.groupedPhotosData.sections.sumOf { it.photoCount },
                            onSelectAll = { viewModel.selectAllPhotos() },
                            onShare = {
                                val uris = viewModel.getSelectedPhotoUris()
                                sharePhotos(context, uris)
                            },
                            onDelete = {
                                android.util.Log.d("RewindScreen", "DELETE BUTTON CLICKED - Selected: ${selectedPhotos.size} photos")
                                showToast(context, "Deleting ${selectedPhotos.size} photos...")
                                viewModel.deleteSelectedPhotos()
                            },
                            onCancel = { viewModel.clearSelection() }
                        )
                    }

                    // Month-grouped Content
                    RewindContent(
                        groupedPhotosData = state.groupedPhotosData,
                        lazyListState = lazyListState,
                        selectedPhotos = selectedPhotos,
                        isSelectionMode = isSelectionMode,
                        onPhotoClick = onPhotoClick,
                        onPhotoLongPress = { photoId ->
                            viewModel.togglePhotoSelection(photoId)
                        },
                        onSelectAllMonth = { year, month ->
                            viewModel.selectAllPhotosInMonth(year, month)
                        }
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
 * Share multiple photos using Android intent
 */
private fun sharePhotos(context: Context, photoUris: List<String>) {
    if (photoUris.isEmpty()) return

    val uris = photoUris.mapNotNull { uriString ->
        try {
            uriString.toUri()
        } catch (_: Exception) {
            null
        }
    }

    if (uris.isEmpty()) return

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(Intent.createChooser(shareIntent, "Share Photos"))
    } catch (e: Exception) {
        android.util.Log.e("RewindScreen", "Error sharing photos", e)
    }
}

/**
 * Show toast message
 */
private fun showToast(context: Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}

/**
 * Rewind selection action bar showing selected count and action buttons
 */
@Composable
private fun RewindSelectionActionBar(
    selectedCount: Int,
    totalPhotos: Int,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Orange)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancel Selection",
                    modifier = Modifier.size(28.dp),
                    tint = White
                )
            }
            Text(
                text = "$selectedCount selected",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Select All with Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSelectAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedCount == totalPhotos && totalPhotos > 0,
                    onCheckedChange = null,
                    modifier = Modifier.size(26.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = White,
                        uncheckedColor = White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "Select All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = White,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            // Share button
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(26.dp),
                    tint = White
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(26.dp),
                    tint = White
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
    groupedPhotosData: GroupedPhotosData,
    lazyListState: LazyListState,
    selectedPhotos: Set<Long>,
    isSelectionMode: Boolean,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPhotoLongPress: (Long) -> Unit = {},
    onSelectAllMonth: (Int, Int) -> Unit = { _, _ -> }
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
                selectedPhotos = selectedPhotos,
                isSelectionMode = isSelectionMode,
                onPhotoClick = onPhotoClick,
                onPhotoLongPress = onPhotoLongPress,
                onSelectAllMonth = onSelectAllMonth
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
    monthSection: MonthSection,
    selectedPhotos: Set<Long>,
    isSelectionMode: Boolean,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPhotoLongPress: (Long) -> Unit = {},
    onSelectAllMonth: (Int, Int) -> Unit = { _, _ -> }
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames[monthSection.month]
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    val totalPhotos = monthSection.photos.size
    var visiblePhotoCount by remember(monthSection.year, monthSection.month) {
        mutableIntStateOf(kotlin.math.min(6, totalPhotos))
    }

    LaunchedEffect(monthSection.year, monthSection.month, totalPhotos) {
        var currentCount = visiblePhotoCount
        while (currentCount < totalPhotos) {
            delay(32)
            currentCount = kotlin.math.min(currentCount + 6, totalPhotos)
            visiblePhotoCount = currentCount
        }
    }

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
            } catch (_: Exception) {
                // Silent fail - preloading is non-critical
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month Header with Location and Memory Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            // Select All checkbox for this month (only in selection mode)
            if (isSelectionMode) {
                val monthPhotosIds = monthSection.photos.map { it.id }.toSet()
                val allPhotosInMonthSelected = monthPhotosIds.all { it in selectedPhotos }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSelectAllMonth(monthSection.year, monthSection.month)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = allPhotosInMonthSelected,
                        onCheckedChange = null,
                        modifier = Modifier.size(28.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Orange,
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }
        }

        // Photo Grid
        RewindPhotoGrid(
            photos = monthSection.photos.take(visiblePhotoCount),
            selectedPhotos = selectedPhotos,
            isSelectionMode = isSelectionMode,
            onPhotoClick = onPhotoClick,
            onPhotoLongPress = onPhotoLongPress
        )
    }
}

/**
 * Rewind photo grid - 3 column layout with optimized rendering
 */
@Composable
private fun RewindPhotoGrid(
    photos: List<Photo>,
    selectedPhotos: Set<Long>,
    isSelectionMode: Boolean,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPhotoLongPress: (Long) -> Unit = {}
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
                            isSelected = photo.id in selectedPhotos,
                            isSelectionMode = isSelectionMode,
                            onPhotoClick = onPhotoClick,
                            onPhotoLongPress = onPhotoLongPress,
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
 * Supports long-press selection with visual feedback
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RewindPhotoItem(
    photo: Photo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPhotoLongPress: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val rowWidthDp = configuration.screenWidthDp.dp - 48.dp
    val cellSizePx = with(density) { (rowWidthDp / 3).roundToPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = {
                    // If in selection mode, toggle selection on click
                    // Otherwise, open the photo/video
                    if (isSelectionMode) {
                        onPhotoLongPress(photo.id)
                    } else {
                        // For videos, launch system video player
                        if (photo.mediaType == MediaType.VIDEO) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(photo.uri.toUri(), "video/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("RewindScreen", "Failed to open video", e)
                            }
                        } else {
                            // For images, navigate to detail screen
                            onPhotoClick(photo.id.toString())
                        }
                    }
                },
                onLongClick = { onPhotoLongPress(photo.id) }
            )
    ) {
        val imageRequest = remember(photo.id, cellSizePx) {
            ImageRequest.Builder(context)
                .data(photo.uri)
                .crossfade(durationMillis = 150)
                .size(cellSizePx, cellSizePx)
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
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 0.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Video indicator overlay (shown when not in selection mode)
                if (!isSelectionMode && photo.mediaType == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Video",
                            tint = White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Selection overlay
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSelected)
                                    Orange.copy(alpha = 0.3f)
                                else
                                    Color.Transparent
                            )
                    )

                    // Checkbox in corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(White.copy(alpha = 0.9f))
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Orange,
                                uncheckedColor = Color.Gray
                            )
                        )
                    }
                }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
            ) {
                RewindDateNavigationBar(
                    currentMonth = 1,
                    currentDay = 14,
                    onSettingsClick = {},
                    onPreviousClick = {},
                    onNextClick = {},
                    onCalendarClick = {}
                )
                RewindYearFilterRow(
                    years = PreviewData.availableYears,
                    selectedYear = PreviewData.availableYears.first(),
                    onYearSelected = {}
                )
                RewindContent(
                    groupedPhotosData = PreviewData.groupedPhotosData,
                    lazyListState = rememberLazyListState(),
                    selectedPhotos = emptySet(),
                    isSelectionMode = false,
                    onPhotoClick = {}
                )
            }
        }
    }
}
