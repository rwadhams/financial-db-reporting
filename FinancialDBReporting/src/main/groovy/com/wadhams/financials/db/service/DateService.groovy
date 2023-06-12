package com.wadhams.financials.db.service

import com.wadhams.financials.db.dto.FinancialDTO
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DateService {
	Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	
	//static dates
	LocalDate caravanStartDate
	LocalDate campHillSaleDate
	LocalDate fingalPurchaseDate
	
	LocalDate maxTransactionDate
	YearMonth latestYearMonth
	List<YearMonth> previous12YearMonthList
	
	public DateService() {
		caravanStartDate = LocalDate.of(2020, 3, 28)
		campHillSaleDate = LocalDate.of(2020, 9, 24)
		fingalPurchaseDate = LocalDate.of(2023, 3, 2)
		
		maxTransactionDate = findMaxTransactionDate()
		latestYearMonth = YearMonth.from(maxTransactionDate)
		previous12YearMonthList = buildPrevious12YearMonthList()
	}

	LocalDate findMaxTransactionDate() {
		String query = "select max(transaction_dt) as MD from financial where transaction_dt <= '${LocalDate.now().format(h2DTF)}'"
//		println query
//		println ''

		GroovyRowResult grr = sql.firstRow(query)
		Date maxDate = grr.getProperty('MD')

		return LocalDate.parse(maxDate.toString(), h2DTF)
	}
	
	def printDates() {
		println 'Static Dates:'
		println "\tcaravanStartDate.....: $caravanStartDate"
		println "\tcampHillSaleDate.....: $campHillSaleDate"
		println "\tfingalPurchaseDate...: $fingalPurchaseDate"
		println ''
		
		println "maxTransactionDate...........: $maxTransactionDate"
		println "latestYearMonth..............: $latestYearMonth"
		println "previous12YearMonthList......: $previous12YearMonthList"
	}
	
	List<YearMonth> buildPrevious12YearMonthList() {
		YearMonth latest = YearMonth.from(maxTransactionDate)
		List<YearMonth> previous12List = []
		12.times {i ->
			previous12List << latest.minusMonths(i)
		}

		return previous12List.sort()
	}
}
