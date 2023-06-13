package com.wadhams.financials.db.type

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.type.obsolete.MonthDateRange2

import spock.lang.Title

@Title("Unit tests for MonthDateRange")
class MonthDateRangeSpec extends spock.lang.Specification {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern('yyyy-MM-dd')
	
	def "test getters"() {
		given:
			MonthDateRange2 mdr = MonthDateRange2.Sept2019
		
		expect:
			mdr.firstDate == '2019-09-01'
			mdr.lastDate == '2019-09-30'
	}
	
	def "test findByFirstDate"() {
		given:
			MonthDateRange2 mdr = MonthDateRange2.findByFirstDate('2020-09-01')
		
		expect:
			mdr == MonthDateRange2.Sept2020
	}
	
	def "test now"() {
		given:
			MonthDateRange2 resultMonthDateRange = MonthDateRange2.now()
		
		expect:
			LocalDate now = LocalDate.now()
			String firstDate = now.format(dtf).substring(0, 7) + '-01'
			MonthDateRange2 expectedMonthDateRange = MonthDateRange2.findByFirstDate(firstDate)
			
			resultMonthDateRange == expectedMonthDateRange
	}
	
	def "test compare"() {
		given:
			List<MonthDateRange2> list = []
			list << MonthDateRange2.Oct2019
			list << MonthDateRange2.Sept2019
			list << MonthDateRange2.Nov2019
			Collections.sort(list)
			
		expect:
			list[0] == MonthDateRange2.Sept2019
			list[1] == MonthDateRange2.Oct2019
			list[2] == MonthDateRange2.Nov2019
	}
	
	def "test previousTwelve with 16 possible results"() {
		given:
			List<MonthDateRange2> result = MonthDateRange2.previousTwelve(MonthDateRange2.Oct2020)
		
		expect:
			result.size() == 12
			result[0] == MonthDateRange2.Oct2019		//first
			result[-1] == MonthDateRange2.Sept2020	//last
	}
	
	def "test previousTwelve with 12 exact results"() {
		given:
			List<MonthDateRange2> result = MonthDateRange2.previousTwelve(MonthDateRange2.Jun2020)
		
		expect:
			result.size() == 12
			result[0] == MonthDateRange2.Jun2019		//first
			result[-1] == MonthDateRange2.May2020	//last
	}
	
	def "test previousTwelve with 11 exact results"() {
		given:
			List<MonthDateRange2> result = MonthDateRange2.previousTwelve(MonthDateRange2.May2020)
		
		expect:
			result.size() == 11
			result[0] == MonthDateRange2.Jun2019		//first
			result[-1] == MonthDateRange2.Apr2020	//last
	}
}
