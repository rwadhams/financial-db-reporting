package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.CampingNonCampingContinuousDTO
import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.TimelineDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.dto.TripDTO
import com.wadhams.financials.db.helper.ListControlBreak
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.TimelineXMLService
import com.wadhams.financials.db.type.BudgetCategory

class BudgetReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	DateTimeFormatter reportingdtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
	
	def execute(PrintWriter pw) {
		Map<BudgetCategory, List<String>> budgetCategoryMap = buildBudgetCategoryMap()
		assert budgetCategoryMap.size() > 0
		
		TimelineXMLService timelineXMLService = new TimelineXMLService()
		TimelineDTO timelineDTO = timelineXMLService.loadTimelineData()
		//println timelineDTO
		
		reportCampingNonCampingDates(timelineDTO, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''

		//FIXED
		String query1 = buildQuery1(budgetCategoryMap[BudgetCategory.Fixed])
		//println query1
		//println ''

		List<FinancialDTO> financialList1 = databaseQueryService.buildList(query1)
		reportFixed(financialList1, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//REGULAR and OCCASSIONAL
		String query2
		List<TotalDTO> totalListBeforeAveraging
		List<TotalDTO> totalListAveraged
		
		//REGULAR
		Map<String, CampingNonCampingContinuousDTO> regularMap = [:]
		budgetCategoryMap[BudgetCategory.Regular].each {category ->
			regularMap[category] = new CampingNonCampingContinuousDTO(categoryName : category)
		}
		
		//REGULAR + CAMPING
		query2 = buildQuery2WithTransactionDates(budgetCategoryMap[BudgetCategory.Regular], timelineDTO.campingTripList)
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.campingDays)
		totalListAveraged.each {t ->
			regularMap[t.totalName].campingAmount = t.amount 
		}

		//REGULAR + NON-CAMPING
		query2 = buildQuery2WithTransactionDates(budgetCategoryMap[BudgetCategory.Regular], timelineDTO.nonCampingTripList)
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.nonCampingDays)
		totalListAveraged.each {t ->
			regularMap[t.totalName].nonCampingAmount = t.amount
		}

		//REGULAR + CONTINUOUS
		query2 = buildQuery2WithoutTransactionDates(budgetCategoryMap[BudgetCategory.Regular])
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.totalDays)
		totalListAveraged.each {t ->
			regularMap[t.totalName].continuousAmount = t.amount
		}

		reportRegularOccassional('REGULAR ITEMS', regularMap.values(), pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//OCCASSIONAL
		Map<String, CampingNonCampingContinuousDTO> occassionalMap = [:]
		budgetCategoryMap[BudgetCategory.Occassional].each {category ->
			occassionalMap[category] = new CampingNonCampingContinuousDTO(categoryName : category)
		}
		
		//OCCASSIONAL + CAMPING
		query2 = buildQuery2WithTransactionDates(budgetCategoryMap[BudgetCategory.Occassional], timelineDTO.campingTripList)
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.campingDays)
		totalListAveraged.each {t ->
			occassionalMap[t.totalName].campingAmount = t.amount 
		}

		//OCCASSIONAL + NON-CAMPING
		query2 = buildQuery2WithTransactionDates(budgetCategoryMap[BudgetCategory.Occassional], timelineDTO.nonCampingTripList)
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.nonCampingDays)
		totalListAveraged.each {t ->
			occassionalMap[t.totalName].nonCampingAmount = t.amount
		}

		//OCCASSIONAL + CONTINUOUS
		query2 = buildQuery2WithoutTransactionDates(budgetCategoryMap[BudgetCategory.Occassional])
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query2)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, timelineDTO.totalDays)
		totalListAveraged.each {t ->
			occassionalMap[t.totalName].continuousAmount = t.amount
		}

		reportRegularOccassional('OCCASSIONAL ITEMS', occassionalMap.values(), pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//EQUIPMENT
		long previousDays = 365L
		String query3 = buildQuery3(budgetCategoryMap[BudgetCategory.Equipment], previousDays)
		//println query3
		//println ''
		
		totalListBeforeAveraging = databaseQueryService.buildTotalsList(query3)
		totalListAveraged = averageTotalsList(totalListBeforeAveraging, previousDays)
		
		reportEquipment(totalListAveraged, previousDays, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//OTHER
		String query4 = buildQuery4(budgetCategoryMap[BudgetCategory.Other])
		//println query4
		//println ''
		
		List<TotalDTO> totalList = databaseQueryService.buildTotalsList(query4)
		
		reportOther(totalList, pw)
	}
	
	
	def reportCampingNonCampingDates(TimelineDTO timelineDTO, PrintWriter pw) {
		//report camping dates
		List<TripDTO> campingList = timelineDTO.campingTripList
		pw.println 'CAMPING DATES:'
		campingList.each {trip ->
			String s1 = trip.startDate.format(reportingdtf)
			String s2 = trip.endDate.format(reportingdtf)
			String s3 = trip.tripDays.toString().padLeft(4, ' ')
			pw.println "\t$s1 - $s2 $s3 days  ${trip.tripName}" 
		}
		pw.println "\tTotal Days: ${timelineDTO.campingDays}"
		pw.println ''

		//report non-camping dates
		List<TripDTO> nonCampingList = timelineDTO.nonCampingTripList
		pw.println 'NON CAMPING DATES:'
		nonCampingList.each {trip ->
			String s1 = trip.startDate.format(reportingdtf)
			String s2 = trip.endDate.format(reportingdtf)
			String s3 = trip.tripDays.toString().padLeft(4, ' ')
			pw.println "\t$s1 - $s2 $s3 days"
		}
		pw.println "\tTotal Days: ${timelineDTO.nonCampingDays}"
		pw.println ''

		pw.println "CONTINUOUS DAYS: ${timelineDTO.totalDays} days"
		pw.println ''
	}
	
	
	def reportFixed(List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println 'FIXED DURATION ITEMS (MONTHLY AVERAGE)'
		pw.println '--------------------------------------'
			
		NumberFormat nf = NumberFormat.getCurrencyInstance()

		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		
		//Total
		BigDecimal reportTotal = BigDecimal.ZERO
			
		ListControlBreak cb = new ListControlBreak(financialList)
		FinancialDTO current = cb.first()

		while (cb.hasMore()) {
			String savedCategory = current.category
			BigDecimal categoryTotal = BigDecimal.ZERO
			BigDecimal categoryDays = BigDecimal.ZERO
			while (cb.hasMore() && savedCategory == current.category) {
				//println savedCategory
				categoryTotal = categoryTotal.add(current.amount)
				int days = current.endDt - current.startDt + 1
				categoryDays = categoryDays.add(days)
				current = cb.next()
			}
			//println "$categoryTotal\t$categoryDays"
			BigDecimal categoryAverage = categoryTotal.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
			pw.println "${commonReportingService.buildFixedWidthLabel(savedCategory, 25)} ${nf.format(categoryAverage)}"
			reportTotal = reportTotal.add(categoryAverage)
		}
		pw.println ''
		pw.println "Monthly Average..........: ${nf.format(reportTotal)}"
	}
	
	def reportRegularOccassional(String heading, Collection<CampingNonCampingContinuousDTO> list, PrintWriter pw) {
		int w1 = 18
		int w2 = 12
		int w3 = 13
		int w4 = 12
		
		String h1 = heading.padRight(w1, ' ')
		String h2 = 'CAMPING'.padLeft(w2, ' ')
		String h3 = 'NON-CAMPING'.padLeft(w3, ' ')
		String h4 = 'CONTINUOUS'.padLeft(w4, ' ')
		String u1 = ''.padRight(w1+w2+w3+w4, '-')
		
		pw.println "$h1$h2$h3$h4"
		pw.println "$u1"
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal campingTotal = new BigDecimal(0.0)
		BigDecimal nonCampingTotal = new BigDecimal(0.0)
		BigDecimal continuousTotal = new BigDecimal(0.0)
		
		list.each {dto ->
			campingTotal = campingTotal.add(dto.campingAmount)
			nonCampingTotal = nonCampingTotal.add(dto.nonCampingAmount)
			continuousTotal = continuousTotal.add(dto.continuousAmount)
			String col1 = dto.categoryName.padRight(w1, ' ')
			String col2 = nf.format(dto.campingAmount).padLeft(w2, ' ')
			String col3 = nf.format(dto.nonCampingAmount).padLeft(w3, ' ')
			String col4 = nf.format(dto.continuousAmount).padLeft(w4, ' ')
			pw.println "$col1$col2$col3$col4"
		}
		String t1 = 'Monthly Averages:'.padRight(w1, ' ')
		String t2 = nf.format(campingTotal).padLeft(w2, ' ')
		String t3 = nf.format(nonCampingTotal).padLeft(w3, ' ')
		String t4 = nf.format(continuousTotal).padLeft(w4, ' ')

		pw.println ''
		pw.println "$t1$t2$t3$t4"

	}
	
	def reportEquipment(List<TotalDTO> totalList, long previousDays, PrintWriter pw) {
		String h1 = "EQUIPMENT ITEMS FROM LAST $previousDays DAYS (MONTHLY AVERAGE)"
		String u1 = ''.padRight(h1.size(), '-')
		pw.println h1
		pw.println u1
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal reportTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			reportTotal = reportTotal.add(dto.amount)
			String col1 = dto.totalName
			String col2 = nf.format(dto.amount).padLeft(6, ' ')
			pw.println "${commonReportingService.buildFixedWidthLabel(col1, 25)} $col2"
		}
		pw.println ''
		pw.println "Monthly Average..........: ${nf.format(reportTotal)}"

	}
	
	def reportOther(List<TotalDTO> totalList, PrintWriter pw) {
		pw.println 'OTHER ITEMS                 TOTAL'
		pw.println '---------------------------------'
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal grandTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			grandTotal = grandTotal.add(dto.amount)
			String col1 = dto.totalName.padRight(21, ' ')
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			pw.println "$col1$col2"
		}
		pw.println ''
		pw.println "Grand Total.........: ${nf.format(grandTotal)}"

	}
	
	String buildQuery1(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 IN ('SPECIFIC_RUNNING_COST', 'ONGOING_RUNNING_COST') ")
		sb.append("AND CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQuery2WithTransactionDates(List<String> categoryList, List<TripDTO> tripList) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("AND (")
		sb.append("TRANSACTION_DT BETWEEN '")
		sb.append(tripList[0].startDate)
		sb.append("' AND '")
		sb.append(tripList[0].endDate)
		sb.append("' ")
		tripList[1..-1].each {trip ->
			sb.append("OR TRANSACTION_DT BETWEEN '")
			sb.append(trip.startDate)
			sb.append("' AND '")
			sb.append(trip.endDate)
			sb.append("' ")
		}
		sb.append(") GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQuery2WithoutTransactionDates(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQuery3(List<String> categoryList, long previousDays) {
		DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		LocalDate now = LocalDate.now()
		LocalDate previousDate = now.minusDays(previousDays)
		
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("AND TRANSACTION_DT > '")
		sb.append(previousDate.format(h2dtf))
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQuery4(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	List<TotalDTO> averageTotalsList(List<TotalDTO> tl, long days) {
		List<TotalDTO> totalList = []
		
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		BigDecimal numberOfDays = new BigDecimal(days)
		
		tl.each {t ->
			BigDecimal categoryAverage = t.amount.multiply(daysPerYear).divide(monthsPerYear, 2).divide(numberOfDays, 2)
			
			TotalDTO dto = new TotalDTO()
			dto.totalName = t.totalName
			dto.amount = categoryAverage
			totalList << dto
		}
		
		return totalList
	}
	
	Map<BudgetCategory, List<String>> buildBudgetCategoryMap() {
		Map<BudgetCategory, List<String>> budgetCategoryMap = [:]
		
		List<String> fixed = [
			'CARAVAN_INSURANCE',
			'CARAVAN_REGISTRATION',
			'CARAVAN_SERVICING',
			'CAR_INSURANCE',
			'CAR_REGISTRATION',
			'CAR_SERVICING',
			'CAR_TYRES',
			'DATA_PLAN',
			'DRIVERS_LICENSE_MOLLY',
			'DRIVERS_LICENSE_ROB',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB',
			'MEMBERSHIP',
			'TRANSMISSION_SERVICING'
			]
		
		List<String> regular = [
			'ALCOHOL',
			'CAMPING_FEES',
			'CAMPING_SUPPLIES',
			'CARAVAN_SUPPLIES',
			'CAR_SUPPLIES',
			'CLOTHING',
			'CLOUD_STORAGE',
			'DRINKS',
			'ENTERTAINMENT',
			'FOOD',
			'FUEL',
			'LAUNDRY',
			'PREPARED_FOOD'
			]
			
		List<String> occassional = [
			'CARAVAN_REPAIR',
			'CAR_REPAIR',
			'FERRY',
			'FISHING',
			'GIFTS',
			'MEDICAL',
			'OFFICE',
			'PARKING',
			'PARKS_PASS',
			'PERSONAL_GROOMING',
			'PHARMACY',
			'SAFETY',
			'TOLLS',
			'TRANSIT',
			'TRAVEL_PUBLICATION'
			]
			
		List<String> equipment = [
			'CAMPING_EQUIPMENT',
			'CARAVAN_EQUIPMENT',
			'CAR_EQUIPMENT',
			'TECHNOLOGY',
			'TOOLS'
			]
			
		List<String> other = [
			'ACCOMODATION',
			'ACCOUNTING_FEES',
			'BANKING_FEES',
			'CARAVAN_STORAGE',
			'CASH',
			'ELECTRIC_UTILITIES',
			'FURNITURE',
			'GAS_UTILITIES',
			'HOME_BREW',
			'HOUSEWARES',
			'HOUSE_INSURANCE',
			'HOUSE_MAINTENANCE',
			'HOUSE_SALE',
			'HOUSE_SUPPLIES',
			'MISC',
			'OVERSEAS_TRAVEL',
			'PHONE_AND_DATA_PLAN',
			'PURCHASE_DEPOSIT',
			'PURCHASE_PAYMENT',
			'PURCHASE_STAMP_DUTY',
			'RATES',
			'RENO',
			'RENO_SERVICES',
			'RENTAL_CAR',
			'WATER_UTILITIES'
			]

			//verify hardcoded lists against database			
			List<String> categoryList = databaseQueryService.buildAllCategoryList()
			List<String> budgetList = []
			budgetList.addAll(fixed)
			budgetList.addAll(regular)
			budgetList.addAll(occassional)
			budgetList.addAll(equipment)
			budgetList.addAll(other)
			Collections.sort(budgetList)
			assert budgetList == categoryList

			budgetCategoryMap[BudgetCategory.Fixed] = fixed
			budgetCategoryMap[BudgetCategory.Regular] = regular
			budgetCategoryMap[BudgetCategory.Occassional] = occassional
			budgetCategoryMap[BudgetCategory.Equipment] = equipment
			budgetCategoryMap[BudgetCategory.Other] = other
			
		return budgetCategoryMap
	}
	
}
