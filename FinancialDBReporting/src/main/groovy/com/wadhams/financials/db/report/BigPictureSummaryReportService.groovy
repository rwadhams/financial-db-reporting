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

class BigPictureSummaryReportService {
	DatabaseQueryService databaseQueryService
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Big Picture Report'
		pw.println '=================='
		pw.println ''
		
		List<String> allCategoryList = databaseQueryService.buildAllCategoryList()
		int maxCategorySize = commonReportingService.maxTextSize(allCategoryList)
		
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
		println "Years.........: $yearSet"
		println "Grand Total...: ${cf.format(grandTotal)}"
		
		reportCategoryYearlySummary(allCategoryList, yearSet, maxCategorySize, pw)
	}
	
	def reportCategoryYearlySummary(List<String> allCategoryList, Set<Integer> yearSet, int maxCategorySize, PrintWriter pw) {
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
			String querySumYear = buildQuerySumYear(year)
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
	
	String buildQuerySumYear(int year) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT BETWEEN '${year}-01-01' AND '${year}-12-31'")
		
		return sb.toString()
	}
	
}
