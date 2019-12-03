package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.helper.ListControlBreak

class MonthlyRunningCostReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String query = buildQuery()
		println query
		println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		report(financialList, pw)
	}
	
	def report(List<FinancialDTO> financialList, PrintWriter pw) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()

		Date today = new Date()
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')		
		
		ListControlBreak cb = new ListControlBreak(financialList)
		FinancialDTO current = cb.first()
		
		while (cb.hasMore()) {
			String savedRG1 = current.rg1
			//Heading 1
			pw.println "$savedRG1"
			int length = savedRG1.size()
			pw.println "-".padRight(length, '-')
			
			//Total
			BigDecimal reportTotal = BigDecimal.ZERO
			
			while (cb.hasMore() && savedRG1 == current.rg1) {
				String savedAsset = current.asset
				pw.println "$savedAsset:"
				while (cb.hasMore() && savedRG1 == current.rg1 && savedAsset == current.asset) {
					println savedAsset
					String savedCategory = current.category
					BigDecimal categoryTotal = BigDecimal.ZERO
					BigDecimal categoryDays = BigDecimal.ZERO
					while (cb.hasMore() && savedRG1 == current.rg1 && savedAsset == current.asset && savedCategory == current.category) {
						println savedCategory
						categoryTotal = categoryTotal.add(current.amount)
						int days = current.endDt - current.startDt + 1
						categoryDays = categoryDays.add(days)
						current = cb.next()
					}
					println "$categoryTotal\t$categoryDays"
					BigDecimal categoryAverage = categoryTotal.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
					pw.println "\t$savedCategory ${nf.format(categoryAverage)}"
					reportTotal = reportTotal.add(categoryAverage)
				}
				pw.println ''
			}
			pw.println "Total: ${nf.format(reportTotal)}"
			pw.println ''
		}
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	String buildQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 IS NOT NULL ")
		sb.append("ORDER BY RPT_GRP_1, ASSET, CATEGORY, SUB_CATEGORY")
		
		return sb.toString()
	}
	
}
