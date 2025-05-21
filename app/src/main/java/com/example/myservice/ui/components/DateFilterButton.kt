package com.example.myservice.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateFilterButton(
    label: String,
    date: String?,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .height(48.dp), // consistent button height
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Calendar Icon",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = date?.let {
                try {
                    LocalDate.parse(it).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                } catch (e: Exception) {
                    label // fallback to label if parse fails
                }
            } ?: label,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
