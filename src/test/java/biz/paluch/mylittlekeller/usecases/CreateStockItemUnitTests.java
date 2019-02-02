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

import static org.assertj.core.api.Assertions.*;

import biz.paluch.mylittlekeller.domain.StockItem;
import biz.paluch.mylittlekeller.descriptor.ItemDescriptorClient;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreateStockItem}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class CreateStockItemUnitTests {

	@Mock ItemDescriptorClient client;

	@Test
	void shouldCreateStockItem() {

		CreateStockItem create = new CreateStockItem(client);

		LocalDateTime inbound = LocalDateTime.now();
		LocalDateTime outbound = LocalDateTime.now();

		StockItem stockItem = create.create("1234", 44, inbound, outbound);

		assertThat(stockItem.getDescriptor().isFound()).isFalse();
		assertThat(stockItem.getStockCount()).isEqualTo(44);
		assertThat(stockItem.getLastInbound()).isEqualTo(inbound);
		assertThat(stockItem.getLastOutbound()).isEqualTo(outbound);
	}
}
