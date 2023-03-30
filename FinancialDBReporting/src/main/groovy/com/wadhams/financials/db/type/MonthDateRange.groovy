package com.wadhams.financials.db.type

import java.time.YearMonth

enum MonthDateRange implements Comparator<MonthDateRange> {
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
	May2020(2020, 5),
	Jun2020(2020, 6),
	Jul2020(2020, 7),
	Aug2020(2020, 8),
	Sept2020(2020, 9),
	Oct2020(2020, 10),
	Nov2020(2020, 11),
	Dec2020(2020, 12),
	Jan2021(2021, 1),
	Feb2021(2021, 2),
	Mar2021(2021, 3),
	Apr2021(2021, 4),
	May2021(2021, 5),
	Jun2021(2021, 6),
	Jul2021(2021, 7),
	Aug2021(2021, 8),
	Sept2021(2021, 9),
	Oct2021(2021, 10),
	Nov2021(2021, 11),
	Dec2021(2021, 12),
	Jan2022(2022, 1),
	Feb2022(2022, 2),
	Mar2022(2022, 3),
	Apr2022(2022, 4),
	May2022(2022, 5),
	Jun2022(2022, 6),
	Jul2022(2022, 7),
	Aug2022(2022, 8),
	Sept2022(2022, 9),
	Oct2022(2022, 10),
	Nov2022(2022, 11),
	Dec2022(2022, 12),
	Jan2023(2023, 1),
	Feb2023(2023, 2),
	Mar2023(2023, 3),
	Apr2023(2023, 4),
	May2023(2023, 5),
	Jun2023(2023, 6),
	Jul2023(2023, 7),
	Aug2023(2023, 8),
	Sept2023(2023, 9),
	Oct2023(2023, 10),
	Nov2023(2023, 11),
	Dec2023(2023, 12);

	private static EnumSet<MonthDateRange> allEnums = EnumSet.allOf(MonthDateRange.class)

	private final YearMonth yearMonth
	
	MonthDateRange(int year, int month) {
		yearMonth = YearMonth.of(year, month)
	}
	
	static List<MonthDateRange> previousTwelve(MonthDateRange mdr) {
		List<MonthDateRange> list = []
		
		for (MonthDateRange e : allEnums) {
			if (e.yearMonth.isBefore(mdr.yearMonth)) {
				list << e
			}
		}
		
		Collections.sort(list)
		
		if (list.size() > 12) {
			//create a subList using the last 12 items in the list
			return list.subList(list.size()-12, list.size())
		}
		
		return list
	}
	
	static MonthDateRange now() {
		for (MonthDateRange e : allEnums) {
			if (e.yearMonth.equals(YearMonth.now())) {
				return e
			}
		}
		
		return null
	}
	
	static MonthDateRange findByFirstDate(String firstDate) {
		for (MonthDateRange e : allEnums) {
			if (e.getFirstDate() == firstDate) {
				return e
			}
		}
		
		return null
	}
	
	String getFirstDate() {
		return yearMonth.atDay(1)
	}
	
	String getLastDate() {
		return yearMonth.atEndOfMonth()
	}

	@Override
	public int compare(MonthDateRange mdr1, MonthDateRange mdr2) {
		return mdr1.compareTo(mdr2)
	}

}
