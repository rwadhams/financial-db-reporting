package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class LargeTransactionReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String query = buildQuery('400')
		println query
		println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		
		report(financialList, pw)
	}
	
	def report(List<FinancialDTO> financialList, PrintWriter pw) {
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal expenseTotal = new BigDecimal(0.0)
		BigDecimal incomeTotal = new BigDecimal(0.0)
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		pw.println 'LARGE TRANSACTION REPORT'
		pw.println '------------------------'

		financialList.each {dto ->
			if (dto.amount > 0.00) {
				expenseTotal = expenseTotal.add(dto.amount)
			}
			else {
				incomeTotal = incomeTotal.subtract(dto.amount)
			}
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.payee.padRight(maxPayeeSize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "${sdf.format(dto.transactionDt)}  $col2  $col3  $col4"
		}
		
		pw.println ''
		pw.println "Large Expense Transaction Total: ${nf.format(expenseTotal)}"
		pw.println "Large Income  Transaction Total: ${nf.format(incomeTotal)}"
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	String buildQuery(String largeAmount) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE ABS(AMOUNT) > ")
		sb.append(largeAmount)
		sb.append(" ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
