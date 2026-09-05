package com.healthyscan.app.ui.screens.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R

private data class PlanColumn(val titleRes: Int, val featureResIds: List<Int>, val highlighted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val free = PlanColumn(
        R.string.premium_free,
        listOf(
            R.string.premium_free_1, R.string.premium_free_2, R.string.premium_free_3,
            R.string.premium_free_4, R.string.premium_free_5
        ),
        highlighted = false
    )
    val premium = PlanColumn(
        R.string.premium_premium,
        listOf(
            R.string.premium_paid_1, R.string.premium_paid_2, R.string.premium_paid_3,
            R.string.premium_paid_4, R.string.premium_paid_5, R.string.premium_paid_6,
            R.string.premium_paid_7, R.string.premium_paid_8, R.string.premium_paid_9
        ),
        highlighted = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(listOf(free, premium)) { plan ->
                PlanCard(plan)
            }
            item {
                Button(onClick = { /* Wire up Play Billing here when ready to launch Premium */ }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.premium_premium))
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: PlanColumn) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(plan.titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            plan.featureResIds.forEach { res ->
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(res))
                }
            }
        }
    }
}
