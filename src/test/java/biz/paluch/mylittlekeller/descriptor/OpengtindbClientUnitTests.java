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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * Unit tests for {@link OpengtindbClient}.
 *
 * @author Mark Paluch
 */
class OpengtindbClientUnitTests {

	@Test
	void shouldDecodeEan() throws IOException {

		byte[] bytes = StreamUtils.copyToByteArray(new ClassPathResource("ean/4335896362601.bin").getInputStream());

		ItemDescriptor descriptor = OpengtindbClient.decode("4335896362601", bytes);

		assertThat(descriptor.getEan()).isEqualTo("4335896362601");
		assertThat(descriptor.getName()).isEqualTo("Gummibonbon");
		assertThat(descriptor.getDetail()).isEqualTo("Ringelwürmchen");
		assertThat(descriptor.getVendor()).isEqualTo("K-Classic");
		assertThat(descriptor.getCategory()).isEqualTo("Süsswaren, Snacks");
		assertThat(descriptor.getSubcategory()).isEqualTo("Fruchtgummi");
	}
}
