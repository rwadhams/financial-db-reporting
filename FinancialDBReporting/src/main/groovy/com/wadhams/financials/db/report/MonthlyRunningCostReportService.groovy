package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.helper.ListControlBreak
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class MonthlyRunningCostReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String query
		List<FinancialDTO> financialList
		
		//query = buildQuery('SPECIFIC_RUNNING_COST')
		query = buildSpecificRunningCostsQuery()
		println query
		println ''
		financialList = databaseQueryService.buildList(query)
		reportSpecificRunningCosts(financialList, pw)
		
		//query = buildQuery('ONGOING_RUNNING_COST')
		query = buildOngoingRunningCostsQuery()
		println query
		println ''
		financialList = databaseQueryService.buildList(query)
		reportOngoingRunningCosts(financialList, pw)
		
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def reportSpecificRunningCosts(List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println 'SPECIFIC RUNNING COSTS (MONTHLY AVERAGE)'
		pw.println '----------------------------------------'
			
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
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
		
		pw.println "Monthly Average: ${nf.format(reportTotal)}"
		3.times {
			pw.println ''
		}
	}
	
	def reportOngoingRunningCosts(List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println 'ONGOING RUNNING COSTS (MONTHLY AVERAGE)'
		pw.println '---------------------------------------'
			
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
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
		pw.println "Monthly Average: ${nf.format(reportTotal)}"
		pw.println ''
	}
	
//	String buildQuery(String rg1) {
//		StringBuilder sb = new StringBuilder()
//		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
//		sb.append("FROM FINANCIAL ")
//		sb.append("WHERE START_DT IS NOT NULL ")
//		sb.append("AND RPT_GRP_1 = '")
//		sb.append(rg1)
//		sb.append("' ")
//		sb.append("ORDER BY CATEGORY")
//		
//		return sb.toString()
//	}
	
	String buildSpecificRunningCostsQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 = 'SPECIFIC_RUNNING_COST' ")
		sb.append("AND CATEGORY <> 'HOUSE_INSURANCE' ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}
	
	String buildOngoingRunningCostsQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 = 'ONGOING_RUNNING_COST' ")
		sb.append("AND CATEGORY in ('DATA_PLAN', 'FUEL', 'PHONE_PLAN') ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}
	
}
