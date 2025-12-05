/*
 * Copyright (c) 2024-present, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.android.reactagentforce

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider

/**
 * Activity that displays the Agentforce conversation UI
 * Uses Jetpack Compose to show the SDK-provided conversation interface
 */
class ServiceAgentConversationActivity : ComponentActivity() {

    companion object {
        private const val TAG = "ServiceAgentConvActivity"
    }

    private lateinit var viewModel: ServiceAgentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")

        // Get shared ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ServiceAgentViewModel::class.java]

        // Start conversation if not already started
        if (viewModel.conversation.value == null) {
            Log.d(TAG, "Starting new conversation")
            viewModel.startConversation()
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConversationScreen(
                        viewModel = viewModel,
                        onClose = {
                            Log.d(TAG, "Closing conversation")
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ServiceAgentViewModel,
    onClose: () -> Unit
) {
    val conversation by viewModel.conversation.collectAsState()
    val client = viewModel.agentforceClient

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agentforce Service Agent",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0176D3), // Salesforce blue
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(top = 50.dp, bottom = 0.dp),
                modifier = Modifier.heightIn(max = 95.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                conversation != null && client != null -> {
                    // Display the Agentforce Conversation Container with proper constraints
                    client.AgentforceConversationContainer(
                        conversation = conversation!!,
                        onClose = onClose
                    )
                }
                else -> {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Initializing conversation...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

