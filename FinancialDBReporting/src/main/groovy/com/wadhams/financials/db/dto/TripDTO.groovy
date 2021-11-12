package com.wadhams.financials.db.dto

import groovy.transform.ToString
import java.time.LocalDate

@ToString(includeNames=true)
class TripDTO {
	String tripName
	LocalDate startDate
	LocalDate endDate
	long tripDays
}
