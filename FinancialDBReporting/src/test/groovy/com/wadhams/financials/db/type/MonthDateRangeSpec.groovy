package com.wadhams.financials.db.type

import com.wadhams.financials.db.type.MonthDateRange
import spock.lang.Title

@Title("Unit tests for AuditStep")
class MonthDateRangeSpec extends spock.lang.Specification {

	def "test getters"() {
		given:
			MonthDateRange mdr = MonthDateRange.Sept2019
		
		expect:
			mdr.firstDate == '2019-09-01'
			mdr.lastDate == '2019-09-30'
	}
}
