package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.Year
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService
import com.wadhams.financials.db.service.TimelineService
import com.wadhams.financials.db.type.Residence
import groovy.sql.GroovyRowResult
import groovy.transform.ToString

class BigPictureSummaryReportService {
	DatabaseQueryService databaseQueryService
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	DateService dateService
	
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Big Picture Report'
		pw.println '=================='
		pw.println ''
		
		String queryAll = buildQueryAll()
		List<FinancialDTO> financialList = databaseQueryService.buildList(queryAll)
		
		//1. determine which years are in the complete dataset
		//2. calculate grandTotal
		Set<Integer> yearSet = []
		BigDecimal grandTotal = new BigDecimal(0.0)
		financialList.each {dto ->
			yearSet << dto.transactionDt.getYear()
			grandTotal = grandTotal.add(dto.amount)
		}
//		println "Years.........: $yearSet"
//		println "GrandTotal....: ${cf.format(grandTotal)}"
		
		reportGrandTotal(grandTotal, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		List<String> allCategoryList = databaseQueryService.buildAllCategoryList()
		int maxCategorySize = commonReportingService.maxTextSize(allCategoryList)

		List<String> categoryExclusionList
		List<CategoryGroupingDTO> categoryGroupingDTOList
		
		//One Time Purchases Reporting
		categoryGroupingDTOList = buildOneTimePurchases()
//		println "categoryGroupingDTOList: $categoryGroupingDTOList"
//		println ''
		reportCategoryGroupingTotal('One Time Purchases', categoryGroupingDTOList, pw)
		categoryExclusionList = extractCategoryList(categoryGroupingDTOList)
//		println "categoryExclusionList: $categoryExclusionList"
//		println ''

		//remove One Time Purchase categories
		allCategoryList = allCategoryList - categoryExclusionList
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Camp Hill Reporting
		categoryGroupingDTOList = buildCampHill()
		reportCategoryGroupingTotal('Camp Hill', categoryGroupingDTOList, pw)
		categoryExclusionList = extractCategoryList(categoryGroupingDTOList)

		//remove Camp Hill categories
		allCategoryList = allCategoryList - categoryExclusionList
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Low Dollar Reporting
		categoryGroupingDTOList = buildLowDollar()
		reportCategoryGroupingTotal('Low Dollar Categories', categoryGroupingDTOList, pw)
		categoryExclusionList = extractCategoryList(categoryGroupingDTOList)

		//remove Low Dollar categories
		allCategoryList = allCategoryList - categoryExclusionList
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		reportCategoryYearlySummary(allCategoryList, yearSet, maxCategorySize, pw)
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
		pw.println "${'Total:'.padLeft(maxNameSize, ' ')}  ${formattedTotal.padLeft(12)}"
	}
	
	def reportCategoryYearlySummary(List<String> allCategoryList, Set<Integer> yearSet, int maxCategorySize, PrintWriter pw) {
		pw.println 'Categories by Year with Totals'
		pw.println '------------------------------'
		
		//year headings
		pw.print "${'Year: '.padLeft(maxCategorySize, ' ')}  "
		yearSet.each {year ->
			String y = year as String
			pw.print "${y.padLeft(12, ' ')}"
		}
		pw.print ' Category Total'
		pw.println ''

		String formattedTotal
		//category loop
		allCategoryList.each {cat ->
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
					formattedTotal = cf.format(BigDecimal.ZERO)
				}
				pw.print "${formattedTotal.padLeft(12)}"
			}
			formattedTotal = cf.format(rowTotal)
			pw.print "${formattedTotal.padLeft(15)}"
			pw.println ''
		}
		
		//year totals
		BigDecimal grandTotal = new BigDecimal(0.0)
		pw.print "${'Total: '.padLeft(maxCategorySize, ' ')}  "
		yearSet.each {year ->
			String querySumYear = buildQuerySumYear(year, allCategoryList)
			//println querySumYear
			GroovyRowResult grr = databaseQueryService.firstRow(querySumYear)
			def total = grr.getProperty('TOTAL')
			//println "Total: $total"
			formattedTotal = cf.format(total)
			pw.print "${formattedTotal.padLeft(12)}"
			grandTotal = grandTotal.add(total)
		}
		formattedTotal = cf.format(grandTotal)
		pw.print "${formattedTotal.padLeft(15)}"
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
		
		return categoryGroupingDTOList
	}
	
	List<CategoryGroupingDTO> buildCampHill() {
		List<CategoryGroupingDTO> categoryGroupingDTOList = []
		
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CH_ELECTRIC_UTILITIES', 'CH_FURNITURE', 'CH_GAS_UTILITIES', 'CH_HOUSEWARES', 'CH_INSURANCE', 'CH_MAINTENANCE', 'CH_RATES', 'CH_RENO_COST', 'CH_RENO_SERVICES', 'CH_SALE', 'CH_SUPPLIES', 'CH_WATER_UTILITIES'], name : 'Camp Hill', description : '')
		
		return categoryGroupingDTOList
	}
	
	List<CategoryGroupingDTO> buildLowDollar() {
		List<CategoryGroupingDTO> categoryGroupingDTOList = []
		
//		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : [''], name : '', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['ACCOUNTING_FEES'], name : 'Accounting', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['BANKING_FEES'], name : 'Banking', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CAR_REPAIR'], name : 'Car Repairs', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CARAVAN_MAINTENANCE'], name : 'Caravan Maintenance', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['CLEANING'], name : 'Cleaning', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['DRIVERS_LICENSE_MOLLY', 'DRIVERS_LICENSE_ROB'], name : 'Driver Licenses', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FINGAL_SUPPLIES'], name : 'Fingal Supplies', description : 'Diesel for generator, etc')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['FISHING'], name : 'Fishing', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['HOME_BREW'], name : 'Home Brew Beer', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['KK_REGISTRATION'], name : 'Kimberley Kamper Registration', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['KK_SERVICING'], name : 'Kimberley Kamper Servicing', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['MEDIA'], name : 'Media', description : 'DVDs, etc')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['OFFICE'], name : 'Office', description : 'Office Supplies, Printing')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PARKS_PASS'], name : 'Park Passes', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PERSONAL_GROOMING'], name : 'Personal Care', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PHONE_AND_DATA_PLAN', 'PHONE_PLAN_INTL'], name : 'Telco', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['PREPPING_EQUIPMENT'], name : 'Prepping', description : '')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['TOLLS'], name : 'Tolls', description : 'Toll Roads, Ferries')
		categoryGroupingDTOList << new CategoryGroupingDTO(categoryList : ['TRANSIT'], name : 'Public Transportation', description : '')
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
