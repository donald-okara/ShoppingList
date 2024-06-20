/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.home

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.item.DeleteConfirmationDialog
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val totalItemsPrice by viewModel.totalItemsPrice.collectAsState()
    val totalItemsInCartPrice by viewModel.totalItemsInCartPrice.collectAsState()
    val itemsInCart by viewModel.itemsInCart.collectAsState()
    val itemsNotInCart by viewModel.itemsNotInCart.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val selectedItems = remember { mutableStateOf(setOf<Int>()) }
    val selectedCartItems = remember { mutableStateOf(setOf<Int>()) }

    // Log the selected items whenever they change
    LaunchedEffect(selectedItems.value) {
        Log.d("HomeScreen", "Selected items: ${selectedItems.value}")
        Log.d("HomeScreen", "Selected cart items: ${selectedCartItems.value}")

    }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }
    var deleteConfirmationForAllRequired by rememberSaveable { mutableStateOf(false) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            HomeBottomSheetContent(
                itemsInCart = itemsInCart,
                totalItemsPrice = totalItemsPrice,
                totalItemsInCartPrice = totalItemsInCartPrice,
                selectedItems = selectedCartItems,
                onItemClick = navigateToItemUpdate,

                )
        },
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(
                onClick = {
                    if (selectedItems.value.isNotEmpty()) {
                        selectedItems.value = emptySet()
                    }
                    if (selectedCartItems.value.isNotEmpty()) {
                        selectedCartItems.value = emptySet()
                    }
                    deleteConfirmationForAllRequired = false
                    deleteConfirmationRequired = false
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeBody(
                itemList = itemsNotInCart,
                onItemClick = navigateToItemUpdate,
                selectedItems = selectedItems,
                modifier = modifier.fillMaxSize(),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = dimensionResource(R.dimen.padding_large))
            ) {
                // FAB segment
                if (selectedItems.value.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FloatingActionButton(
                            onClick = {
                                deleteConfirmationForAllRequired = true
                            },
                            shape = MaterialTheme.shapes.medium,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderDelete,
                                contentDescription = "Clear list"
                            )
                        }
                        if (deleteConfirmationForAllRequired) {
                            DeleteConfirmationDialog(
                                onDeleteConfirm = {
                                    deleteConfirmationForAllRequired = false
                                    viewModel.deleteAllItems()
                                },
                                onDeleteCancel = { deleteConfirmationForAllRequired = false },
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                            )
                        }
                        ExtendedFloatingActionButton(
                            onClick = navigateToItemEntry,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.item_entry_title)
                            )
                            Text(text = "Add Item")
                        }
                    }
                } else {
                    // Add your action buttons here
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FloatingActionButton(
                            onClick = {
                                deleteConfirmationRequired = true
                            },
                            shape = MaterialTheme.shapes.medium,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                        if (deleteConfirmationRequired) {
                            DeleteConfirmationDialog(
                                onDeleteConfirm = {
                                    deleteConfirmationRequired = false
                                    viewModel.deleteSelectedItems(selectedItems.value)
                                    selectedItems.value = emptySet()
                                },
                                onDeleteCancel = { deleteConfirmationRequired = false },
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                            )
                        }

                        ExtendedFloatingActionButton(
                            onClick = {
                                selectedItems.value.forEach { itemId ->
                                    viewModel.addToCart(itemId)
                                }
                                selectedItems.value = emptySet()
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = "More"
                            )
                            Text(text = stringResource(R.string.add_to_cart))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomSheetContent(
    itemsInCart: List<Item>,
    selectedItems: MutableState<Set<Int>>,
    totalItemsInCartPrice : Double,
    totalItemsPrice: Double,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    var showCheckoutDialog by rememberSaveable { mutableStateOf(false) }

    // Function to toggle the selection state of an item
    fun toggleSelection(itemId: Int) {
        selectedItems.value = if (selectedItems.value.contains(itemId)) {
            selectedItems.value - itemId
        } else {
            selectedItems.value + itemId
        }
        // Log the selected items
        Log.d("BottomSheet", "Selected items: ${selectedItems.value}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ){
            Row {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Shopping Cart",
                    modifier = Modifier.size(48.dp) // Adjust size as needed

                )

                Spacer(Modifier.weight(1f))

                Column {
                    Text(
                        text = "Cart: $totalItemsInCartPrice",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Balance : ${totalItemsPrice - totalItemsInCartPrice} ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        LazyColumn {
            items(itemsInCart) { item ->
                val isSelected = selectedItems.value.contains(item.id)
                InventoryItem(
                    item = item,
                    isSelected = isSelected,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_small))
                        .combinedClickable(
                            onClick = {
                                if (selectedItems.value.isNotEmpty()) {
                                    toggleSelection(item.id)
                                } else {
                                    onItemClick(item.id)
                                }
                            },
                            onLongClick = {
                                toggleSelection(item.id)
                            }
                        )
                )
            }
        }

        if (selectedItems.value.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    // Perform some action, such as removing selected items from cart
                    selectedItems.value.forEach { itemId ->
                        // Remove item from cart
                        viewModel.removeFromCart(itemId)
                    }
                    selectedItems.value = emptySet()
                },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove selected items from cart"
                )
                Text(text = "Remove Selected")
            }
        } else {
            ExtendedFloatingActionButton(
                onClick = {
                    showCheckoutDialog = true
                },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Checkout"
                )
                Text(text = "Checkout")
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Checkout") },
            text = { Text("Total Price: $$totalItemsInCartPrice\nDo you want to proceed with checkout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCheckoutDialog = false
                        // Perform checkout action, such as clearing the cart
                        itemsInCart.forEach { item ->
                            viewModel.deleteCart()
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HomeBody(
    itemList: List<Item>,
    onItemClick: (Int) -> Unit,
    selectedItems: MutableState<Set<Int>>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Surface(
        modifier = modifier,
    ) {
        if (itemList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_item_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            InventoryList(
                itemList = itemList,
                onItemClick = { onItemClick(it.id) },
                selectedItems = selectedItems,
                contentPadding = contentPadding,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventoryList(
    itemList: List<Item>,
    onItemClick: (Item) -> Unit,
    selectedItems: MutableState<Set<Int>>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Group items by category and calculate subtotals
    val itemsByCategory = itemList.groupBy { it.category }
    val categorySubtotals = itemsByCategory.mapValues { entry ->
        entry.value.sumOf { it.totalPrice }
    }
    val context = LocalContext.current

    // Function to toggle the selection state of an item
    fun toggleSelection(itemId: Int) {
        selectedItems.value = if (selectedItems.value.contains(itemId)) {
            selectedItems.value - itemId
        } else {
            selectedItems.value + itemId
        }
        // Log the selected items
        Log.d("InventoryList", "Selected items: ${selectedItems.value}")
    }

    Column(modifier = modifier) {
        // Display total price at the top
        Spacer(modifier = Modifier.padding(16.dp))

        // Display items by category
        LazyRow(contentPadding = contentPadding) {
            items(itemsByCategory.keys.toList()) { category ->
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "${categorySubtotals[category]} $",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    LazyColumn {
                        items(itemsByCategory[category] ?: emptyList(), key = { it.id }) { item ->
                            val isSelected = selectedItems.value.contains(item.id)

                            InventoryItem(
                                item = item,
                                isSelected = isSelected,
                                modifier = Modifier
                                    .padding(dimensionResource(id = R.dimen.padding_small))
                                    .combinedClickable(
                                        onClick = {
                                            if (selectedItems.value.isNotEmpty()) {
                                                toggleSelection(item.id)
                                            } else {
                                                onItemClick(item)
                                            }
                                        },
                                        onLongClick = {
                                            toggleSelection(item.id)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItem(
    item: Item,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small))
            .width(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
                .background(Color.Transparent), // Set transparent background
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.weight(1f)) // Spacer to push the next Text away

            Text(
                text = stringResource(R.string.quantity, item.quantity),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    val selectedItems = remember { mutableStateOf(setOf<Int>()) }
    InventoryTheme {
        HomeBody(
            itemList = listOf(
                Item(1, "Game", 100.0, 20, "Electronics"),
                Item(2, "Pen", 200.0, 30, "Electronics"),
                Item(3, "TV", 300.0, 50, "Electronics")
            ),
            onItemClick = {},
            selectedItems = selectedItems
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
    val selectedItems = remember { mutableStateOf(setOf<Int>()) }
    InventoryTheme {
        HomeBody(
            itemList = listOf(),
            onItemClick = {},
            selectedItems = selectedItems
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemPreview() {
    InventoryTheme {
        InventoryItem(
            item = Item(1, "Game", 100.0, 20, "Electronics"),
            isSelected = true
        )
    }
}
