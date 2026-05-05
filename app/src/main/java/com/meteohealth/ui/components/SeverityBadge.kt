package com.meteohealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meteohealth.domain.WellbeingCalculator.Severity
import com.meteohealth.ui.dashboard.toColor
import com.meteohealth.ui.dashboard.toLabel

/**
 * Трёхцветный «светофор» с подписью текущего уровня риска.
 * Активная точка светится цветом уровня, остальные — приглушённые.
 */
@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SeverityDot(Severity.GREEN, isActive = severity == Severity.GREEN)
        SeverityDot(Severity.YELLOW, isActive = severity == Severity.YELLOW)
        SeverityDot(Severity.RED, isActive = severity == Severity.RED)
        Text(
            text = severity.toLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = severity.toColor()
        )
    }
}

@Composable
private fun SeverityDot(severity: Severity, isActive: Boolean) {
    val color = if (isActive) severity.toColor()
    else severity.toColor().copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .size(if (isActive) 12.dp else 10.dp)
            .background(color, CircleShape)
    )
}
