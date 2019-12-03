package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO

class LargeTransactionReportService {
	String largeAmount = '400'
	
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
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		pw.println 'LARGE TRANSACTION REPORT'
		pw.println '------------------------'

		financialList.each {dto ->
			total = total.add(dto.amount)
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.payee.padRight(maxPayeeSize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "${sdf.format(dto.transactionDt)}  $col2  $col3  $col4"
		}
		
		pw.println ''
		pw.println "Large Transaction Total: ${nf.format(total)}"
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	String buildQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT > ")
		sb.append(largeAmount)
		sb.append(" ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
