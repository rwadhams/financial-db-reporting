package com.wadhams.financials.db.type

import java.text.SimpleDateFormat

import com.wadhams.financials.db.type.MonthDateRange
import spock.lang.Title

@Title("Unit tests for MonthDateRange")
class MonthDateRangeSpec extends spock.lang.Specification {
	SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM')
	
	def "test getters"() {
		given:
			MonthDateRange mdr = MonthDateRange.Sept2019
		
		expect:
			mdr.firstDate == '2019-09-01'
			mdr.lastDate == '2019-09-30'
	}
	
	def "test findByFirstDate"() {
		given:
			MonthDateRange mdr = MonthDateRange.findByFirstDate('2020-09-01')
		
		expect:
			mdr == MonthDateRange.Sept2020
	}
	
	def "test now"() {
		given:
			MonthDateRange resultMonthDateRange = MonthDateRange.now()
		
		expect:
			GregorianCalendar cal = GregorianCalendar.getInstance()
			String firstDate = sdf.format(cal.getTime()) + '-01'
			MonthDateRange expectedMonthDateRange = MonthDateRange.findByFirstDate(firstDate)
			
			resultMonthDateRange == expectedMonthDateRange
	}
	
	def "test compare"() {
		given:
			List<MonthDateRange> list = []
			list << MonthDateRange.Oct2019
			list << MonthDateRange.Sept2019
			list << MonthDateRange.Nov2019
			Collections.sort(list)
			
		expect:
			list[0] == MonthDateRange.Sept2019
			list[1] == MonthDateRange.Oct2019
			list[2] == MonthDateRange.Nov2019
	}
	
	def "test previousTwelve with 16 results"() {
		given:
			List<MonthDateRange> result = MonthDateRange.previousTwelve(MonthDateRange.Oct2020)
		
		expect:
			result.size() == 12
			result[0] == MonthDateRange.Oct2019		//first
			result[-1] == MonthDateRange.Sept2020	//last
	}
	
	def "test previousTwelve with 12 results"() {
		given:
			List<MonthDateRange> result = MonthDateRange.previousTwelve(MonthDateRange.Jun2020)
		
		expect:
			result.size() == 12
			result[0] == MonthDateRange.Jun2019		//first
			result[-1] == MonthDateRange.May2020	//last
	}
	
	def "test previousTwelve with 11 results"() {
		given:
			List<MonthDateRange> result = MonthDateRange.previousTwelve(MonthDateRange.May2020)
		
		expect:
			result.size() == 11
			result[0] == MonthDateRange.Jun2019		//first
			result[-1] == MonthDateRange.Apr2020	//last
	}
}
