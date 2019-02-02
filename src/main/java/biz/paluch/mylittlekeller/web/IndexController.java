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

import biz.paluch.mylittlekeller.descriptor.ItemDescriptor;
import biz.paluch.mylittlekeller.domain.StockItem;
import biz.paluch.mylittlekeller.usecases.ListStockItems;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Index page controller.
 *
 * @author Mark Paluch
 */
@Controller
@RequiredArgsConstructor
public class IndexController {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
			.withLocale(Locale.GERMANY);

	private final ListStockItems listStockItems;

	@GetMapping("/")
	ModelAndView index(@Nullable @RequestParam(required = false, defaultValue = "name") OrderBy sort,
			@Nullable @RequestParam(required = false, defaultValue = "asc") Direction direction) {

		List<Row> rows = listStockItems.list().stream() //
				.sorted(getComparator(sort, direction)) //
				.map(this::mapRow).collect(Collectors.toList());

		return new ModelAndView("index").addObject("rows", rows).addObject("sort", sort).addObject("direction", direction);
	}

	@SuppressWarnings("unchecked")
	private Comparator<StockItem> getComparator(@Nullable OrderBy orderBy, @Nullable Direction direction) {

		Comparator<StockItem> comparator = Comparator.comparing(it -> {
			return (Comparable) extractComparable(orderBy, it);
		});

		if (direction == Direction.desc) {
			return comparator.reversed();
		}

		return comparator;
	}

	private Comparable<?> extractComparable(@Nullable OrderBy orderBy, StockItem it) {

		if (orderBy == null || orderBy == OrderBy.name) {
			ItemDescriptor descriptor = it.getDescriptor();

			if (StringUtils.hasText(descriptor.getName())) {
				return descriptor.getName().toLowerCase();
			}
			if (StringUtils.hasText(descriptor.getDetail())) {
				return descriptor.getDetail().toLowerCase();
			}
		}

		if (orderBy == OrderBy.count) {
			return it.getStockCount();
		}

		return "";
	}

	@GetMapping("/entry")
	String entry() {
		return "entry";
	}

	private Row mapRow(StockItem stockItem) {

		Row row = new Row();
		ItemDescriptor descriptor = stockItem.getDescriptor();

		row.setEan(descriptor.getEan());
		row.setStockCount(stockItem.getStockCount());

		List<String> name = new ArrayList<>();

		if (StringUtils.hasText(descriptor.getName())) {
			name.add(descriptor.getName());
		}

		if (StringUtils.hasText(descriptor.getDetail())) {
			name.add(org.apache.commons.lang3.StringUtils.abbreviate(descriptor.getDetail(), 40));
		}

		if (StringUtils.hasText(descriptor.getVendor())) {
			name.add("(" + org.apache.commons.lang3.StringUtils.abbreviate(descriptor.getVendor(), 20) + ")");
		}

		row.setName(StringUtils.collectionToDelimitedString(name, "<br/>"));

		if (StringUtils.isEmpty(descriptor.getSubcategory())) {
			row.setCategory(descriptor.getCategory());
		} else {
			row.setCategory(descriptor.getCategory() + "/" + descriptor.getSubcategory());
		}

		if (stockItem.getLastInbound() != null) {
			row.setLastIn(formatter.format(stockItem.getLastInbound()));
		} else {
			row.setLastIn("");
		}

		if (stockItem.getLastOutbound() != null) {
			row.setLastOut(formatter.format(stockItem.getLastOutbound()));
		} else {
			row.setLastOut("");
		}

		return row;
	}

	@Data
	static class Row {
		String ean;
		int stockCount;
		String name;
		String category;
		String lastIn;
		String lastOut;

		public boolean hasDates() {
			return lastIn != null || lastOut != null;
		}
	}

	enum OrderBy {
		name, count
	}

	enum Direction {
		asc, desc
	}
}
