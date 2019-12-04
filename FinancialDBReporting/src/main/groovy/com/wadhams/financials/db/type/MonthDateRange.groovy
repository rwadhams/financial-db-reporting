package com.wadhams.financials.db.type

import java.time.YearMonth

enum MonthDateRange {
	Jun2019(2019, 6),
	Jul2019(2019, 7),
	Aug2019(2019, 8),
	Sept2019(2019, 9),
	Oct2019(2019, 10),
	Nov2019(2019, 11),
	Dec2019(2019, 12),
	Jan2020(2020, 1),
	Feb2020(2020, 2),
	Mar2020(2020, 3),
	Apr2020(2020, 4),
	May2020(2020, 5);
	
	private final YearMonth yearMonth
	
	MonthDateRange(int year, int month) {
		yearMonth = YearMonth.of(year, month)
	}
	
	String getFirstDate() {
		return yearMonth.atDay(1)
	}
	
	String getLastDate() {
		return yearMonth.atEndOfMonth()
	}
}
