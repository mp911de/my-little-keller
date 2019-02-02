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

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Client for {@code codecheck.info}.
 *
 * @author Mark Paluch
 */
public class CodecheckClient implements ItemDescriptorClient {

	private final RestTemplate restTemplate;

	public CodecheckClient(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
		this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
					ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

				HttpHeaders headers = httpRequest.getHeaders();

				headers.put("User-Agent", Collections.singletonList(
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"));
				headers.put("Accept", Collections
						.singletonList("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
				headers.put("Referer", Collections.singletonList("https://www.codecheck.info"));

				return clientHttpRequestExecution.execute(httpRequest, bytes);
			}
		});
	}

	@Override
	public Optional<ItemDescriptor> find(String ean) {
		//
		ResponseEntity<String> entity = restTemplate
				.getForEntity("https://www.codecheck.info/product.search?q={ean}&OK=Suchen", String.class, ean);

		if (entity.getBody() == null || entity.getBody().contains("Es wurde kein Produkt")) {

			ItemDescriptor itemDescriptor = createDescriptor();

			return Optional.of(itemDescriptor);
		}

		return Optional.of(decode(entity.getBody()));
	}

	static ItemDescriptor decode(String html) {

		ItemDescriptor descriptor = createDescriptor();
		descriptor.setFound(true);

		Document document = Jsoup.parse(html, "http://void");

		Element title = document.getElementsByTag("h1").first();
		descriptor.setName(title.text());

		Element subtitle = document.getElementsByTag("h3").first();
		descriptor.setCategory(subtitle.text());

		Elements elementsByClass = document.getElementsByClass("product-info-item");

		for (Element infoItem : elementsByClass) {

			if (infoItem.getElementsContainingText("Kategorie").isEmpty()) {
				continue;
			}

			Elements category = infoItem.getElementsByTag("p");

			if (category.isEmpty()) {
				continue;
			}

			if (!StringUtils.hasText(descriptor.getCategory())) {
				descriptor.setCategory(category.next().text());
			} else {
				descriptor.setSubcategory(category.next().text());
			}
		}

		return descriptor;
	}

	private static ItemDescriptor createDescriptor() {

		ItemDescriptor itemDescriptor = new ItemDescriptor();
		itemDescriptor.setService(CodecheckClient.class.getName());

		return itemDescriptor;
	}
}
