package com.example.logger.presentation.splash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextAlign
import com.example.logger.R
import com.example.logger.ui.theme.LoggerTheme

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    onConfigureSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📊", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.size(24.dp))
        Text(
            text = stringResource(id = R.string.splash_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W500),
            color = Color(0xFF6200EA),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(id = R.string.splash_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(48.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Dashboard, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(id = R.string.splash_get_started))
            }
            Spacer(Modifier.size(12.dp))
            Button(onClick = onConfigureSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Settings, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(id = R.string.splash_configure_settings))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    LoggerTheme {
        SplashScreen(onGetStarted = {}, onConfigureSettings = {})
    }
}
