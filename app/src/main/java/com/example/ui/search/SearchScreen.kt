package com.example.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.ContactRowItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.DialerViewModel

@Composable
fun SearchScreen(
    viewModel: DialerViewModel,
    onInitiateCall: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar Area: header title "Search"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("search_screen_title")
            )
        }

        // Result list area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (query.isEmpty()) {
                // Friendly search guidance empty state panel
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Global Search",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Type any alphabet or sequence of numbers in the input box below to instantly crawl your directory logs with high definition tags.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (results.isEmpty()) {
                // No results matches state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⚠️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matches found",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No contact profile matches the key sequence \"$query\"",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_results_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp) // Leave roomy bottom padding for query capsule
                ) {
                    items(results, key = { it.id }) { contact ->
                        SearchMatchRow(
                            contact = contact,
                            query = query,
                            onCallClick = { onInitiateCall(contact.name, contact.phone) },
                            onClick = { onInitiateCall(contact.name, contact.phone) }
                        )
                    }
                }
            }
        }

        // Floating Navigation Query Box (Wide horizontal capsule component)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp) // Sit beautifully above expanding bottom bars
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(ContainerLevel1)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left-aligned search icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Active text entry input field
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input_field"),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(ConfirmAction),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Enter subscriber name or digits...",
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (query.isNotEmpty()) {
                    // Right-aligned cross-mark vector button (X) to easily wipe the search cache
                    IconButton(
                        onClick = { viewModel.updateSearchQuery("") },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Wipe Search Box",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchMatchRow(
    contact: Contact,
    query: String,
    onCallClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ContainerLevel1)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Monogram
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(ContainerLevel2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ". ${contact.name.firstOrNull()?.uppercase() ?: "#"} .",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Stack
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // High highlight rendering
            val highlightedName = buildAnnotatedString {
                val fullText = contact.name
                val index = fullText.indexOf(query, ignoreCase = true)
                if (index != -1 && query.isNotEmpty()) {
                    append(fullText.substring(0, index))
                    // Apply visual SearchHighlight yellow color properties
                    val spanStyle = SpanStyle(
                        color = SearchHighlight,
                        fontWeight = FontWeight.Black
                    )
                    pushStyle(spanStyle)
                    append(fullText.substring(index, index + query.length))
                    pop()
                    append(fullText.substring(index + query.length))
                } else {
                    append(fullText)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = highlightedName,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Capabilities HD badge
                Box(
                    modifier = Modifier
                        .border(1.dp, ConfirmAction.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "HD",
                        color = ConfirmAction,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle text (with matching phone or info highlighted)
            val highlightedPhone = buildAnnotatedString {
                val fullPhone = contact.phone
                val index = fullPhone.indexOf(query, ignoreCase = true)
                if (index != -1 && query.isNotEmpty()) {
                    append(fullPhone.substring(0, index))
                    val spanStyle = SpanStyle(
                        color = SearchHighlight,
                        fontWeight = FontWeight.Black
                    )
                    pushStyle(spanStyle)
                    append(fullPhone.substring(index, index + query.length))
                    pop()
                    append(fullPhone.substring(index + query.length))
                } else {
                    append(fullPhone)
                }
            }

            Text(
                text = highlightedPhone,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Handset icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(InteractivePillTrack)
                .clickable { onCallClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("📞", fontSize = 14.sp)
        }
    }
}
