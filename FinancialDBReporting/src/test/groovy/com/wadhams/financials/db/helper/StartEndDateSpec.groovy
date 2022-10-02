package com.wadhams.financials.db.helper

import java.time.LocalDate
import java.time.Month
import spock.lang.Title

@Title("Unit tests for StartEndDate")
class StartEndDateSpec extends spock.lang.Specification {
	
	def "test DB2StartDate and DB2EndDate using LocalDate constructor"() {
		given:
			LocalDate start = LocalDate.of(2021, Month.JUNE, 14)
			LocalDate end   = LocalDate.of(2021, Month.OCTOBER, 31)
			StartEndDate sed = new StartEndDate(start, end)
		
		expect:
			sed.DB2StartDate == '20210614'
			sed.DB2EndDate == '20211031'
	}
	
	def "test DB2StartDate and DB2EndDate using single digit day and month"() {
		given:
			LocalDate start = LocalDate.of(2020, Month.JUNE, 4)
			LocalDate end   = LocalDate.of(2021, Month.AUGUST, 31)
			StartEndDate sed = new StartEndDate(start, end)
		
		expect:
			sed.DB2StartDate == '20200604'
			sed.DB2EndDate == '20210831'
	}
	
	def "test getDays method, same month"() {
		given:
			LocalDate start = LocalDate.of(2021, Month.OCTOBER, 30)
			LocalDate end   = LocalDate.of(2021, Month.OCTOBER, 31)
			StartEndDate sed = new StartEndDate(start, end)
		
		expect:
			sed.getDays() == 2
	}
	
	def "test getDays method, across months"() {
		given:
			LocalDate start = LocalDate.of(2021, Month.OCTOBER, 30)
			LocalDate end   = LocalDate.of(2021, Month.NOVEMBER, 10)
			StartEndDate sed = new StartEndDate(start, end)
		
		expect:
			sed.getDays() == 12
	}
	
}
