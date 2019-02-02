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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Persist a {@link StockItem} in a Redis Hash at {@link Constants#STOCK}.
 *
 * @author Mark Paluch
 */
@Service
class PersistStockItem {

	private final HashOperations<String, String, Object> hashOperations;

	PersistStockItem(@Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> jsonRedisTemplate) {
		this.hashOperations = jsonRedisTemplate.opsForHash();
	}

	void persist(StockItem stockItem) {
		hashOperations.put(Constants.STOCK, stockItem.getDescriptor().getEan(), stockItem);
	}
}
