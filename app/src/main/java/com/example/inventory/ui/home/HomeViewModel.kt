package com.example.inventory.ui.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
    val itemsInCart: StateFlow<List<Item>> = itemsRepository.getItemsInCart()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    val itemsNotInCart: StateFlow<List<Item>> = itemsRepository.getItemsNotInCart()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun addToCart(itemId: Int) {
        viewModelScope.launch {
            val item = itemsRepository.getItemStream(itemId).firstOrNull()
            item?.let {
                val updatedItem = it.copy(cart = true)
                itemsRepository.updateItem(updatedItem)
            }
        }
    }

    fun removeFromCart(itemId: Int) {
        viewModelScope.launch {
            val item = itemsRepository.getItemStream(itemId).firstOrNull()
            item?.let {
                val updatedItem = it.copy(cart = false)
                itemsRepository.updateItem(updatedItem)
            }
        }
    }


    fun deleteAllItems() {
        viewModelScope.launch {
            itemsRepository.deleteAllItems()
        }
    }

    fun deleteSelectedItems(selectedItemIds: Set<Int>) {
        viewModelScope.launch {
            val selectedItems = homeUiState.value.itemList.filter { it.id in selectedItemIds }
            itemsRepository.deleteItems(selectedItems)
        }
    }

    fun deleteCart() {
        viewModelScope.launch {
            val cartItems = itemsInCart.value
            itemsRepository.deleteItems(cartItems)
        }
    }

    val homeUiState: StateFlow<HomeUiState> =
        itemsRepository.getAllItemsStream().map { items ->
            Log.d("HomeViewModel", "Retrieved items: $items")
            val itemsByCategory = items.groupBy { it.category }
            HomeUiState(items, itemsByCategory)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )


    val totalItemsPrice: StateFlow<Double> = itemsRepository.getTotalPrice()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = 0.0
        ).also { priceFlow ->
            viewModelScope.launch {
                priceFlow.collect { price ->
                    Log.d("HomeViewModel", "Total price updated: $price")
                }
            }
        }

    val totalItemsInCartPrice: StateFlow<Double> = itemsInCart.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = 0.0
    )


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(
    val itemList: List<Item> = listOf(),
    val itemsByCategory: Map<String, List<Item>> = emptyMap()

)
