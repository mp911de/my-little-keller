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

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * Unit tests for {@link CodecheckClient}.
 *
 * @author Mark Paluch
 */
class CodecheckClientUnitTests {

	@Test
	void shouldDecodeItem() throws Exception {

		ClassPathResource resource = new ClassPathResource("ean/codecheck.html");

		String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

		ItemDescriptor item = CodecheckClient.decode("", html);

		assertThat(item.isFound()).isTrue();
		assertThat(item.getName()).isEqualTo("Milfina fettarme H-Milch ultrahocherhitzt 1,5% Fett");
		assertThat(item.getDetail()).isNull();
		assertThat(item.getCategory()).isEqualTo("Milch");
		assertThat(item.getSubcategory()).isEqualTo("Milch haltbar");
		assertThat(item.getService()).isEqualTo(CodecheckClient.class.getName());
	}
}
