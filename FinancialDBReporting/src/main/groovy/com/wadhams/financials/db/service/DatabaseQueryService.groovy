package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.TotalDTO

class DatabaseQueryService {
	Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
	
	DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	
	List<FinancialDTO> buildList(String query) {
		List<FinancialDTO> financialList = []

		sql.eachRow(query) {row ->
			Date c01 = row.TXN
			LocalDate d01 = LocalDate.parse(c01.toString(), h2dtf)
			BigDecimal c02 = row.AMT
			String c03 = row.PAYEE
			String c04 = row.DESC
			String c05 = row.ASSET
			String c06 = row.CAT
			String c07 = row.SUBCAT
			Date c08 = row.START
			LocalDate d08 = (c08) ? LocalDate.parse(c08.toString(), h2dtf) : null     
			Date c09 = row.END
			LocalDate d09 = (c09) ? LocalDate.parse(c09.toString(), h2dtf) : null
			String c10 = row.RG1
			String c11 = row.RG2
			String c12 = row.RG3
			//println "$d01\t$c02\t$c03\t$c04\t$c05\t$c06\t$c07\t$d08\t$d09\t$c10\t$c11\t$c12"
			//println ''
			FinancialDTO dto = new FinancialDTO(transactionDt : d01, amount : c02, payee : c03, description : c04, asset : c05, category : c06, subCategory : c07, startDt: d08, endDt : d09, rg1 : c10, rg2 : c11, rg3 : c12)
			//println dto
			//println ''
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

	List<String> buildAllAssetList() {
		List<String> assetList = []
		
		String query = 'SELECT DISTINCT ASSET as ASSET FROM FINANCIAL WHERE ASSET IS NOT NULL ORDER BY ASSET'
		
		sql.eachRow(query) {row ->
			String asset = row.ASSET
			assetList << asset
		}
		
		return assetList
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

	List<String> buildPreviousYearPopularCategoryList() {
		List<String> categoryList = []
		
		YearMonth now = YearMonth.now()
		YearMonth res1 = now.minusYears(1L)	//1 year
		YearMonth res2 = res1.minusMonths(1L)	//1 month
		String dte = res2.atEndOfMonth().toString()
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT DISTINCT CATEGORY as CAT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT > '")
		sb.append(dte)
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("HAVING COUNT(*) > 2 ")
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
	
	String buildFormattedList(List<String> list) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("'${list[0]}'")
		list[1..-1].each {s ->
			sb.append(", '$s'")
		}
		
		return sb.toString()
	}

}
