package com.wadhams.financials.db.dto

import groovy.transform.ToString
import java.time.LocalDate

@ToString(includeNames=true)
class TimelineDTO {
	LocalDate startTimelineDate
	LocalDate endTimelineDate
	long totalDays
	
	List<TripDTO> campingTripList = []
	long campingDays
	
	List<TripDTO> nonCampingTripList = []
	long nonCampingDays
}
