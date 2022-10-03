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
		
		//CAMPING TIMELINE CATEGORIES
		List<CategoryTotalAverageDTO> campingTimelineList = []
		//TODO Investigate overlapping buildQuery methods
		String query5 = buildQuery5WithTransactionDates(budgetCategoryMap[BudgetCategory.CampingTimeline], timelineDTO.campingTripList)
		List<TotalDTO> totalList5 = databaseQueryService.buildTotalsList(query5)
		totalList5.each {t ->
			BigDecimal average = averageBigDecimal(t.totalAmount, timelineDTO.campingDays)
			campingTimelineList << new CategoryTotalAverageDTO(categoryName : t.totalName, total : t.totalAmount, average : average)
		}
		
		reportCategoryTotalAverageList('CAMPING TIMELINE CATEGORIES', campingTimelineList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//CONTINUOUS HISTORY CATEGORIES
		List<CategoryTotalAverageDTO> continuousHistoryList = []
		String query6 = buildQuery6WithoutTransactionDates(budgetCategoryMap[BudgetCategory.ContinuousHistory])
		List<TotalDTO> totalList6 = databaseQueryService.buildTotalsList(query6)
		totalList6.each {t ->
			BigDecimal average = averageBigDecimal(t.totalAmount, timelineDTO.totalDays)
			continuousHistoryList << new CategoryTotalAverageDTO(categoryName : t.totalName, total : t.totalAmount, average : average)
		}
		
		reportCategoryTotalAverageList('CONTINUOUS HISTORY CATEGORIES', continuousHistoryList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//RECENT HISTORY
		long previousDays = 2 * 365L	//2 years
		String query3 = buildQuery3(budgetCategoryMap[BudgetCategory.RecentHistory], previousDays)
		//println query3
		//println ''
		
		List<TotalDTO> totalListBeforeAveraging = databaseQueryService.buildTotalsList(query3)
		List<TotalDTO> totalListAveraged = averageTotalsList(totalListBeforeAveraging, previousDays)
		
		reportRecentHistory(totalListAveraged, previousDays, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//UNBUDGETED
		//TODO
		String query7 = buildQuery7(budgetCategoryMap[BudgetCategory.Unbudgeted])
		//println query7
		//println ''
		
		List<TotalDTO> totalList7 = databaseQueryService.buildTotalsList(query7)
		
		reportUnbudgeted(totalList7, pw)
		
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
	}
	
	
	def reportFixed(List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println 'FIXED DURATION CATEGORIES (MONTHLY AVERAGE)'
		pw.println '-------------------------------------------'
			
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
			pw.println "${commonReportingService.buildFixedWidthLabel(savedCategory, 25)}${nf.format(categoryAverage).padLeft(11, ' ')}"
			reportTotal = reportTotal.add(categoryAverage)
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${nf.format(reportTotal).padLeft(11, ' ')}"
	}
	
	def reportCategoryTotalAverageList(String heading, Collection<CategoryTotalAverageDTO> list, PrintWriter pw) {
		String h1 = "$heading (MONTHLY AVERAGE)"
		pw.println h1
		
		String u1 = ''.padRight(h1.size(), '-')
		pw.println u1
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal total = new BigDecimal(0.0)
		
		list.each {dto ->
			total = total.add(dto.average)
			pw.println "${commonReportingService.buildFixedWidthLabel(dto.categoryName, 25)}${nf.format(dto.average).padLeft(11, ' ')}"
		}
		
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${nf.format(total).padLeft(11, ' ')}"
	}
	
	def reportRecentHistory(List<TotalDTO> totalList, long previousDays, PrintWriter pw) {
		String h1 = "CATEGORIES FROM LAST $previousDays DAYS (MONTHLY AVERAGE)"
		pw.println h1
		
		String u1 = ''.padRight(h1.size(), '-')
		pw.println u1
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal reportTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			reportTotal = reportTotal.add(dto.totalAmount)
			pw.println "${commonReportingService.buildFixedWidthLabel(dto.totalName, 25)}${nf.format(dto.totalAmount).padLeft(11, ' ')}"
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${nf.format(reportTotal).padLeft(11, ' ')}"
	}
	
	def reportUnbudgeted(List<TotalDTO> totalList, PrintWriter pw) {
		pw.println 'UNBUDGETED CATEGORIES            TOTAL'
		pw.println '--------------------------------------'
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal grandTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			grandTotal = grandTotal.add(dto.totalAmount)
			pw.println "${commonReportingService.buildFixedWidthLabel(dto.totalName, 25)}${nf.format(dto.totalAmount).padLeft(11, ' ')}"
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Grand Total', 25)}${nf.format(grandTotal).padLeft(11, ' ')}"
	}
	
	def reportOther(List<TotalDTO> totalList, PrintWriter pw) {
		pw.println 'OTHER CATEGORIES                 TOTAL'
		pw.println '--------------------------------------'
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal grandTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			grandTotal = grandTotal.add(dto.totalAmount)
			pw.println "${commonReportingService.buildFixedWidthLabel(dto.totalName, 25)}${nf.format(dto.totalAmount).padLeft(11, ' ')}"
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Grand Total', 25)}${nf.format(grandTotal).padLeft(11, ' ')}"
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

//	String buildQuery2WithTransactionDates(List<String> categoryList, List<TripDTO> tripList) {
//		StringBuilder sb = new StringBuilder()
//		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
//		sb.append("FROM FINANCIAL ")
//		sb.append("WHERE CATEGORY IN (")
//		sb.append(databaseQueryService.buildFormattedList(categoryList))
//		sb.append(") ")
//		sb.append("AND (")
//		sb.append("TRANSACTION_DT BETWEEN '")
//		sb.append(tripList[0].startDate)
//		sb.append("' AND '")
//		sb.append(tripList[0].endDate)
//		sb.append("' ")
//		tripList[1..-1].each {trip ->
//			sb.append("OR TRANSACTION_DT BETWEEN '")
//			sb.append(trip.startDate)
//			sb.append("' AND '")
//			sb.append(trip.endDate)
//			sb.append("' ")
//		}
//		sb.append(") GROUP BY CATEGORY ")
//		sb.append("ORDER BY CATEGORY")
//		
//		return sb.toString()
//	}

//	String buildQuery2WithoutTransactionDates(List<String> categoryList) {
//		StringBuilder sb = new StringBuilder()
//		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
//		sb.append("FROM FINANCIAL ")
//		sb.append("WHERE CATEGORY IN (")
//		sb.append(databaseQueryService.buildFormattedList(categoryList))
//		sb.append(") ")
//		sb.append("GROUP BY CATEGORY ")
//		sb.append("ORDER BY CATEGORY")
//		
//		return sb.toString()
//	}

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

	String buildQuery7(List<String> categoryList) {
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

	String buildQuery5WithTransactionDates(List<String> categoryList, List<TripDTO> tripList) {
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

	String buildQuery6WithoutTransactionDates(List<String> categoryList) {
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
			BigDecimal categoryAverage = t.totalAmount.multiply(daysPerYear).divide(monthsPerYear, 2).divide(numberOfDays, 2)
			
			TotalDTO dto = new TotalDTO()
			dto.totalName = t.totalName
			dto.totalAmount = categoryAverage
			totalList << dto
		}
		
		return totalList
	}
	
	BigDecimal averageBigDecimal(BigDecimal bd, long days) {
		//TODO
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		BigDecimal numberOfDays = new BigDecimal(days)
		
		return bd.multiply(daysPerYear).divide(monthsPerYear, 2).divide(numberOfDays, 2)
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
			'CARAVAN_TYRES',
			'DATA_PLAN',
			'DRIVERS_LICENSE_MOLLY',
			'DRIVERS_LICENSE_ROB',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB',
			'MEMBERSHIP',
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
			'ELECTRONICS',
			'OVERSEAS_TRAVEL'
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
