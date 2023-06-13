package com.wadhams.financials.db.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.wadhams.financials.db.dto.NamedTimePeriodDTO
import com.wadhams.financials.db.type.Residence

class TimelineService {
	DateService dateService = new DateService()
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
	
	//Timeline data
	LocalDate startTimelineDate
	LocalDate endTimelineDate
	long totalDays = 0L
	List<NamedTimePeriodDTO> namedTimePeriodDTOList
	long caravanDays = 0L
	long nonCaravanDays = 0L
	Set<LocalDate> caravanDateSet = []
	Set<LocalDate> nonCaravanDateSet = []

	def TimelineService() {
		parseXML()
		buildAdditionalValues()
	}
	
	Residence determineResidence(LocalDate ld) {
		if (caravanDateSet.contains(ld)) {
			return Residence.Caravan
		}
		else if (nonCaravanDateSet.contains(ld)) {
			return Residence.NonCaravan
		}
		else if (ld.isBefore(startTimelineDate)) {
			return Residence.BeforeCaravan
		}
		else {
			return Residence.Unknown
		}
	}
	
	List<NamedTimePeriodDTO> getCaravanNamedTimePeriodDTOList() {
		return namedTimePeriodDTOList.findAll {it.residence == Residence.Caravan}
	}
	
	List<NamedTimePeriodDTO> getNonCaravanNamedTimePeriodDTOList() {
		return namedTimePeriodDTOList.findAll {it.residence == Residence.NonCaravan}
	}
	
	def reportTimeline(PrintWriter pw) {
		pw.println "startTimelineDate...: $startTimelineDate"
		pw.println "endTimelineDate.....: $endTimelineDate"
		pw.println "totalDays...........: $totalDays"
		pw.println "caravanDays.........: $caravanDays"
		pw.println "nonCaravanDays......: $nonCaravanDays"
		pw.println ''
		namedTimePeriodDTOList.each {ntp -> 
			String s1 = ntp.startDate.format(dtf)
			String s2 = ntp.endDate.format(dtf)
			String s3 = ntp.days.toString().padLeft(4, ' ')
			pw.println "$s1 - $s2 $s3 days  ${ntp.residence}\t${(ntp.name) ? ntp.name : ''}"
		}
		pw.println ''
	}
	
	def parseXML() {
		File tripFile
		URL resource = getClass().getClassLoader().getResource("Timeline2.xml")
		if (resource == null) {
			throw new IllegalArgumentException("file not found!")
		}
		else {
			tripFile = new File(resource.toURI())
		}
		
		def timeline = new XmlSlurper().parse(tripFile)
		//startTimelineDate
		startTimelineDate = LocalDate.parse(timeline.startTimelineDate.text(), dtf)
		//endTimelineDate
		endTimelineDate = dateService.maxTransactionDate
		//totalDays
		//totalDays = startTimelineDate.until(endTimelineDate, ChronoUnit.DAYS) + 1
		totalDays = ChronoUnit.DAYS.between(startTimelineDate, endTimelineDate) + 1
		
		//NamedTimePeriod
		def ntpNodes = timeline.namedTimePeriod
		namedTimePeriodDTOList = []
		ntpNodes.each {ntp ->
			//println txn
			namedTimePeriodDTOList << buildCaravanDTO(ntp)
			NamedTimePeriodDTO dto = buildPartialNonCaravanDTO(ntp)
			if (dto) {
				namedTimePeriodDTOList << dto
			}
		}
		//Complete the NonCaravan namedTimePeriods with a endDate and days
		for (int i = 1; i < namedTimePeriodDTOList.size(); i+=2) {
			NamedTimePeriodDTO nonCaravan = namedTimePeriodDTOList[i]	//nonCaravan
			assert nonCaravan.residence == Residence.NonCaravan
			NamedTimePeriodDTO caravan = namedTimePeriodDTOList[i+1]	//caravan
			assert caravan.residence == Residence.Caravan
			nonCaravan.endDate = caravan.startDate.minusDays(1L)
			nonCaravan.days = ChronoUnit.DAYS.between(nonCaravan.startDate, nonCaravan.endDate) + 1
		}
	}
	
	def buildAdditionalValues() {
		List<NamedTimePeriodDTO> caravanNamedTimePeriodDTOList = getCaravanNamedTimePeriodDTOList()
		caravanNamedTimePeriodDTOList.each {ntp -> caravanDays += ntp.days}
		
		List<NamedTimePeriodDTO> nonCaravanNamedTimePeriodDTOList = getNonCaravanNamedTimePeriodDTOList()
		nonCaravanNamedTimePeriodDTOList.each {ntp -> nonCaravanDays += ntp.days}
		
		//caravanDateSet
		caravanNamedTimePeriodDTOList.each {ntp ->
			LocalDate ld = ntp.startDate
			while (ld.isBefore(ntp.endDate)) {
				caravanDateSet << ld
				ld = ld.next()
			}
			caravanDateSet << ntp.endDate
		}
		
		//nonCaravanDateSet
		nonCaravanNamedTimePeriodDTOList.each {ntp ->
			LocalDate ld = ntp.startDate
			while (ld.isBefore(ntp.endDate)) {
				nonCaravanDateSet << ld
				ld = ld.next()
			}
			nonCaravanDateSet << ntp.endDate
		}
	}
	
	NamedTimePeriodDTO buildCaravanDTO(txn) {
			NamedTimePeriodDTO dto = new NamedTimePeriodDTO()
			
			dto.residence = Residence.Caravan
			
			//name
			String name = txn.name.text()
			dto.name = name

			//startDate
			String s1 = txn.start.text()
			dto.startDate = LocalDate.parse(s1, dtf)
			
			//endDate
			String s2 = txn.end.text()
			if (s2) {
				dto.endDate = LocalDate.parse(s2, dtf)
			}
			else {
				dto.endDate = dateService.maxTransactionDate
			}

			//tripDays
			dto.days = ChronoUnit.DAYS.between(dto.startDate, dto.endDate) + 1
			
			return dto
	}
	
	NamedTimePeriodDTO buildPartialNonCaravanDTO(txn) {
			//If endDate is null, return null
			String s1 = txn.end.text()
			if (!s1) {
				return null
			}

			NamedTimePeriodDTO dto = new NamedTimePeriodDTO()

			dto.residence = Residence.NonCaravan
			
			//name
			String name = txn.after.text()
			if (name) {
				dto.name = name
			}

			//startDate = endDate + 1
			LocalDate ld = LocalDate.parse(s1, dtf)
			dto.startDate = ld.plusDays(1L)

			//tripDays
			dto.days = 0L
			
			return dto
	}
}
