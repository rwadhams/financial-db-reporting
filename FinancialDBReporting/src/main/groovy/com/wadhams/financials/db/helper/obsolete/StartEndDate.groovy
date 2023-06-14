package com.wadhams.financials.db.helper.obsolete

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class StartEndDate {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern('dd/MM/yyyy')
	DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern('dd')
	DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern('MM')
	
	LocalDate startDate
	LocalDate endDate

	def StartEndDate(LocalDate start, LocalDate end) {
		startDate = start
		endDate = end
	}
	
	String getDB2StartDate() {
		return "${startDate.getYear()}${startDate.format(dtfMonth)}${startDate.format(dtfDay)}"
	}
	
	String getDB2EndDate() {
		return "${endDate.getYear()}${endDate.format(dtfMonth)}${endDate.format(dtfDay)}"
	}
	
	int getDays() {
		return ChronoUnit.DAYS.between(startDate, endDate) + 1
	}
	
}
