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
		
		pw.println 'MONTHLY RUNNING COST REPORT'
		pw.println '---------------------------'

		BigDecimal reportTotal = BigDecimal.ZERO
		
		ListControlBreak cb = new ListControlBreak(financialList)
		FinancialDTO current = cb.first()
		
		while (cb.hasMore()) {
			String savedAsset = current.asset
			pw.println "$savedAsset:"
			while (cb.hasMore() && savedAsset == current.asset) {
				println savedAsset
				String savedCategory = current.category
				BigDecimal categoryTotal = BigDecimal.ZERO
				BigDecimal categoryDays = BigDecimal.ZERO
				Date startDate = today + 3650
				Date endDate = today - 3650
				while (cb.hasMore() && savedAsset == current.asset && savedCategory == current.category) {
					println savedCategory
					categoryTotal = categoryTotal.add(current.amount)
					if (current.startDt.before(startDate)) {
						startDate = current.startDt
					}
					if (current.endDt.after(endDate)) {
						endDate = current.endDt
					}
					current = cb.next()
				}
				int days = endDate - startDate + 1
				categoryDays = categoryDays.add(days)
				println "$categoryTotal\t$categoryDays"
				BigDecimal categoryAverage = categoryTotal.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
				pw.println "\t$savedCategory ${nf.format(categoryAverage)}"
				reportTotal = reportTotal.add(categoryAverage)
			}
			pw.println ''
		}
		pw.println "Total: ${nf.format(reportTotal)}"
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	String buildQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("ORDER BY ASSET, CATEGORY, SUB_CATEGORY, TRANSACTION_DT")
		
		return sb.toString()
	}
	
}
