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

package com.example.advertiserapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.advertiserapp.R
import com.example.advertiserapp.data.Destination
import com.example.advertiserapp.theme.Purple500

@Composable
fun DestinationListItem(
    destination: Destination,
    onClick: (Destination) -> Unit
) {
    Card (
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.card_side_margin))
            .padding(bottom = dimensionResource(id = R.dimen.card_bottom_margin))
            .clickable { onClick(destination) },
        elevation = CardDefaults.cardElevation(dimensionResource(id = R.dimen.card_elevation)),
        shape = MaterialTheme.shapes.small

    ) {
        Column(Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(destination.imageId), contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = destination.name,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Purple500,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }
    }
}