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

package com.example.advertiserapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.advertiserapp.data.Destination
import com.example.mmpsdk.MmpSdkImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrivacySandboxViewModel(
    private val mmpSdk: MmpSdkImpl?
) : ViewModel() {

    private val _detailState = MutableStateFlow(Destination())
    val detailState: StateFlow<Destination> = _detailState.asStateFlow()

    fun updateDestination(data: Destination) {
        _detailState.update {currentState ->
            currentState.copy(
                id = data.id,
                name = data.name,
                description = data.description,
                imageId = data.imageId
            )
        }
    }
    fun joinCustomAudience(ca: String) {
        // Join a custom audience when the user presses on a destination
        // This custom audience allows us to target the user with ads
        // specific to the destination they are interested in
        mmpSdk?.let {
            it.joinCustomAudience(ca)
        }
    }

    fun registerTrigger(attributionIdentifier: String): String? {
        mmpSdk?.let {
            return try {
                it.registerTrigger(attributionIdentifier).get()
            } catch (e: Exception) {
                "registerTrigger failed"
            }
        }
        return null
    }
}