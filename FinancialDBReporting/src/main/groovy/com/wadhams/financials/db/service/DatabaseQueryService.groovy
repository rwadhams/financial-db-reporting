package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.TotalDTO

class DatabaseQueryService {
	Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		
	List<FinancialDTO> buildList(String query) {
		List<FinancialDTO> financialList = []

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
			String c10 = row.RG1
			String c11 = row.RG2
			String c12 = row.RG3
			println "$c01\t$c02\t$c03\t$c04\t$c05\t$c06\t$c07\t$c08\t$c09\t$c10\t$c11\t$c12"
			println ''
			FinancialDTO dto = new FinancialDTO(transactionDt : c01, amount : c02, payee : c03, description : c04, asset : c05, category : c06, subCategory : c07, startDt: c08, endDt : c09, rg1 : c10, rg2 : c11, rg3 : c12)
			println dto
			println ''
			financialList << dto
		}
		
		return financialList
	}

	List<String> buildAllCategoryList() {
		List<String> categoryList = []
		
		String query = 'SELECT DISTINCT CATEGORY as CAT FROM FINANCIAL ORDER BY CATEGORY'
		
		sql.eachRow(query) {row ->
			String category = row.CAT
			categoryList << category
		}
		
		return categoryList
	}

	List<String> buildPreviousThreeMonthCategoryList() {
		List<String> categoryList = []
		
		YearMonth now = YearMonth.now()
		YearMonth res = now.minusMonths(3L)		//3 months
		String dte = res.atEndOfMonth().toString()
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT DISTINCT CATEGORY as CAT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT > '")
		sb.append(dte)
		sb.append("' ")
		sb.append("ORDER BY CATEGORY")
		
		sql.eachRow(sb.toString()) {row ->
			String category = row.CAT
			categoryList << category
		}
		
		return categoryList
	}

	List<TotalDTO> buildTotalsList(String query) {
		List<TotalDTO> totalList = []
		
		sql.eachRow(query) {row ->
			String totalName = row.TOTAL_NAME
			BigDecimal amount = row.AMT
			TotalDTO dto = new TotalDTO()
			dto.totalName = totalName
			dto.amount = amount
			totalList << dto
		}
		
		return totalList
	}

	GroovyRowResult firstRow(String query) {
		return sql.firstRow(query)
	}
	
}
