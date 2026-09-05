package com.healthyscan.app.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.local.ScanHistoryEntity
import com.healthyscan.app.data.model.ScoreBand
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.ui.components.HealthyScanTopBar
import com.healthyscan.app.ui.components.ScorePill
import com.healthyscan.app.ui.components.colorForBand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    productRepository: ProductRepository,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val history by productRepository.allHistory().collectAsState(initial = emptyList())
    val dateFormat = remember(currentLanguage) {
        SimpleDateFormat("dd MMM, HH:mm", if (currentLanguage == "el") Locale("el") else Locale.ENGLISH)
    }

    Scaffold(
        topBar = {
            HealthyScanTopBar(
                title = stringResource(R.string.history_title),
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.favorites_empty), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.history_products_count, history.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(history) { scan ->
                    HistoryRow(scan, dateFormat.format(Date(scan.timestampMillis))) { onOpenProduct(scan.barcode) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(scan: ScanHistoryEntity, dateText: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(scan.name, fontWeight = FontWeight.Medium)
                Text(dateText, style = MaterialTheme.typography.bodyMedium)
            }
            val band = ScoreBand.forScore(scan.score)
            ScorePill(text = "${scan.score}/100", color = colorForBand(band))
        }
    }
}
