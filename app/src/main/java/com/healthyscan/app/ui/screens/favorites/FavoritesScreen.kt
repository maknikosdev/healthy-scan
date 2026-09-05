package com.healthyscan.app.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.local.FavoriteEntity
import com.healthyscan.app.data.model.ScoreBand
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.ui.components.HealthyScanTopBar
import com.healthyscan.app.ui.components.ScorePill
import com.healthyscan.app.ui.components.colorForBand

@Composable
fun FavoritesScreen(
    productRepository: ProductRepository,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val favorites by productRepository.favorites().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            HealthyScanTopBar(
                title = stringResource(R.string.favorites_title),
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
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
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(favorites) { fav ->
                    FavoriteRow(fav) { onOpenProduct(fav.barcode) }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(fav: FavoriteEntity, onClick: () -> Unit) {
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
                Text(fav.name, fontWeight = FontWeight.Medium)
                fav.brand?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            val band = ScoreBand.forScore(fav.score)
            ScorePill(text = "${fav.score}/100", color = colorForBand(band))
        }
    }
}
