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

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import org.springframework.util.StreamUtils;

/**
 * @author Mark Paluch
 */
public class FilebasedOpengtindbClient extends AbstractOpengtindbClient implements ItemDescriptorClient {

	@SneakyThrows
	public Optional<ItemDescriptor> find(String ean) {

		File file = new File(ean + ".bin");

		if (!file.exists()) {
			return Optional.empty();
		}

		try (FileInputStream is = new FileInputStream(file)) {
			return Optional.ofNullable(decode(ean, StreamUtils.copyToByteArray(is)));
		}
	}
}
