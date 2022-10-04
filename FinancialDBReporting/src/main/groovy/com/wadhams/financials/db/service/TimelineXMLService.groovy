package com.wadhams.financials.db.service

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.wadhams.financials.db.dto.TimelineDTO
import com.wadhams.financials.db.dto.TripDTO

class TimelineXMLService {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
	
	TimelineDTO loadTimelineData() {
		TimelineDTO timelineDTO = new TimelineDTO()
		
		File tripFile
		URL resource = getClass().getClassLoader().getResource("Timeline.xml")
		if (resource == null) {
			throw new IllegalArgumentException("file not found!")
		} 
		else {
			tripFile = new File(resource.toURI())
		}
		
		def timeline = new XmlSlurper().parse(tripFile)
		//startTimelineDate
		timelineDTO.startTimelineDate = LocalDate.parse(timeline.startTimelineDate.text(), dtf)
		//endTimelineDate
		timelineDTO.endTimelineDate = LocalDate.now()
		//totalDays
		timelineDTO.totalDays = timelineDTO.startTimelineDate.until(timelineDTO.endTimelineDate, ChronoUnit.DAYS) + 1

		//CAMPING TRIPS
		def trips = timeline.trip
		List<TripDTO> campingTripList = []
		trips.each {txn ->
			//println txn
			campingTripList << build(txn)
		}
		timelineDTO.campingTripList = campingTripList
		
		long campingDays = 0L
		campingTripList.each {trip ->
			campingDays += trip.tripDays
		}
		timelineDTO.campingDays = campingDays

		//NON CAMPING TRIPS
		List<TripDTO> nonCampingTripList = []
		
		//first item
		TripDTO firstCampingTrip = campingTripList[0]
		TripDTO firstNonCampingTrip = new TripDTO()
		firstNonCampingTrip.startDate = timelineDTO.startTimelineDate
		firstNonCampingTrip.endDate = firstCampingTrip.startDate.minusDays(1L)
		firstNonCampingTrip.tripDays = firstNonCampingTrip.startDate.until(firstNonCampingTrip.endDate, ChronoUnit.DAYS) + 1
		nonCampingTripList << firstNonCampingTrip
		
		//middle items
		for (int i=0; i < campingTripList.size()-1; i++) {
			TripDTO nonCampingTrip = new TripDTO()
			nonCampingTrip.startDate = campingTripList[i].endDate.plusDays(1L)
			nonCampingTrip.endDate = campingTripList[i+1].startDate.minusDays(1L)
			nonCampingTrip.tripDays = nonCampingTrip.startDate.until(nonCampingTrip.endDate, ChronoUnit.DAYS) + 1
			nonCampingTripList << nonCampingTrip
		}
		
		//last item
		TripDTO lastCampingTrip = campingTripList[-1]
		LocalDate now = LocalDate.now()
		if (now != lastCampingTrip.endDate) {
			TripDTO lastNonCampingTrip = new TripDTO()
			lastNonCampingTrip.startDate = lastCampingTrip.endDate.plusDays(1L)
			lastNonCampingTrip.endDate = now
			lastNonCampingTrip.tripDays = lastNonCampingTrip.startDate.until(lastNonCampingTrip.endDate, ChronoUnit.DAYS) + 1
			nonCampingTripList << lastNonCampingTrip
		}
		timelineDTO.nonCampingTripList = nonCampingTripList
		
		long nonCampingDays = 0L
		nonCampingTripList.each {trip ->
			nonCampingDays += trip.tripDays
		}
		timelineDTO.nonCampingDays = nonCampingDays
		
		//nonCampingDateSet
		nonCampingTripList.each {trip ->
			LocalDate ld = trip.startDate
			while (ld.isBefore(trip.endDate)) {
				timelineDTO.nonCampingDateSet << ld
				ld = ld.next()
			}
			timelineDTO.nonCampingDateSet << trip.endDate
		}
		//println "nonCampingDateSet.size(): ${timelineDTO.nonCampingDateSet.size()}"
		
		//campingDateSet
		campingTripList.each {trip ->
			LocalDate ld = trip.startDate
			while (ld.isBefore(trip.endDate)) {
				timelineDTO.campingDateSet << ld
				ld = ld.next()
			}
			timelineDTO.campingDateSet << trip.endDate
		}
		//println "campingDateSet.size(): ${timelineDTO.campingDateSet.size()}"
		
		return timelineDTO
	}
	
	TripDTO build(txn) {
			TripDTO dto = new TripDTO()
			
			//tripName
			String tripName = txn.name.text()
			//println tripName
			dto.tripName = tripName

			//startDate
			String s1 = txn.start.text()
			dto.startDate = LocalDate.parse(s1, dtf)
			
			//endDate
			String s2 = txn.end.text()
			if (s2) {
				dto.endDate = LocalDate.parse(s2, dtf)
			}
			else {
				dto.endDate = LocalDate.now()
			}

			//tripDays
			dto.tripDays = dto.startDate.until(dto.endDate, ChronoUnit.DAYS) + 1
			
			return dto
	}
}
