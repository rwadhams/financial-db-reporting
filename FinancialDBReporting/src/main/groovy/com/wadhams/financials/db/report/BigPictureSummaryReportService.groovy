package com.wadhams.financials.db.report

import static java.math.RoundingMode.UP

import java.text.NumberFormat
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService
import com.wadhams.financials.db.service.TimelineService
import com.wadhams.financials.db.type.Residence
import com.wadhams.financials.db.type.SQLOrdering

import groovy.sql.GroovyRowResult
import groovy.transform.ToString

class BigPictureSummaryReportService {
	DatabaseQueryService databaseQueryService
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	DateService dateService
	
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	BigDecimal monthsPerYear = new BigDecimal('12')
	BigDecimal daysPerYear = new BigDecimal('365')

	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Big Picture Report'
		pw.println '=================='
		pw.println ''
		
		String queryAll = buildQueryAll()
		List<FinancialDTO> financialList = databaseQueryService.buildList(queryAll)
		
		//1. determine which years are in the complete dataset
		//2. calculate grandTotal
		//3. calculate totalsDays based on start and end transaction dates
		Set<Integer> yearSet = []
		BigDecimal grandTotal = new BigDecimal(0.0)
		financialList.each {dto ->
			yearSet << dto.transactionDt.getYear()
			grandTotal = grandTotal.add(dto.amount)
		}
		BigDecimal totalDays = (ChronoUnit.DAYS.between(dateService.minTransactionDate, dateService.maxTransactionDate) + 1L) as BigDecimal
		BigDecimal averagingDivisor = monthsPerYear.multiply(totalDays).divide(daysPerYear, 1, UP)
		
//		println "Years..............: $yearSet"
//		println "GrandTotal.........: ${cf.format(grandTotal)}"
//		println "TotalDays..........: $totalDays"
//		println "AveragingDivisor...: $averagingDivisor"
//		println ''
		
