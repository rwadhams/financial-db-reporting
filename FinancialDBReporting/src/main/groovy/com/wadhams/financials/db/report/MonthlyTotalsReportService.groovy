package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

import groovy.sql.GroovyRowResult

class MonthlyTotalsReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	DateTimeFormatter reportDTF = DateTimeFormatter.ofPattern("LLLyyyy")
	
	def execute(PrintWriter pw) {
		String largeAmount = '400'
		String heading = "MONTHLY SMALL TOTALS REPORT (Transactions not exceeeding \$$largeAmount)" 
		pw.println heading
		pw.println ''.padLeft(heading.size(), '-')

		//previous 12 YearMonth
		YearMonth now = YearMonth.now()
		List<YearMonth> previous12List = []
		12.times {i ->
			previous12List << now.minusMonths(i+1)
		}
		previous12List.sort()
		
		BigDecimal total = new BigDecimal(0.0)
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		previous12List.each {ym ->
			String query = buildMonthlyTotalsQuery(largeAmount, ym.atDay(1), ym.atEndOfMonth())
			//println query
			GroovyRowResult grr = databaseQueryService.firstRow(query)
			def amount = grr.getProperty('AMT')
			//println "Amount: $amount"
			if (amount) {
				total = total.add(amount)
				String formattedAmount = nf.format(amount)
				pw.println "${ym.format(reportDTF).padRight(12, '.')}: ${formattedAmount.padLeft(11)}"
			}
		}
		pw.println ''
		String formattedTotal = nf.format(total)
		pw.println "${'Total'.padRight(12, '.')}: ${formattedTotal.padLeft(11)}"
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	//TODO: refactor to common Category enum
	String buildMonthlyTotalsQuery(String largeAmount, LocalDate firstDate, LocalDate lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT <= $largeAmount ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(h2DTF.format(firstDate))
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(h2DTF.format(lastDate))
		sb.append("'")

		return sb.toString()
	}
	
}
