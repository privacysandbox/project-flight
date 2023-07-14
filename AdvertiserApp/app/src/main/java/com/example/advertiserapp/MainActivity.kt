/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.advertiserapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.advertiserapp.data.Destination
import com.example.advertiserapp.theme.AdvertiserAppTheme
import com.example.advertiserapp.theme.Purple500
import com.example.advertiserapp.ui.DestinationDetailView
import com.example.advertiserapp.ui.DestinationListScreen
import com.example.advertiserapp.viewmodel.PrivacySandboxViewModel
import com.example.mmpsdk.MmpSdkImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the MMP SDK
        val mmpSdk = try {
            MmpSdkImpl(this)
        } catch (e: Exception) {
            Log.e("Flight Advertiser", "Error initializing the MMP SDK", e)
            null
        }

        val viewModel = PrivacySandboxViewModel(mmpSdk)

        setContent {
            AdvertiserAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    TravelHomeApp(viewModel)
                }
            }
        }
    }

    companion object {
        const val HOME_SCREEN = 99
    }
}

@Composable
fun TravelHomeApp(viewModel: PrivacySandboxViewModel) {
    var screen by remember { mutableStateOf(MainActivity.HOME_SCREEN) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (screen) {
            MainActivity.HOME_SCREEN -> {
                Column {
                    TopTitleBar()
                    DestinationListScreen(
                        onClick = {
                            screen = it.id
                            viewModel.updateDestination(it)
                                  },
                        Modifier.fillMaxSize())
                }
            }

            else -> {
                Column {
                DestinationDetailView(viewModel = viewModel, onHomeClick = { screen = it })
                }
            }
        }
    }
}

@Composable
fun TopTitleBar() {
    Text(
        text = stringResource(R.string.header_text),
        style = MaterialTheme.typography.titleLarge,
        color = Purple500,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center),
        textAlign = TextAlign.Center,
    )
}
