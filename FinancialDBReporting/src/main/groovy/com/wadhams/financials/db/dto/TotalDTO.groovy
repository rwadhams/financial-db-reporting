package com.wadhams.financials.db.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class TotalDTO {
	String totalName
	BigDecimal totalAmount
}
