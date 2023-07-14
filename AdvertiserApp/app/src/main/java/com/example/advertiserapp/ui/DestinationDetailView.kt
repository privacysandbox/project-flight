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

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.advertiserapp.MainActivity
import com.example.advertiserapp.R
import com.example.advertiserapp.theme.Purple500
import com.example.advertiserapp.viewmodel.PrivacySandboxViewModel

@Composable
fun DestinationDetailView(
    viewModel: PrivacySandboxViewModel,
    onHomeClick: (screen: Int) -> Unit
) {
    val destination = viewModel.detailState.collectAsState().value
    viewModel.joinCustomAudience(destination.name.lowercase())

    Text(
        text = "Visit ${destination.name}",
        style = MaterialTheme.typography.titleLarge,
        color = Purple500,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center),
        textAlign = TextAlign.Center,
    )
    Image(
        painter = painterResource(destination.imageId), contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
    )
    Text(
        text = destination.description,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        val context = LocalContext.current
        Button(
            onClick = {
                val outcome = viewModel.registerTrigger(destination.name)
                outcome?.let {
                    Toast.makeText(context, outcome, Toast.LENGTH_SHORT)
                }
                      },
            modifier = Modifier.padding(all = 14.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Purple500,
            )
        ) {
            Text(
                text = stringResource(R.string.book_trip),
                color = Color.White,
                fontSize = 22.sp
            )
        }
        Button(
            onClick = { onHomeClick(MainActivity.HOME_SCREEN) },
            modifier = Modifier.padding(all = 14.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Purple500,
            )
        ) {
            Text(
                text = stringResource(R.string.home),
                color = Color.White,
                fontSize = 22.sp
            )
        }
    }
}