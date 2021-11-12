package com.wadhams.financials.db.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class CampingNonCampingContinuousDTO {
	String categoryName
	BigDecimal campingAmount = BigDecimal.ZERO
	BigDecimal nonCampingAmount = BigDecimal.ZERO
	BigDecimal continuousAmount = BigDecimal.ZERO
}
