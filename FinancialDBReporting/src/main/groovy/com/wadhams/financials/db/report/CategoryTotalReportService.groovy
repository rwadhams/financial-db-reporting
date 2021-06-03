package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class CategoryTotalReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		//Report by Category
		pw.println 'CATEGORY TOTALS REPORT'
		pw.println '----------------------'
		
		String query = 'SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT FROM FINANCIAL GROUP BY CATEGORY ORDER BY CATEGORY'
		println query
		println ''
		
		List<TotalDTO> totalList = databaseQueryService.buildTotalsList(query)
		
		report(totalList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//Report by Amount 
		pw.println 'CATEGORY TOTALS REPORT (Largest to Smallest amount)'
		pw.println '---------------------------------------------------'
		
		String query2 = 'SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT FROM FINANCIAL GROUP BY CATEGORY ORDER BY 2 DESC'
		println query2
		println ''
		
		List<TotalDTO> totalList2 = databaseQueryService.buildTotalsList(query2)
		
		report(totalList2, pw)
		
		
		pw.println ''
		pw.println '\t(See \'category-detail-report.txt\' for specific details)'
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def report(List<TotalDTO> totalList, PrintWriter pw) {
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		BigDecimal grandTotal = new BigDecimal(0.0)
		
		totalList.each {dto ->
			grandTotal = grandTotal.add(dto.amount)
			String col1 = dto.totalName.padRight(21, ' ')
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			pw.println "$col1$col2"
		}
		pw.println ''
		pw.println "Grand Total: ${nf.format(grandTotal)}"

	}
	
}
