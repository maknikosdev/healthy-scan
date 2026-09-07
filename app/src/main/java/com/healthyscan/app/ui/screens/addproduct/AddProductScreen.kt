package com.healthyscan.app.ui.screens.addproduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.repository.ContributionResult
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.data.repository.SettingsRepository
import kotlinx.coroutines.launch

private enum class SubmitOutcome { NONE, SUCCESS, MISSING_CREDENTIALS, FAILURE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    barcode: String,
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository,
    prefilledName: String = "",
    prefilledBrand: String = "",
    prefilledIngredients: String = "",
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val savedUsername by settingsRepository.offUsername.collectAsState(initial = "")
    val savedPassword by settingsRepository.offPassword.collectAsState(initial = "")

    var name by remember { mutableStateOf(prefilledName) }
    var brand by remember { mutableStateOf(prefilledBrand) }
    var ingredients by remember { mutableStateOf(prefilledIngredients) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf(SubmitOutcome.NONE) }

    LaunchedEffect(savedUsername, savedPassword) {
        if (username.isBlank()) username = savedUsername
        if (password.isBlank()) password = savedPassword
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_product_title)) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_product_barcode)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.add_product_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(stringResource(R.string.add_product_brand)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text(stringResource(R.string.add_product_ingredients)) },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.add_product_off_account_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.add_product_off_account_body),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(stringResource(R.string.add_product_username)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.add_product_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            when (outcome) {
                SubmitOutcome.SUCCESS -> item {
                    Text(stringResource(R.string.add_product_success), color = MaterialTheme.colorScheme.primary)
                }
                SubmitOutcome.FAILURE -> item {
                    Text(stringResource(R.string.add_product_error), color = MaterialTheme.colorScheme.error)
                }
                SubmitOutcome.MISSING_CREDENTIALS -> item {
                    Text(stringResource(R.string.add_product_missing_credentials), color = MaterialTheme.colorScheme.error)
                }
                SubmitOutcome.NONE -> {}
            }

            item {
                if (isSubmitting) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                outcome = SubmitOutcome.MISSING_CREDENTIALS
                                return@Button
                            }
                            isSubmitting = true
                            scope.launch {
                                settingsRepository.setOffCredentials(username, password)
                                val result = productRepository.submitProductToOpenFoodFacts(
                                    barcode = barcode,
                                    productName = name,
                                    brand = brand,
                                    ingredientsText = ingredients,
                                    offUsername = username,
                                    offPassword = password
                                )
                                isSubmitting = false
                                outcome = when (result) {
                                    is ContributionResult.Success -> SubmitOutcome.SUCCESS
                                    is ContributionResult.Failure -> SubmitOutcome.FAILURE
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_product_submit))
                    }
                }
            }
        }
    }
}
