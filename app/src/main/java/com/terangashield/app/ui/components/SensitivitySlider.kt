package com.terangashield.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.terangashield.app.R
import com.terangashield.app.domain.model.DetectionSensitivity

@Composable
fun SensitivitySlider(
    value: DetectionSensitivity,
    onValueChange: (DetectionSensitivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = listOf(DetectionSensitivity.LOW, DetectionSensitivity.MEDIUM, DetectionSensitivity.HIGH)
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = steps.indexOf(value).toFloat(),
            onValueChange = { onValueChange(steps[it.toInt().coerceIn(0, steps.size - 1)]) },
            valueRange = 0f..(steps.size - 1).toFloat(),
            steps = steps.size - 2,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.sensitivity_low), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(R.string.sensitivity_medium), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(R.string.sensitivity_high), style = MaterialTheme.typography.labelMedium)
        }
    }
}
