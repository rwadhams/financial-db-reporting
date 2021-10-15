package com.wadhams.financials.db.dto

import groovy.transform.ToString
import java.time.YearMonth

@ToString(includeNames=true)
class SmallMediumLargeDTO {
	YearMonth ym
	
	String startDate
	String endDate
	
	BigDecimal smallAmount
	BigDecimal mediumAmount
	BigDecimal largeAmount
	
	BigDecimal totalAmount
}
