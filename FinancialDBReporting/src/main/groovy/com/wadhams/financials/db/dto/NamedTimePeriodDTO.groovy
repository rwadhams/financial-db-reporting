package com.wadhams.financials.db.dto

import com.wadhams.financials.db.type.Residence
import groovy.transform.ToString
import java.time.LocalDate

@ToString(includeNames=true)
class NamedTimePeriodDTO {
	Residence residence
	String name
	LocalDate startDate
	LocalDate endDate
	long days
}
