package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService

import groovy.sql.GroovyRowResult

class CategoryByMonthReportService {
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	DatabaseQueryService databaseQueryService
	DateService dateService
	
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	DateTimeFormatter reportDTF = DateTimeFormatter.ofPattern("LLLyyyy")

	def execute(PrintWriter pw) {
		pw.println 'CATEGORY EXPENSE TOTALS BY MONTH REPORT'
		pw.println '---------------------------------------'

//		List<String> categoryList = databaseQueryService.buildPreviousYearPopularCategoryList()
		List<String> categoryList = categoryListService.dayToDayCategoryList
		//println categoryList
		
		int maxCategorySize = 0
		categoryList.each {cat ->
			if (cat.size()>maxCategorySize) {
				maxCategorySize = cat.size()
			}
		}
		//println "maxCategorySize: $maxCategorySize"
		maxCategorySize = maxCategorySize + 5	//add margin for report

		//previous 12 YearMonth
		List<YearMonth> previous12List = dateService.previous12YearMonthList

		NumberFormat nf = NumberFormat.getCurrencyInstance()
		int maxAmountSize = 12	//used for padding amount
		
		//Print heading
		pw.print 'Categories'.padRight(maxCategorySize)
		previous12List.each {ym ->
			pw.print ym.format(reportDTF).padLeft(maxAmountSize)
		}
		pw.println '  Categories'

		categoryList.each {cat ->
			//println cat
			pw.print cat.padRight(maxCategorySize)
			previous12List.each {ym ->
				String query = buildCategoryMonthQuery(cat, ym.atDay(1), ym.atEndOfMonth())
				//println query
				GroovyRowResult grr = databaseQueryService.firstRow(query)
				def amount = grr.getProperty('AMT')
				//println "Amount: $amount"
				if (amount) {
					String formattedAmount = nf.format(amount)
					pw.print formattedAmount.padLeft(maxAmountSize)
				}
				else {
					pw.print '$0.00'.padLeft(maxAmountSize)
				}
			}
			pw.println "  $cat"
		}
	}
	
	//TODO: refactor to common Category enum
	String buildCategoryMonthQuery(String category, LocalDate firstDate, LocalDate lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '")
		sb.append(category)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(h2DTF.format(firstDate))
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(h2DTF.format(lastDate))
		sb.append("'")

		return sb.toString()
	}
	
}
