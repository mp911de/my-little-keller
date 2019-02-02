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

/**
 * @author Mark Paluch
 */
public class Constants {

	/**
	 * Redis List containing events.
	 */
	public final static String EVENT_LIST = "events";

	/**
	 * Redis Hash for the Stock.
	 */
	public static final String STOCK = "stock";

	/**
	 * Pub/Sub channel for event processing.
	 */
	public static final String WAREHOUSE_CHANNEL = "warehouse";

	/**
	 * Redis Set containing EAN's for stock recalculation requests.
	 */
	public final static String RECALC_QUEUE = "recalc";
}
