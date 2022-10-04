package com.wadhams.financials.db.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class CategoryTotalAverageDTO {
	String category
	BigDecimal total = BigDecimal.ZERO
	BigDecimal average = BigDecimal.ZERO
}
