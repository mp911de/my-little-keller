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

import lombok.SneakyThrows;

import java.util.Optional;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Client for {@code https://opengtindb.org}.
 *
 * @author Mark Paluch
 */
public class OpengtindbClient extends AbstractOpengtindbClient implements ItemDescriptorClient {

	private final RestTemplate restTemplate;

	public OpengtindbClient(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	@SneakyThrows
	public Optional<ItemDescriptor> find(String ean) {

		ResponseEntity<byte[]> entity = restTemplate
				.getForEntity("http://opengtindb.org/?ean={ean}&cmd=query&queryid=400000000", byte[].class, ean);

		if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
			return Optional.empty();
		}

		return Optional.ofNullable(decode(ean, entity.getBody()));
	}
}
