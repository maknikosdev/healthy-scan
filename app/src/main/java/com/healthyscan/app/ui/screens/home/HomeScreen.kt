package com.healthyscan.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.local.ScanHistoryEntity
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.ui.components.HealthyScanTopBar
import com.healthyscan.app.ui.components.ScorePill
import com.healthyscan.app.ui.components.colorForBand
import com.healthyscan.app.data.model.ScoreBand
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    productRepository: ProductRepository,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onScanBarcode: () -> Unit,
    onSearch: () -> Unit,
    onScanLabel: () -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val recentScans by productRepository.recentScans(5).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            HealthyScanTopBar(
                title = stringResource(R.string.app_name),
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            item {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                HomeActionCard(
                    icon = Icons.Filled.CameraAlt,
                    title = stringResource(R.string.home_scan_barcode_title),
                    subtitle = stringResource(R.string.home_scan_barcode_subtitle),
                    onClick = onScanBarcode
                )
            }
            item {
                HomeActionCard(
                    icon = Icons.Filled.Search,
                    title = stringResource(R.string.home_search_title),
                    subtitle = stringResource(R.string.home_search_subtitle),
                    onClick = onSearch
                )
            }
            item {
                HomeActionCard(
                    icon = Icons.Filled.DocumentScanner,
                    title = stringResource(R.string.home_scan_label_title),
                    subtitle = stringResource(R.string.home_scan_label_subtitle),
                    onClick = onScanLabel
                )
            }

            if (recentScans.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.home_recent_scans),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                items(recentScans, key = { it.id }) { scan ->
                    RecentScanRow(
                        scan = scan,
                        onClick = { onOpenProduct(scan.barcode) },
                        onDelete = { scope.launch { productRepository.deleteHistoryItem(scan.id) } }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HomeActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RecentScanRow(
    scan: ScanHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(scan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                scan.brand?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val band = ScoreBand.forScore(scan.score)
                ScorePill(text = "${scan.score}/100", color = colorForBand(band))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}
