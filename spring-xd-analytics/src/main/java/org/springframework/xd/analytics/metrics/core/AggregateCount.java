/*
 * Copyright 2011-2013 the original author or authors.
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

package org.springframework.xd.analytics.metrics.core;

import org.joda.time.DateTimeField;
import org.joda.time.Interval;

/**
 * @author Luke Taylor
 */
public class AggregateCount {

	private final String name;

	private final Interval interval;

	private final long[] counts;

	private final DateTimeField resolution;

	public String getName() {
		return name;
	}


	public Interval getInterval() {
		return interval;
	}


	public long[] getCounts() {
		return counts;
	}


	public DateTimeField getResolution() {
		return resolution;
	}

	public AggregateCount(String name, Interval interval, long[] counts, DateTimeField resolution) {
		this.name = name;
		this.interval = interval;
		this.counts = counts.clone();
		this.resolution = resolution;
	}

	/**
	 * @return the total number of counts in the interval.
	 */
	public int getTotal() {
		int total = 0;
		for (int i = 0; i < counts.length; i++) {
			total += counts[i];
		}
		return total;
	}
}
