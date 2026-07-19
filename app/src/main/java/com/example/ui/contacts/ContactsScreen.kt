package com.example.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.ContactRowItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.DialerViewModel

@Composable
fun ContactsScreen(
    viewModel: DialerViewModel,
    onInitiateCall: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.allContacts.collectAsState()

    // Separate Carlcare Service Hotline from the general sorted directory list
    val (hotlines, regularContacts) = remember(contacts) {
        contacts.partition { it.name.contains("Carlcare", ignoreCase = true) }
    }

    val favorites = remember(regularContacts) {
        regularContacts.filter { it.isFavorite }
    }

    // Group contacts alphabetically
    val groupedRegular = remember(regularContacts) {
        regularContacts.groupBy { it.sectionHeader }.toSortedMap()
    }

    val groups by viewModel.allGroups.collectAsState()

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showManageGroupsDialog by remember { mutableStateOf(false) }
    var selectedContactForDetails by remember { mutableStateOf<Contact?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar Area: Large "Contacts" title, left-aligned, matching the system overflow menu icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Contacts",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.testTag("contacts_screen_title")
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showAddContactDialog = true },
                    modifier = Modifier.testTag("add_contact_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = { showManageGroupsDialog = true },
                    modifier = Modifier.testTag("contacts_menu_overflow")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("contacts_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Cushion above floating dock
        ) {
            // 1. "Your Profile" consolidated card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ContainerLevel1)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Profile info */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ContainerLevel2, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ME", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("My Profile", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Willy Kez", color = TextSecondary, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(
                        color = ContainerLevel2,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showManageGroupsDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ContainerLevel2, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👥", color = TextPrimary, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("My Groups", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            val groupsNames = remember(groups) {
                                if (groups.isEmpty()) "Work, Family, Friends" else groups.joinToString { it.name }
                            }
                            Text(groupsNames, color = TextSecondary, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }
            }

            // 2. "Favorite Contacts" Sticky Section
            if (favorites.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorite Contacts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ConfirmAction,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favorites.forEach { favorite ->
                            ContactRowItem(
                                primaryText = favorite.name,
                                secondaryText = favorite.phone,
                                onCallClick = { onInitiateCall(favorite.name, favorite.phone) },
                                onClick = { selectedContactForDetails = favorite }
                            )
                        }
                    }
                }
            }

            // 3. A-Z Directory List Index (alphabetical contact names inside structural dark cards)
            groupedRegular.forEach { (header, itemsInGroup) ->
                item {
                    Column {
                        // Letter Header
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ConfirmAction,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsInGroup.forEach { contact ->
                                ContactRowItem(
                                    primaryText = contact.name,
                                    secondaryText = contact.phone,
                                    onCallClick = { onInitiateCall(contact.name, contact.phone) },
                                    onClick = { selectedContactForDetails = contact }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Persistent Corporate Hotline Accent Component (at base of directory list)
            if (hotlines.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Support",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    hotlines.forEach { hotline ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ContainerLevel2)
                                .clickable { onInitiateCall(hotline.name, hotline.phone) }
                                .padding(16.dp)
                                .testTag("corporate_hotline_card"),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Corporate square icon asset
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ConfirmAction),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CC",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = hotline.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = hotline.phone,
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(InteractivePillTrack, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📞", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Contact Quick Dialog with Group Selection supported directly
    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var selectedGroupId by remember { mutableStateOf<Long?>(null) }
        var showGroupDropdown by remember { mutableStateOf(false) }

        val activeGroupStr = remember(selectedGroupId, groups) {
            groups.find { it.id == selectedGroupId }?.name ?: "(No Group Assigned)"
        }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add Contact", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ContainerLevel1,
                            unfocusedContainerColor = ContainerLevel1
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ContainerLevel1,
                            unfocusedContainerColor = ContainerLevel1
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showGroupDropdown = !showGroupDropdown },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ContainerLevel1)
                        ) {
                            Text("Group: $activeGroupStr", color = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showGroupDropdown,
                            onDismissRequest = { showGroupDropdown = false },
                            modifier = Modifier.background(ContainerLevel2)
                        ) {
                            DropdownMenuItem(
                                text = { Text("(No Group)", color = TextPrimary) },
                                onClick = {
                                    selectedGroupId = null
                                    showGroupDropdown = false
                                }
                            )
                            groups.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name, color = TextPrimary) },
                                    onClick = {
                                        selectedGroupId = g.id
                                        showGroupDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            viewModel.addContact(name, phone, selectedGroupId)
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("Save", color = ConfirmAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = ContainerLevel2
        )
    }

    // Manage Groups Dialog overlay
    if (showManageGroupsDialog) {
        var newGroupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManageGroupsDialog = false },
            title = { Text("Manage Groups", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            placeholder = { Text("New Group Name...") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ContainerLevel1,
                                unfocusedContainerColor = ContainerLevel1
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newGroupName.isNotBlank()) {
                                    viewModel.createGroup(newGroupName.trim())
                                    newGroupName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ConfirmAction)
                        ) {
                            Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (groups.isEmpty()) {
                        Text("No custom groups folder setup yet.", color = TextSecondary)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(groups) { group ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ContainerLevel1)
                                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(group.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    IconButton(
                                        onClick = { viewModel.deleteGroup(group) }
                                    ) {
                                        Text("❌", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageGroupsDialog = false }) {
                    Text("Done", color = ConfirmAction)
                }
            },
            containerColor = ContainerLevel2
        )
    }

    // Detailed Contact interactive panel Overlay
    if (selectedContactForDetails != null) {
        val contact = selectedContactForDetails!!
        val contactGroup = groups.find { it.id == contact.groupId }
        var showGroupAssignDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedContactForDetails = null },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(InteractivePillTrack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.firstOrNull()?.uppercase() ?: "#",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(contact.name, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Text(contact.phone, color = TextSecondary, fontSize = 15.sp)
                    if (contactGroup != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ConfirmAction.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = contactGroup.name,
                                color = ConfirmAction,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onInitiateCall(contact.name, contact.phone)
                            selectedContactForDetails = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ConfirmAction)
                    ) {
                        Text("Call Number", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.toggleContactFavorite(contact)
                            selectedContactForDetails = contact.copy(isFavorite = !contact.isFavorite)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ContainerLevel1)
                    ) {
                        val favText = if (contact.isFavorite) "⭐ Favorited (Tap to Unpin)" else "☆ Pin to Favorites"
                        Text(favText, color = TextPrimary)
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showGroupAssignDropdown = !showGroupAssignDropdown },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ContainerLevel1)
                        ) {
                            Text("Set Custom Group: ${contactGroup?.name ?: "None"}", color = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showGroupAssignDropdown,
                            onDismissRequest = { showGroupAssignDropdown = false },
                            modifier = Modifier.background(ContainerLevel2)
                        ) {
                            DropdownMenuItem(
                                text = { Text("(No Group)", color = TextPrimary) },
                                onClick = {
                                    viewModel.assignContactToGroup(contact.id, null)
                                    selectedContactForDetails = contact.copy(groupId = null)
                                    showGroupAssignDropdown = false
                                }
                            )
                            groups.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name, color = TextPrimary) },
                                    onClick = {
                                        viewModel.assignContactToGroup(contact.id, g.id)
                                        selectedContactForDetails = contact.copy(groupId = g.id)
                                        showGroupAssignDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.deleteContact(contact)
                            selectedContactForDetails = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DestructiveAction.copy(alpha = 0.2f))
                    ) {
                        Text("Delete Contact", color = DestructiveAction, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedContactForDetails = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = ContainerLevel2
        )
    }
}
