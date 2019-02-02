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
package biz.paluch.mylittlekeller.descriptor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis-caching {@link ItemDescriptorClient}. Stores {@link ItemDescriptor} in a Redis Hash at key {@code ean:<ean>}.
 *
 * @author Mark Paluch
 */
public class RedisCachingItemDescriptorClient implements ItemDescriptorClient {

	private final RedisTemplate<String, String> redisTemplate;
	private final List<ItemDescriptorClient> delegates;

	public RedisCachingItemDescriptorClient(RedisTemplate<String, String> redisTemplate,
			ItemDescriptorClient... delegates) {
		this.redisTemplate = redisTemplate;
		this.delegates = Arrays.asList(delegates);
	}

	@Override
	public Optional<ItemDescriptor> find(String ean) {

		String cacheKey = String.format("ean:%s", ean);
		HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
		Map<String, String> hash = hashOps.entries(cacheKey);

		if (hash.isEmpty()) {

			Optional<ItemDescriptor> articleDescriptor = doFind(ean);

			hash = new LinkedHashMap<>();

			Map<String, String> hashToUse = hash;

			hash.put("found", "" + articleDescriptor.filter(ItemDescriptor::isFound).isPresent());
			articleDescriptor.ifPresent(descriptor -> {

				hashToUse.put("ean", descriptor.getEan());
				hashToUse.put("name", descriptor.getName());
				hashToUse.put("detail", descriptor.getDetail());
				hashToUse.put("vendor", descriptor.getVendor());
				hashToUse.put("category", descriptor.getCategory());
				hashToUse.put("subcategory", descriptor.getSubcategory());
				hashToUse.put("service", descriptor.getService());
				hashToUse.put("raw", descriptor.getRaw());
			});

			hashOps.putAll(cacheKey, hash);

			return articleDescriptor;
		}

		ItemDescriptor descriptor = new ItemDescriptor();

		descriptor.setEan(hash.get("ean"));
		descriptor.setName(hash.get("name"));
		descriptor.setDetail(hash.get("detail"));
		descriptor.setVendor(hash.get("vendor"));
		descriptor.setCategory(hash.get("category"));
		descriptor.setSubcategory(hash.get("subcategory"));
		descriptor.setService(hash.get("service"));
		descriptor.setRaw(hash.get("raw"));
		descriptor.setFound(Boolean.valueOf(hash.get("found")));

		return Optional.of(descriptor);
	}

	private Optional<ItemDescriptor> doFind(String ean) {

		Optional<ItemDescriptor> last = Optional.empty();
		for (ItemDescriptorClient delegate : delegates) {

			last = delegate.find(ean);
			if (last.filter(ItemDescriptor::isFound).isPresent()) {
				return last;
			}
		}

		return last;
	}
}
