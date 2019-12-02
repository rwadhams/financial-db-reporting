package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO

class DatabaseQueryService {
	List<FinancialDTO> buildList(String query) {
		List<FinancialDTO> financialList = []

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
	
}
