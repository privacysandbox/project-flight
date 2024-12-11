/*
 * Copyright 2024 The Android Open Source Project
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

import com.example.publisherapp.model.PageData
import org.junit.Assert
import org.junit.Test

class PrivacySandboxViewModelTest {
    @Test
    fun initialUiState() {
        // TODO: replace null param with a mocked SspSdkImpl
        val privacySandboxViewModel = PrivacySandboxViewModel(null)
        val initialUiState = privacySandboxViewModel.uiState.value

        // default uninitialized values
        Assert.assertEquals(
            PageData("", "", "", "", listOf(""), null),
            initialUiState)
    }

    @Test
    fun initializeFromStaticData() {
        // TODO: replace null param with a mocked SspSdkImpl
        val privacySandboxViewModel = PrivacySandboxViewModel(null)

        val updatedFields = PageData(
            "my-title",
            "my-subtitle",
            "my-author",
            "my-metadata",
            listOf("paragraph-1", "paragraph-2"),
            "my-url"
        )
        privacySandboxViewModel.initializeFromStaticData(updatedFields)

        val updatedUiState = privacySandboxViewModel.uiState.value
        Assert.assertEquals(updatedFields, updatedUiState)
    }
}