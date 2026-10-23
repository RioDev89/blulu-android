package com.blulu.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.blulu.app.data.Providers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: ChatViewModel, onBack: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(vm.providerId) }
    val initial = remember(selected) { vm.config(selected) }
    var key by rememberSaveable(selected) { mutableStateOf(initial.key) }
    var model by rememberSaveable(selected) { mutableStateOf(initial.model) }
    var url by rememberSaveable(selected) { mutableStateOf(initial.baseUrl) }
    var showKey by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val provider = Providers.byId(selected)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = provider.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Provider") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Providers.all.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                selected = p.id
                                expanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("API key") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                singleLine = true,
                supportingText = { Text("Default: ${provider.defaultModel}") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Base URL (https)") },
                singleLine = true,
                enabled = provider.editableBaseUrl,
                supportingText = {
                    Text(
                        if (provider.editableBaseUrl) "Any OpenAI-compatible endpoint"
                        else "Fixed for ${provider.name}"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val baseUrl = if (provider.editableBaseUrl) url else provider.baseUrl
                    vm.saveConfig(selected, ProviderConfig(key, model, baseUrl))
                    vm.selectProvider(selected)
                    status = "Saved. ${provider.name} is now active."
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save settings")
            }

            status?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Text(
                "Keys are encrypted on this device with the Android Keystore. " +
                    "Messages go directly from this phone to the provider you choose.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
