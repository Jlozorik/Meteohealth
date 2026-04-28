package com.meteohealth.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteohealth.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    realDestination: String?,
    onFinished: (String) -> Unit
) {
    var minDelayElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        minDelayElapsed = true
    }

    LaunchedEffect(minDelayElapsed, realDestination) {
        if (minDelayElapsed && realDestination != null) {
            onFinished(realDestination)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0E1A), Color(0xFF0F1E38))
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(R.drawable.meteohealth_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            )

            Spacer(Modifier.height(52.dp))

            Text(
                "Метео·Здоровье",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "METEOHEALTH",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = Color(0xFF4DA6FF).copy(alpha = 0.65f)
            )
        }

        Text(
            "Дышите спокойно",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}
