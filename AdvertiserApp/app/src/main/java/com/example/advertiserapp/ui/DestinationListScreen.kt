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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.advertiserapp.R
import com.example.advertiserapp.data.Destination

@Composable
fun DestinationListScreen(
    onClick: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    DestinationListScreen(getDestinations(), modifier, onClick)
}

@Composable
fun DestinationListScreen(
    destinations: ArrayList<Destination>,
    modifier: Modifier = Modifier,
    onClick: (Destination) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.testTag("destination_list"),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(id = R.dimen.card_side_margin),
            vertical = dimensionResource(id = R.dimen.header_margin)
        )
    ) {
        items(
            items = destinations,
            key = { it.id }
        ) { destination ->
            DestinationListItem(destination = destination) {
                onClick(destination)
            }
        }
    }
}

@Composable
private fun getDestinations() : ArrayList<Destination> {
    val returnArray = ArrayList<Destination>()
    val destinations: Array<String> = stringArrayResource(R.array.destinations)
    for (i in destinations.indices) {
        returnArray.add(
            Destination(i,
                destinations[i],
                getDescription(destinations[i]),
                getImageId(destinations[i])))
    }

    return returnArray
}

@Composable
private fun getDescription(destination: String) : String {
    return when (destination) {
        "Athens" -> stringResource(R.string.athens_desc)
        "Berlin" -> stringResource(R.string.berlin_desc)
        "Cairo" -> stringResource(R.string.cairo_desc)
        "Delhi" -> stringResource(R.string.delhi_desc)
        else -> stringResource(R.string.lorem_ipsum)
    }
}

@Composable
fun getImageId(destination: String) : Int {
    return when (destination) {
        "Athens" -> R.drawable.athens
        "Berlin" -> R.drawable.berlin
        "Cairo" -> R.drawable.cairo
        else -> R.drawable.generic
    }
}