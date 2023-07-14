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

package com.example.publisherapp.ui

import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.publisherapp.R
import com.example.publisherapp.model.PageData
import com.example.publisherapp.viewmodel.PrivacySandboxViewModel

private val defaultSpacerSize = 16.dp
@Composable
fun PageContent(
    viewModel: PrivacySandboxViewModel
) {
    val uiState = viewModel.uiState.collectAsState()
    LazyColumn(
        state = rememberLazyListState()
    ) {
        postContentItems(viewModel, uiState.value)
    }
}

fun LazyListScope.postContentItems(viewModel: PrivacySandboxViewModel, data: PageData) {
    val sidePadding = Modifier.padding(horizontal = defaultSpacerSize)
    item {
        HeaderImage()
        Spacer(Modifier.height(defaultSpacerSize))
        Text(data.title, style = MaterialTheme.typography.headlineLarge, modifier = sidePadding)
        Spacer(Modifier.height(8.dp))
        Text(data.subtitle, style = MaterialTheme.typography.bodyMedium, modifier = sidePadding)
        Spacer(Modifier.height(defaultSpacerSize))
        ArticleInfo(data)
        Paragraph(data.paragraphs.get(0))
        AdBox(viewModel, data)
        Spacer(Modifier.height(24.dp))
    }
    items(data.paragraphs.subList(1, data.paragraphs.size)) { Paragraph(content = it) }
}

@Composable
private fun HeaderImage() {
    val imageModifier = Modifier
        .heightIn(min = 180.dp)
        .fillMaxWidth()
    Image(
        painter = painterResource(R.drawable.news_header),
        contentDescription = null, // no-op
        modifier = imageModifier
    )
}

@Composable
private fun AdBox(viewModel: PrivacySandboxViewModel, data: PageData) {
    val imageModifier = Modifier
        .heightIn(min = 180.dp)
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .clip(shape = MaterialTheme.shapes.medium)

    if (!data.adUrl.isNullOrEmpty()) {
        AndroidView(
            factory = {
                val webview = WebView(it)
                webview.setOnTouchListener(getTouchListener(viewModel))
                webview.apply {
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = WebViewClient()
                    loadUrl(data.adUrl!!)
                }
            },
            modifier = imageModifier
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ad_placeholder),
            contentDescription = null, // no-op
            modifier = imageModifier
        )
    }
}

private fun getTouchListener(viewModel: PrivacySandboxViewModel): View.OnTouchListener {
    return View.OnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            var outcome = viewModel.registerSource(event)
            outcome?.let {
                Toast.makeText(view.context, outcome, Toast.LENGTH_SHORT).show()
            }
        }
        false
    }
}

@Composable
private fun ArticleInfo(data: PageData) {
    Row(
        modifier = Modifier
            .padding(bottom = 24.dp, start = defaultSpacerSize, end = defaultSpacerSize)
            // Merge semantics so accessibility services consider this row a single element
            .semantics(mergeDescendants = true) {}
    ) {
        Image(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null, // no-op
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = data.author,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = data.metadata,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Paragraph(content: String) {
    val modifier = Modifier
        .padding(bottom = 24.dp, start = defaultSpacerSize, end = defaultSpacerSize)
    Box(modifier) {
        Text(
            text = content,
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
        )
    }
}