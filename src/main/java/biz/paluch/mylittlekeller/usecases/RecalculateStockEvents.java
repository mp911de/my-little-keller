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

import biz.paluch.mylittlekeller.events.EanEvent;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Eventing to submit movements for recalculation.
 *
 * @author Mark Paluch
 */
@Service
class RecalculateStockEvents {

	private final RedisTemplate<String, String> redisTemplate;
	private final RedisTemplate<String, Object> jsonRedisTemplate;

	public RecalculateStockEvents(RedisTemplate<String, String> redisTemplate,
			RedisTemplate<String, Object> jsonRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.jsonRedisTemplate = jsonRedisTemplate;
	}

	void publish(EanEvent event) {

		redisTemplate.opsForSet().add(Constants.RECALC_QUEUE, event.getEan());
		jsonRedisTemplate.convertAndSend(Constants.WAREHOUSE_CHANNEL, event);
	}
}
