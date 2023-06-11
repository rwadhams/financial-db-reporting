package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.CampingNonCampingContinuousDTO
import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.CategoryTotalAverageDTO
import com.wadhams.financials.db.dto.TimelineDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.dto.TripDTO
import com.wadhams.financials.db.helper.ListControlBreak
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.TimelineXMLService
import com.wadhams.financials.db.type.BudgetCategory
import com.wadhams.financials.db.type.ReportingAmount

class BudgetReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	DateTimeFormatter reportingdtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	BigDecimal monthsPerYear = new BigDecimal('12')
	BigDecimal daysPerYear = new BigDecimal('365')

	BigDecimal monthlyAverageTotal = BigDecimal.ZERO

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

		//Reused data structures
		String query
		List<FinancialDTO> financialDTOList
		List<CategoryTotalAverageDTO> ctaDTOList
		
		//FIXED
		query = buildQuerySpecificOngoing(budgetCategoryMap[BudgetCategory.Fixed])
		financialDTOList = databaseQueryService.buildList(query)
		reportFixed('FIXED DURATION CATEGORIES (MONTHLY AVERAGE)', financialDTOList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//CAMPING TIMELINE CATEGORIES
		ctaDTOList = []
		query = buildQueryWithTransactionDates(budgetCategoryMap[BudgetCategory.CampingTimeline], timelineDTO.campingTripList)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		//average each total
		ctaDTOList.each {cta ->
			cta.average = averageBigDecimal(cta.total, timelineDTO.campingDays)
		}
		reportCategoryTotalAverageList('CAMPING TIMELINE CATEGORIES (MONTHLY AVERAGE)', ctaDTOList, ReportingAmount.Average, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//CONTINUOUS HISTORY CATEGORIES
		ctaDTOList = []
		query = buildQueryWithGreaterTransactionDate(budgetCategoryMap[BudgetCategory.ContinuousHistory], timelineDTO.startTimelineDate)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		//average each total
		ctaDTOList.each {cta ->
			cta.average = averageBigDecimal(cta.total, timelineDTO.totalDays)
		}
		reportCategoryTotalAverageList('CONTINUOUS HISTORY CATEGORIES (MONTHLY AVERAGE)', ctaDTOList, ReportingAmount.Average, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//RECENT HISTORY
		long previousDays = 2 * 365L	//2 years
		LocalDate now = LocalDate.now()
		LocalDate startDate = now.minusDays(previousDays)
		query = buildQueryWithGreaterTransactionDate(budgetCategoryMap[BudgetCategory.RecentHistory], startDate)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		//average each total
		ctaDTOList.each {cta ->
			cta.average = averageBigDecimal(cta.total, previousDays)
		}
		String heading = "CATEGORIES FROM LAST $previousDays DAYS (MONTHLY AVERAGE)"
		reportCategoryTotalAverageList(heading, ctaDTOList, ReportingAmount.Average, pw)

		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//UNBUDGETED
		query = buildQueryCategoryTotal(budgetCategoryMap[BudgetCategory.Unbudgeted])
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		reportCategoryTotalAverageList('UNBUDGETED CATEGORIES            TOTAL', ctaDTOList, ReportingAmount.Total, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//OTHER
		query = buildQueryCategoryTotal(budgetCategoryMap[BudgetCategory.Other])
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		reportCategoryTotalAverageList('OTHER CATEGORIES                 TOTAL', ctaDTOList, ReportingAmount.Total, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		reportMonthlyYearlyTotal(pw)
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
	}
	
	
	def reportFixed(String heading, List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
			
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
			pw.println "${commonReportingService.buildFixedWidthLabel(savedCategory, 25)}${cf.format(categoryAverage).padLeft(11, ' ')}"
			reportTotal = reportTotal.add(categoryAverage)
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${cf.format(reportTotal).padLeft(11, ' ')}"
		
		monthlyAverageTotal += reportTotal
	}
	
	def reportCategoryTotalAverageList(String heading, List<CategoryTotalAverageDTO> list, ReportingAmount ra, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
		
		BigDecimal total = new BigDecimal(0.0)
		list.each {dto ->
			if (ra == ReportingAmount.Average) {
				total = total.add(dto.average)
				pw.println "${commonReportingService.buildFixedWidthLabel(dto.category, 25)}${cf.format(dto.average).padLeft(11, ' ')}"
			}
			else if (ra == ReportingAmount.Total) {
				total = total.add(dto.total)
				pw.println "${commonReportingService.buildFixedWidthLabel(dto.category, 25)}${cf.format(dto.total).padLeft(11, ' ')}"
			}
		}
		
		pw.println ''
		if (ra == ReportingAmount.Average) {
			pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${cf.format(total).padLeft(11, ' ')}"
			monthlyAverageTotal += total
		}
		else if (ra == ReportingAmount.Total) {
			pw.println "${commonReportingService.buildFixedWidthLabel('Grand Total', 25)}${cf.format(total).padLeft(11, ' ')}"
		}
		else {
			pw.println 'Unknown ReportingAmount Enum'
		}
	}
	
	def reportMonthlyYearlyTotal(PrintWriter pw) {
		String heading = 'MONTHLY and YEARLY TOTALS:'
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Total', 25)}${cf.format(monthlyAverageTotal).padLeft(11, ' ')}"
		BigDecimal yearlyAverageTotal = monthlyAverageTotal.multiply(monthsPerYear)
		pw.println "${commonReportingService.buildFixedWidthLabel('Yearly Total', 25)}${cf.format(yearlyAverageTotal).padLeft(11, ' ')}"
	}
	
	String buildQuerySpecificOngoing(List<String> categoryList) {
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

	String buildQueryCategoryTotal(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT CATEGORY as CAT, SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQueryWithTransactionDates(List<String> categoryList, List<TripDTO> tripList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT CATEGORY as CAT, SUM(AMOUNT) as TOTAL ")
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

	String buildQueryWithGreaterTransactionDate(List<String> categoryList, LocalDate startDate) {
		DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as CAT, SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(startDate.format(h2dtf))
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	BigDecimal averageBigDecimal(BigDecimal bd, long days) {
		BigDecimal numberOfDays = new BigDecimal(days)
		return bd.multiply(daysPerYear).divide(monthsPerYear, 2).divide(numberOfDays, 2)
	}
	
	Map<BudgetCategory, List<String>> buildBudgetCategoryMap() {
		Map<BudgetCategory, List<String>> budgetCategoryMap = [:]
		
		List<String> fixed = [
			'CARAVAN_INSURANCE',
			'CARAVAN_REGISTRATION',
			'CARAVAN_SERVICING',
			'CARAVAN_TYRES',
			'CAR_INSURANCE',
			'CAR_REGISTRATION',
			'CAR_SERVICING',
			'CAR_TYRES',
			'DATA_PLAN',
			'DRIVERS_LICENSE_MOLLY',
			'DRIVERS_LICENSE_ROB',
			'MEMBERSHIP',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB',
			'TRANSMISSION_SERVICING'
			]
		
		List<String> campingTimeline = [
			'CAMPING_FEES',
			'DRINKS',
			'FUEL'
			]
			
		List<String> continuousHistory = [
			'ALCOHOL',
			'FOOD'
			]
			
		List<String> recentHistory = [
			'CARAVAN_REPAIR',
			'TRAVEL_PUBLICATION',
			'CAMPING_EQUIPMENT',
			'CARAVAN_EQUIPMENT',
			'CAMPING_SUPPLIES',
			'CARAVAN_SUPPLIES',
			'CAR_REPAIR',
			'FERRY',
			'FISHING',
			'GIFTS',
			'MEDIA',
			'MEDICAL',
			'OFFICE',
			'PARKING',
			'PARKS_PASS',
			'PERSONAL_GROOMING',
			'PHARMACY',
			'SAFETY',
			'TOLLS',
			'TRANSIT',
			'CAR_SUPPLIES',
			'LAUNDRY',
			'PREPARED_FOOD',
			'ENTERTAINMENT',
			'CLOTHING',
			'CLOUD_STORAGE',
			'CAR_EQUIPMENT',
			'TECHNOLOGY',
			'TOOLS'
			]
			
		List<String> unbudgeted = [
			'BASS_STRAIT_FERRY',
			'DOMESTIC_TRAVEL',
			'ELECTRONICS',
			'MAJOR_EQUIPMENT',
			'MAJOR_WORK',
			'OVERSEAS_TRAVEL'
			]

		List<String> other = [
			'ACCOMODATION',
			'ACCOUNTING_FEES',
			'ASSET_RELATED_COST',
			'BANKING_FEES',
			'CARAVAN_STORAGE',
			'CASH',
			'CH_ELECTRIC_UTILITIES',
			'FINGAL_EQUIPMENT',
			'FINGAL_LAND_TAX',
			'FINGAL_RATES',
			'FINGAL_SHED',
			'FINGAL_SUPPLIES',
			'FINGAL_WATER',
			'FINGAL_WORK',
			'CH_FURNITURE',
			'CH_GAS_UTILITIES',
			'HOME_BREW',
			'CH_HOUSEWARES',
			'CH_INSURANCE',
			'CH_MAINTENANCE',
			'CH_SUPPLIES',
			'MISC',
			'PHONE_AND_DATA_PLAN',
			'CH_RATES',
			'CH_RENO_COST',
			'CH_RENO_SERVICES',
			'RENTAL_CAR',
			'CH_WATER_UTILITIES'
			]

			//verify hardcoded lists against database			
			List<String> categoryList = databaseQueryService.buildAllCategoryList()
			List<String> budgetList = []
			budgetList.addAll(fixed)
			budgetList.addAll(continuousHistory)
			budgetList.addAll(campingTimeline)
			budgetList.addAll(recentHistory)
			budgetList.addAll(unbudgeted)
			budgetList.addAll(other)
			Collections.sort(budgetList)
			assert budgetList == categoryList

			budgetCategoryMap[BudgetCategory.Fixed] = fixed
			budgetCategoryMap[BudgetCategory.ContinuousHistory] = continuousHistory
			budgetCategoryMap[BudgetCategory.CampingTimeline] = campingTimeline
			budgetCategoryMap[BudgetCategory.RecentHistory] = recentHistory
			budgetCategoryMap[BudgetCategory.Unbudgeted] = unbudgeted
			budgetCategoryMap[BudgetCategory.Other] = other
			
		return budgetCategoryMap
	}
	
}
