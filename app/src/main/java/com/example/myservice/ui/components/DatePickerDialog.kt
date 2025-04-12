package com.example.myservice.ui.components

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        val context = LocalContext.current

        DisposableEffect(Unit) {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                initialDate.year,
                initialDate.monthValue - 1,  // Android DatePicker uses 0-based months
                initialDate.dayOfMonth
            )

            dialog.setOnDismissListener {
                onDismiss()
            }

            dialog.show()

            onDispose {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
    }
}