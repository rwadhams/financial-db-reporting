package com.wadhams.financials.db.app

import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.CategoryTrendingReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.service.CategoryListService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.time.LocalDate

class FinancialReportingSQLApp {
		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
	static main(args) {
		println 'FinancialReportingSQLApp started...'
		println ''

		FinancialReportingSQLApp app = new FinancialReportingSQLApp()
		app.execute()

		println 'FinancialReportingSQLApp ended.'
	}
	
	def execute() {
//		String query = "select max(transaction_dt) as MD from financial"
		String query = "select max(transaction_dt) as MD from financial where transaction_dt <= '${LocalDate.now().format(h2DTF)}'"
		println query
		println ''

		GroovyRowResult grr = sql.firstRow(query)
		def maxDate = grr.getProperty('MD')
		println maxDate
		
		println ''
	}
}
