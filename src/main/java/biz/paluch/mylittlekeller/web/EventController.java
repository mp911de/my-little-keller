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
package biz.paluch.mylittlekeller.web;

import biz.paluch.mylittlekeller.usecases.SubmitWarehouseMovement;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Eventing controller
 *
 * @author Mark Paluch
 */
@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
class EventController {

	private final SubmitWarehouseMovement movement;

	/**
	 * Receive inbound (incoming stock) event.
	 *
	 * @param descriptor
	 */
	@PostMapping("inbound")
	public void inbound(@RequestBody Descriptor descriptor) {
		movement.inbound(descriptor.getEan(), descriptor.count);
	}

	/**
	 * Receive outbound (outgoing stock) event.
	 *
	 * @param descriptor
	 */
	@PostMapping("outbound")
	public void outbound(@RequestBody Descriptor descriptor) {
		movement.outbound(descriptor.getEan(), descriptor.getCount());
	}

	/**
	 * Receive inventory count (correction) event.
	 *
	 * @param descriptor
	 */
	@PostMapping("inventory")
	public void inventory(@RequestBody Descriptor descriptor) {
		movement.inventory(descriptor.getEan(), descriptor.getCount());
	}

	@Data
	static class Descriptor {

		String ean;
		int count = 1;
	}
}
