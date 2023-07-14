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

package com.example.publisherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.publisherapp.model.PageData
import com.example.publisherapp.theme.PublisherAppTheme
import com.example.publisherapp.ui.PageContent
import com.example.publisherapp.viewmodel.PrivacySandboxViewModel
import com.example.sspsdk.SspSdkImpl

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the SSP SDK
        val sspSdk = try {
            SspSdkImpl(this)
        } catch(e: Exception) {
            Log.e("Flight Publisher", "Error initializing the SSP SDK", e)
            null
        }

        val viewModel = PrivacySandboxViewModel(sspSdk)
        viewModel.initializeFromStaticData(getPageData(this))

        setContent {
            PublisherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PageContent(viewModel)
                }
            }
        }
    }
}

private fun getPageData(context: Context): PageData {
    val res = context.resources
    val data = PageData(
        title = res.getString(R.string.title),
        subtitle = res.getString(R.string.subtitle),
        author = res.getString(R.string.author),
        metadata = res.getString(R.string.metadata),
        paragraphs = res.getStringArray(R.array.paragraphs).toList()
    )
    return data
}
