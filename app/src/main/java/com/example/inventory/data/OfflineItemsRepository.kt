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

package com.example.inventory.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = itemDao.getAllItems()

    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)

    override fun getItemsInCart(): Flow<List<Item>> = itemDao.getItemsInCart()

    override fun getItemsNotInCart(): Flow<List<Item>> = itemDao.getItemsNotInCart()

    override fun getDistinctCategories(): Flow<List<String>> {
        return itemDao.getDistinctCategories()
    }

    override suspend fun insertItem(item: Item) = itemDao.insert(item)

    override suspend fun deleteItem(item: Item) = itemDao.delete(item)

    override suspend fun deleteItems(items: List<Item>) {
        itemDao.deleteItems(items)
    }

    override suspend fun deleteAllItems() {
        itemDao.deleteAllItems()
    }

    override suspend fun updateItem(item: Item) = itemDao.update(item)

    override fun getTotalPrice(): Flow<Double> {
        return itemDao.getAllItems().map { itemList ->
            itemList.sumOf { it.grandTotal }
        }
    }

    override suspend fun updateItems(items: List<Item>) {
        itemDao.updateItems(items)
    }

}
