/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.mylittlekeller.usecases;

import biz.paluch.mylittlekeller.domain.StockItem;
import biz.paluch.mylittlekeller.descriptor.ItemDescriptor;
import biz.paluch.mylittlekeller.descriptor.ItemDescriptorClient;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Create a {@link StockItem} given {@code ean}, {@code stockCount} and dates.
 *
 * @author Mark Paluch
 */
@Service
@RequiredArgsConstructor
class CreateStockItem {

	private final ItemDescriptorClient client;

	StockItem create(String ean, int stockCount, LocalDateTime lastInbound, LocalDateTime lastOutbound) {

		Optional<ItemDescriptor> articleDescriptor = client.find(ean);

		ItemDescriptor descriptor = articleDescriptor.orElseGet(() -> {

			ItemDescriptor fallback = new ItemDescriptor();
			fallback.setEan(ean);
			fallback.setName("Unbekannt");
			fallback.setFound(false);

			return fallback;
		});

		StockItem stockItem = new StockItem();
		stockItem.setDescriptor(descriptor);
		stockItem.setLastInbound(lastInbound);
		stockItem.setLastOutbound(lastOutbound);
		stockItem.setStockCount(stockCount);

		return stockItem;
	}
}
