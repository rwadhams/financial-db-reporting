package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO

class MajorPurchaseReportService {

	def execute(PrintWriter pw) {
		List<FinancialDTO> financialList = buildList()
		
		report(financialList, pw)
	}
	
	def report(List<FinancialDTO> financialList, PrintWriter pw) {
		int maxPayeeSize = 0
		financialList.each {dto ->
			if (dto.payee.size() > maxPayeeSize) {
				maxPayeeSize = dto.payee.size()
			}
		}
		
		BigDecimal total = new BigDecimal(0.0)
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		pw.println "Major Purchase Details:"
		pw.println '-----------------------'

		financialList.each {dto ->
			total = total.add(dto.amount)
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.payee.padRight(maxPayeeSize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "${sdf.format(dto.transactionDt)}  $col2  $col3  $col4"
		}
		
		pw.println ''
		pw.println "Major Purchase Total: ${nf.format(total)}"
		pw.println ''
	}
	
	List<FinancialDTO> buildList() {
		List<FinancialDTO> financialList = []
		
		String query = buildQuery('200')
		println query
		println ''
		
		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		
		sql.eachRow(query) {row ->
			Date c01 = row.TXN
			BigDecimal c02 = row.AMT
			String c03 = row.PAYEE
			String c04 = row.DESC
			String c05 = row.ASSET
			String c06 = row.CAT
			String c07 = row.SUBCAT
			Date c08 = row.START
			Date c09 = row.END
			println "$c01\t$c02\t$c03\t$c04\t$c05\t$c06\t$c07\t$c08\t$c09"
			println ''
			FinancialDTO dto = new FinancialDTO(transactionDt : c01, amount : c02, payee : c03, description : c04, asset : c05, category : c06, subCategory : c07, startDt: c08, endDt : c09)
			println dto
			println ''
			financialList << dto
		}
		
		return financialList
	}
	
	String buildQuery(String amount) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT > ")
		sb.append(amount)
		sb.append(" AND CATEGORY = 'PURCHASE'")
		
		return sb.toString()
	}
	
}
