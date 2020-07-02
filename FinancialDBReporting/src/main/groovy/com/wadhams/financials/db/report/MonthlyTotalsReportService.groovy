package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.type.MonthDateRange

class MonthlyTotalsReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		pw.println 'MONTHLY TOTALS REPORT (excluding: Large purchases)'
		pw.println '--------------------------------------------------'

		//TODO: dynamic month range
		List<MonthDateRange> mdrList = [MonthDateRange.Sept2019, MonthDateRange.Oct2019, MonthDateRange.Nov2019, MonthDateRange.Dec2019, MonthDateRange.Jan2020, MonthDateRange.Feb2020, MonthDateRange.Mar2020, MonthDateRange.Apr2020, MonthDateRange.May2020]
		
		BigDecimal total = new BigDecimal(0.0)
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		mdrList.each {mdr ->
			String query = buildMonthlyTotalsQuery(mdr.firstDate, mdr.lastDate)
			//println query
			GroovyRowResult grr = databaseQueryService.firstRow(query)
			def amount = grr.getProperty('AMT')
			//println "Amount: $amount"
			if (amount) {
				total = total.add(amount)
				String formattedAmount = nf.format(amount)
				pw.println "${mdr.name().padRight(12, '.')}: ${formattedAmount.padLeft(11)}"
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
	String buildMonthlyTotalsQuery(String firstDate, String lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY NOT IN ('PURCHASE') ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(firstDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(lastDate)
		sb.append("'")

		return sb.toString()
	}
	
}
