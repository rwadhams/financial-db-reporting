package com.wadhams.financials.db.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class CategoryAmountDTO {
	String category
	BigDecimal amount = BigDecimal.ZERO
}
