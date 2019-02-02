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
import biz.paluch.mylittlekeller.events.EanEvent;
import biz.paluch.mylittlekeller.events.InboundEvent;
import biz.paluch.mylittlekeller.events.InventoryCountEvent;
import biz.paluch.mylittlekeller.events.OutboundEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Recalculate the stock for a single {@link StockItem}.
 *
 * @author Mark Paluch
 */
@Service
@Slf4j
public class RecalculateSingleItemStock {

	private final RedisTemplate<String, String> redisTemplate;
	private final RedisTemplate<String, Object> jsonRedisTemplate;
	private final CreateStockItem createStockItem;
	private final PersistStockItem persistStockItem;

	public RecalculateSingleItemStock(RedisTemplate<String, String> redisTemplate,
			@Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate, CreateStockItem createStockItem,
			PersistStockItem persistStockItem) {
		this.redisTemplate = redisTemplate;
		this.jsonRedisTemplate = jsonRedisTemplate;
		this.createStockItem = createStockItem;
		this.persistStockItem = persistStockItem;
	}

	public void recalculate(String ean) {

		if (redisTemplate.opsForSet().remove(Constants.RECALC_QUEUE, ean).longValue() == 0) {
			log.info("Drop recalculation request for {}", ean);
			return;
		}

		List<Object> events = jsonRedisTemplate.opsForList().range(Constants.EVENT_LIST, 0, -1);

		int stock = 0;
		LocalDateTime inbound = null;
		LocalDateTime outbound = null;

		for (Object event : events) {

			if (!(event instanceof EanEvent) || !((EanEvent) event).getEan().equals(ean)) {
				continue;
			}

			if (event instanceof InboundEvent) {
				stock += ((InboundEvent) event).getCount();
				inbound = ((InboundEvent) event).getTime();
			}

			if (event instanceof OutboundEvent) {
				stock -= ((OutboundEvent) event).getCount();
				outbound = ((OutboundEvent) event).getTime();
			}

			if (event instanceof InventoryCountEvent) {
				stock = ((InventoryCountEvent) event).getCount();
			}
		}

		StockItem stockItem = createStockItem.create(ean, stock, inbound, outbound);
		persistStockItem.persist(stockItem);

		log.info("Recalculated stock for " + stockItem);
	}
}
