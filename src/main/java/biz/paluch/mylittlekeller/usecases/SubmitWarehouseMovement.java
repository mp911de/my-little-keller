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

import biz.paluch.mylittlekeller.events.InboundEvent;
import biz.paluch.mylittlekeller.events.InventoryCountEvent;
import biz.paluch.mylittlekeller.events.OutboundEvent;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Submit warehouse movement (inbound, outbound, inventory count)
 *
 * @author Mark Paluch
 */
@Service
public class SubmitWarehouseMovement {

	private final RedisTemplate<String, Object> jsonRedisTemplate;
	private final RecalculateStockEvents recalculateStockEvents;

	public SubmitWarehouseMovement(@Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate,
			RecalculateStockEvents recalculateStockEvents) {
		this.jsonRedisTemplate = jsonRedisTemplate;
		this.recalculateStockEvents = recalculateStockEvents;
	}

	/**
	 * Inbound event.
	 *
	 * @param ean
	 * @param count
	 */
	public void inbound(String ean, int count) {

		InboundEvent event = new InboundEvent(ean, count, LocalDateTime.now());
		jsonRedisTemplate.opsForList().rightPush(Constants.EVENT_LIST, event);

		recalculateStockEvents.publish(event);
	}

	/**
	 * Outbound event.
	 *
	 * @param ean
	 * @param count
	 */
	public void outbound(String ean, int count) {

		OutboundEvent event = new OutboundEvent(ean, count, LocalDateTime.now());
		jsonRedisTemplate.opsForList().rightPush(Constants.EVENT_LIST, event);

		recalculateStockEvents.publish(event);
	}

	/**
	 * Inventory correction event.
	 *
	 * @param ean
	 * @param count
	 */
	public void inventory(String ean, int count) {

		InventoryCountEvent event = new InventoryCountEvent(ean, count, LocalDateTime.now());
		jsonRedisTemplate.opsForList().rightPush(Constants.EVENT_LIST, event);

		recalculateStockEvents.publish(event);
	}
}
