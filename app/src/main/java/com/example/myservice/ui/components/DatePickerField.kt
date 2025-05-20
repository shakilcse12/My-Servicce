package com.example.myservice.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
fun DatePickerField(
    labelText: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    // Consider making the formatter a remember {} constant if reused often
    val dateFormatter = remember { DateTimeFormatter.ISO_DATE }

    // Use a Box to contain the visual TextField and the clickable overlay
    Box(
        modifier = modifier // Apply the modifier passed to this function here
            .padding(vertical = 8.dp) // Apply padding here if needed, or outside
    ) {
        // 1. The visual OutlinedTextField (non-interactive)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {}, // Not directly editable
            readOnly = true,    // Mark as read-only
            label = { Text(labelText) },
            // Add a trailing icon as a visual cue that it's clickable/interactive
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Collection Date"
                )
            },
            modifier = Modifier.fillMaxWidth(), // Fill width within the Box
            // Prevent the TextField itself from handling interactions or showing ripple
            interactionSource = remember { MutableInteractionSource() }
            // Optional: Customize colors for readOnly state if needed
        )

        // 2. Transparent Clickable Overlay
        // This Box sits on top of the OutlinedTextField
        Box(
            modifier = Modifier
                .matchParentSize() // Makes this Box cover the OutlinedTextField
                .clickable(
                    // Indicate the purpose of the click clearly
                    onClickLabel = labelText,
                    onClick = { showDatePicker = true }, // Action to show the dialog
                    // Disable ripple effect for the transparent overlay itself
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
        )
    } // End of container Box

    // --- Date Picker Dialog Logic (remains mostly the same) ---
    if (showDatePicker) {
        // Remember state for the DatePicker Dialog
        val datePickerState = rememberDatePickerState(
            // Optionally initialize with the currently selected date
            initialSelectedDateMillis = try {
                if (selectedDate.isNotEmpty()) {
                    LocalDate.parse(selectedDate, dateFormatter)
                        .atStartOfDay(java.time.ZoneOffset.UTC) // Use UTC or system default ZoneId
                        .toInstant()
                        .toEpochMilli()
                } else null
            } catch (e: Exception) { null /* Handle parse error */ }
        )

        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Get selected millis, default to current date if null? Optional.
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            // Convert millis to LocalDate using UTC epoch day
                            val localDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                .atZone(java.time.ZoneOffset.UTC) // Use UTC Zone
                                .toLocalDate()
                            onDateSelected(localDate.format(dateFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            // Add a dismiss button for better UX
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
