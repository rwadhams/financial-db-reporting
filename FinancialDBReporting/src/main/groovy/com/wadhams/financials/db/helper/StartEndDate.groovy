package com.wadhams.financials.db.helper

class StartEndDate {
	int startDay
	int startMonth
	int startYear
	
	int endDay
	int endMonth
	int endYear

	GregorianCalendar startCal
	GregorianCalendar endCal

	def StartEndDate(Map m) {
		this.startYear = m.startYear
		this.startMonth = m.startMonth
		this.startDay = m.startDay
		
		this.endYear = m.endYear
		this.endMonth = m.endMonth
		this.endDay = m.endDay
		
		startCal = new GregorianCalendar(m.startYear, m.startMonth, m.startDay)
		endCal = new GregorianCalendar(m.endYear, m.endMonth, m.endDay)
	}
	
	String getDB2StartDate() {
		String month = startMonth + 1
		String day = startDay
		return "$startYear${month.padLeft(2, '0')}${day.padLeft(2, '0')}"
	}
	
	String getDB2EndDate() {
		String month = endMonth + 1
		String day = endDay
		return "$endYear${month.padLeft(2, '0')}${day.padLeft(2, '0')}"
	}
	
	int getDays() {
		return endCal - startCal + 1
	}
	
	Date getStartDate() {
		return startCal.getTime()
	}

	Date getEndDate() {
		return endCal.getTime()
	}
}
