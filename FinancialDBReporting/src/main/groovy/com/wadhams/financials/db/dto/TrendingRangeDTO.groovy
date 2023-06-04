package com.wadhams.financials.db.dto

import groovy.transform.ToString
import java.time.LocalDate
import java.time.YearMonth

@ToString(includeNames=true)
class TrendingRangeDTO {
	YearMonth startYM
	YearMonth endYM
	
	LocalDate startRangeDate
	LocalDate endRangeDate
	
	BigDecimal amount
	
	def TrendingRangeDTO(YearMonth startYM, YearMonth endYM) {
		this.startYM = startYM
		this.endYM = endYM
		
		startRangeDate = LocalDate.of(startYM.getYear(), startYM.getMonth(), 1)
		endRangeDate = endYM.atEndOfMonth()
		
		amount = BigDecimal.ZERO
	}
}
