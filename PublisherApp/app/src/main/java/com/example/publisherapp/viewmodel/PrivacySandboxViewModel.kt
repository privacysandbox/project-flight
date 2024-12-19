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

package com.example.publisherapp.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import com.example.publisherapp.model.PageData
import com.example.sspsdk.SspSdkImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrivacySandboxViewModel(
    private val sspSdk: SspSdkImpl?
) : ViewModel() {

    private val _uiState = MutableStateFlow(PageData())
    val uiState: StateFlow<PageData> = _uiState.asStateFlow()

    init {
        _uiState.update { currentState ->
            currentState.copy(
                adUrl = retrieveAd()
            )
        }
    }

    fun initializeFromStaticData(data: PageData) {
        _uiState.update { currentState ->
            currentState.copy(
                title = data.title,
                subtitle = data.subtitle,
                author = data.author,
                metadata = data.metadata,
                paragraphs = data.paragraphs
            )
        }
    }

    /**
     * Uses Protected Audience to retrieve an ad to show in the article.
     */
    @SuppressLint("NewApi")
    private fun retrieveAd(): String? {
        sspSdk?.let {
            try {
                // Retrieve the ad to render. Here we are rending the ad immediately, but
                // this outcome can be fetched early and then delay rendering the ad.
                val outcome = it.runAdSelection().get()
                outcome?.let {
                    val adSelectionId = it.adSelectionId
                    sspSdk?.let {
                        it.reportImpression(adSelectionId).get()
                        it.reportEvent(adSelectionId, "view", "{example_event_data: \"ad viewed\"}")
                    }
                    return it.renderUri.toString()
                }
            } catch (e: Exception) {
                Log.e("Flight Publisher", "runAdSelection failed", e)
            }
        }
        return null
    }

    /**
     * Uses Attribution Reporting API to register that the ad was clicked.
     */
    fun registerSource(event: MotionEvent): String? {
        sspSdk?.let {
            return try {
                sspSdk.registerSource(getAttributionIdentifier(), event).get()
            } catch (e: Exception) {
                "registerSource failed"
            }
        }
        return null
    }

    /*
     * In this example, the identifier will take the last part of the rendering URL to identify
     * which ad was shown, for example "Athens". In production, the identifier can be anything
     * that is best suited for what is being measured.
     */
    private fun getAttributionIdentifier(): String {
        val adUrl = _uiState.value.adUrl
        adUrl?.let {
            val identifier = it.substringAfterLast("/")
            return identifier.substring(0, identifier.length - 4) // -4 removes ".jpg"
        }
        return "URL_EMPTY"
    }

}