		reportGrandTotal(grandTotal, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		List<String> allCategoryList = databaseQueryService.buildAllCategoryList()
		int maxCategorySize = commonReportingService.maxTextSize(allCategoryList)

		//One Time Purchases Reporting
		List<CategoryGroupingDTO> oneTimePurchasesCategoryGroupingDTOList = buildOneTimePurchases()
//		println "categoryGroupingDTOList: $categoryGroupingDTOList"
//		println ''
		reportCategoryGroupingTotal('One Time Purchases', oneTimePurchasesCategoryGroupingDTOList, pw)

		//remove One Time Purchase categories
		List<String> remainingCategoryList = allCategoryList - extractCategoryList(oneTimePurchasesCategoryGroupingDTOList)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Camp Hill Preparation
		List<CategoryGroupingDTO> campHillCategoryGroupingDTOList = buildCampHill()
		//remove Camp Hill categories
		remainingCategoryList = remainingCategoryList - extractCategoryList(campHillCategoryGroupingDTOList)
		
		//Low Dollar Preparation
		List<CategoryGroupingDTO> lowUsageLowDollarCategoryGroupingDTOList = buildLowUsageLowDollar()
		//remove LowUsageLowDollar categories
		remainingCategoryList = remainingCategoryList - extractCategoryList(lowUsageLowDollarCategoryGroupingDTOList)
		
		reportCategoryYearlySummary(remainingCategoryList, yearSet, maxCategorySize, averagingDivisor, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Camp Hill Reporting
		reportCategoryGroupingTotal('Camp Hill', campHillCategoryGroupingDTOList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Low Dollar Reporting
		reportCategoryGroupingTotal('Low Usage / Low Dollar Categories', lowUsageLowDollarCategoryGroupingDTOList, pw)
	}
	
	def reportGrandTotal(BigDecimal grandTotal, PrintWriter pw) {
		pw.println "Grand Total...: ${cf.format(grandTotal)} over ${dateService.formatPeriodBetweenDates(dateService.minTransactionDate, dateService.maxTransactionDate)}"
	}
	
	def reportCategoryGroupingTotal(String heading, List<CategoryGroupingDTO> categoryGroupingDTOList, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
		
		//maxNameSize
		List<String> nameList = []
		categoryGroupingDTOList.each {dto ->
			nameList << dto.name
		}
		int maxNameSize = commonReportingService.maxTextSize(nameList)
		
		String formattedTotal
		BigDecimal reportTotal = new BigDecimal(0.0)
		
		categoryGroupingDTOList.each {dto ->
			String querySumCategoryList = buildQuerySumCategoryList(dto.categoryList)
			//println querySumCategoryList
			GroovyRowResult grr = databaseQueryService.firstRow(querySumCategoryList)
			def total = grr.getProperty('TOTAL')
			//println "Total: $total"
			formattedTotal = cf.format(total)
			pw.println "${dto.name.padRight(maxNameSize, ' ')}  ${formattedTotal.padLeft(12)}  ${dto.description}"
			reportTotal = reportTotal.add(total)
		}
		formattedTotal = cf.format(reportTotal)
		pw.println ''
		pw.println "${'Total:'.padLeft(maxNameSize, ' ')}  ${formattedTotal.padLeft(12)}"
	}
	
	def reportCategoryYearlySummary(List<String> remainingCategoryList, Set<Integer> yearSet, int maxCategorySize, BigDecimal averagingDivisor, PrintWriter pw) {
		pw.println 'Summary Categories by Year with Totals'
		pw.println '--------------------------------------'
		
		//reorder the remainingCategoryList based on descending dollar totals
		List<String> reorderCategoryList = databaseQueryService.orderCategoryList(remainingCategoryList, SQLOrdering.Decsending, null)
		
		//year headings
		pw.print "${'Year:'.padLeft(maxCategorySize, ' ')}  "
		yearSet.each {year ->
			String y = year as String
			pw.print "${y.padLeft(12, ' ')}"
		}
		pw.print '     Category'
		pw.print '     Monthly'
		pw.println ''
		//year heading underline
		pw.print "${'-----'.padLeft(maxCategorySize, ' ')}  "
		yearSet.size().times {
			pw.print "${'----'.padLeft(12, ' ')}"
		}
		pw.print '        Total'
		pw.print '     Average'
		pw.println ''

		String formattedTotal
		//category loop
		reorderCategoryList.each {cat ->
			BigDecimal rowTotal = new BigDecimal(0.0)
			print "${cat.padRight(maxCategorySize, ' ')}  "
			//year loop
			yearSet.each {year ->
				String querySumCategoryYear = buildQuerySumCategoryYear(cat, year)
				//println querySumCategoryYear
				GroovyRowResult grr = databaseQueryService.firstRow(querySumCategoryYear)
				def total = grr.getProperty('TOTAL')
				//println "Total: $total"
				if (total) {
					formattedTotal = cf.format(total)
					rowTotal = rowTotal.add(total)
				}
				else {
//					formattedTotal = cf.format(BigDecimal.ZERO)
					formattedTotal = '-----'
				}
				pw.print "${formattedTotal.padLeft(12)}"
			}
			formattedTotal = cf.format(rowTotal)
			pw.print "${formattedTotal.padLeft(13)}"
			if (categoryListService.dayToDayCategoryList.contains(cat)) {
				formattedTotal = cf.format(rowTotal.divide(averagingDivisor, 2, UP))
				pw.print "${formattedTotal.padLeft(12)}"
			}
			pw.println ''
		}
		
		pw.println ''
		//year totals
		BigDecimal grandTotal = new BigDecimal(0.0)
		pw.print "${'Total: '.padLeft(maxCategorySize, ' ')}  "
		yearSet.each {year ->
			String querySumYear = buildQuerySumYear(year, reorderCategoryList)
			//println querySumYear
			GroovyRowResult grr = databaseQueryService.firstRow(querySumYear)
			def total = grr.getProperty('TOTAL')
			//println "Total: $total"
			formattedTotal = cf.format(total)
			pw.print "${formattedTotal.padLeft(12)}"
			grandTotal = grandTotal.add(total)
		}
		formattedTotal = cf.format(grandTotal)
		pw.print "${formattedTotal.padLeft(13)}"
		pw.println ''
	}
	
	List<CategoryGroupingDTO> buildOneTimePurchases() {
		List<CategoryGroupingDTO> categoryGroupingDTOList = []
		
//		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : [''], name : '', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_LAND'], name : 'Fingal Land Purchase', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_SHED'], name : 'Fingal Shed', description : 'Excavation, Concrete slab, Installation')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CARAVAN_PURCHASE'], name : 'Salute Caravan', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CAR_PURCHASE'], name : 'Toyota Landcruiser', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_STUDIO'], name : 'Fingal Studio', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['KK_PURCHASE'], name : 'Kimberley Kamper', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_IMPROVEMENTS'], name : 'Fingal Improvements', description : 'Fencing, Drinking water, Some excavation work')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['NZ_CAMPERVAN'], name : 'NZ Campervan', description : 'Purchase, Sale, Insurance, RUC, Rego, Maintenance, Setup, Ferry, NZMCA')
		
		return categoryGroupingDTOList
	}
	
	List<CategoryGroupingDTO> buildCampHill() {
		List<CategoryGroupingDTO> categoryGroupingDTOList = []
		
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CH_ELECTRIC_UTILITIES', 'CH_FURNITURE', 'CH_GAS_UTILITIES', 'CH_HOUSEWARES', 'CH_INSURANCE', 'CH_MAINTENANCE', 'CH_RATES', 'CH_RENO_COST', 'CH_RENO_SERVICES', 'CH_SALE', 'CH_SUPPLIES', 'CH_WATER_UTILITIES'], name : 'Camp Hill', description : '')
		
		return categoryGroupingDTOList
	}
	
	List<CategoryGroupingDTO> buildLowUsageLowDollar() {
		List<CategoryGroupingDTO> categoryGroupingDTOList = []
		
//		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : [''], name : '', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['ACCOUNTING_FEES'], name : 'Accounting', description : 'Camp Hill Sale')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['BANKING_FEES'], name : 'Banking', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CAR_REPAIR'], name : 'Car Repairs', description : 'Taillight, Tyre repair, Windscreen chip')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CARAVAN_MAINTENANCE'], name : 'Caravan Maintenance', description : 'New Tyres')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CLEANING'], name : 'Cleaning', description : 'Vehicle cleaning products, car wash')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['DRIVERS_LICENSE_MOLLY', 'DRIVERS_LICENSE_ROB'], name : 'Driver Licenses', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_SUPPLIES'], name : 'Fingal Supplies', description : 'Diesel for generator, Fuel, etc')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FISHING'], name : 'Fishing', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['HOME_BREW'], name : 'Home Brew Beer', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['KK_REGISTRATION'], name : 'Kimberley Kamper Registration', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['KK_SERVICING'], name : 'Kimberley Kamper Servicing', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['MEDIA'], name : 'Media', description : 'DVDs, Books, etc')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['OFFICE'], name : 'Office', description : 'Office Supplies, Printing')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['ONROAD_FEES'], name : 'Onroad Fees', description : 'Ferries, Weighbridge, RACQ Claim excess')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PARKS_PASS'], name : 'Park Passes', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PERSONAL_GROOMING'], name : 'Personal Care', description : 'Haircuts, Health & Beauty products')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PHONE_AND_DATA_PLAN', 'PHONE_PLAN_INTL'], name : 'Telco', description : 'Old Plans, Intl Phone plans')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PREPPING_EQUIPMENT'], name : 'Prepping', description : 'First Aid Kit, Jerrycans, Propane')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['TOLLS'], name : 'Tolls', description : 'Toll Roads')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['TRANSIT'], name : 'Transportation', description : 'Bus, Train, Taxi, Uber')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['TRAVEL_PUBLICATION'], name : 'Travel Material', description : 'Maps, Road Atlas, Travel Guides')
		
		return categoryGroupingDTOList
	}
	
	List<String> extractCategoryList(List<CategoryGroupingDTO> categoryGroupingDTOList) {
		List<String> categoryList = []
		
		categoryGroupingDTOList.each {dto ->
			categoryList.addAll(dto.categoryList)
		}
		
		return categoryList
	}
	
	String buildQueryAll() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("ORDER BY TRANSACTION_DT")
		
		return sb.toString()
	}
	
	String buildQuerySumCategoryYear(String cat, int year) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '${cat}' ")
		sb.append("AND ")
		sb.append("TRANSACTION_DT BETWEEN '${year}-01-01' AND '${year}-12-31'")
		
		return sb.toString()
	}
	
	String buildQuerySumYear(int year, List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT BETWEEN '${year}-01-01' AND '${year}-12-31'")
		sb.append("AND CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")

		return sb.toString()
	}
	
	String buildQuerySumCategoryList(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		
		return sb.toString()
	}
	
	@ToString(includeNames=true)
	class CategoryGroupingDTO {
		List<String> categoryList
		String name
		String description
	}
}
