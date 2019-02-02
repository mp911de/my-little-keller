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
package biz.paluch.mylittlekeller.events;

import biz.paluch.mylittlekeller.usecases.Constants;
import biz.paluch.mylittlekeller.usecases.RecalculateSingleItemStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Process events that were not picked up by the Pub/Sub mechanism.
 *
 * @author Mark Paluch
 */
@Service
@RequiredArgsConstructor
@Slf4j
class FallbackScheduler {

	private final RedisTemplate<String, String> redisTemplate;

	private final RecalculateSingleItemStock recalculateSingleItemStock;

	/**
	 * Every hour, every 10 minutes.
	 */
	@Scheduled(cron = "0 0/10 * * * *")
	void processDroppedEvents() {

		Set<String> eans = redisTemplate.opsForSet().members(Constants.RECALC_QUEUE);

		if (eans.isEmpty()) {
			return;
		}

		log.info("Starting stock calculation for {} items", eans.size());

		eans.forEach(recalculateSingleItemStock::recalculate);
	}
}
