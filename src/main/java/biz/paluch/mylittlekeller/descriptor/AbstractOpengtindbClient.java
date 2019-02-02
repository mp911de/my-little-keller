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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.data.util.Pair;

/**
 * @author Mark Paluch
 */
abstract class AbstractOpengtindbClient implements ItemDescriptorClient {

	static ItemDescriptor decode(String ean, byte[] raw) {

		String response = new String(raw, StandardCharsets.ISO_8859_1);

		ItemDescriptor descriptor = new ItemDescriptor();
		descriptor.setService(OpengtindbClient.class.getName());

		descriptor.setEan(ean);
		AtomicBoolean error = new AtomicBoolean();
		Arrays.stream(response.split("\n")) //
				.filter(it -> it.contains("=")) //
				.map(AbstractOpengtindbClient::splitToPair).forEach(it -> {

					if (it.getFirst().equals("error")) {

						if (it.getSecond().equals("0")) {
							descriptor.setFound(true);
							return;
						}

						if (it.getSecond().equals("1")) {
							descriptor.setFound(false);
							return;
						}

						error.set(true);
					}

					apply(it, "name", descriptor::setName);
					apply(it, "detailname", descriptor::setDetail);
					apply(it, "vendor", descriptor::setVendor);
					apply(it, "maincat", descriptor::setCategory);
					apply(it, "subcat", descriptor::setSubcategory);

				});

		return error.get() ? null : descriptor;
	}

	private static void apply(Pair<String, String> pair, String name, Consumer<String> setter) {
		if (pair.getFirst().equals(name)) {
			setter.accept(pair.getSecond());
		}
	}

	private static Pair<String, String> splitToPair(String item) {

		String[] split = item.split("=", 2);
		return Pair.of(split[0], split[1]);
	}
}
