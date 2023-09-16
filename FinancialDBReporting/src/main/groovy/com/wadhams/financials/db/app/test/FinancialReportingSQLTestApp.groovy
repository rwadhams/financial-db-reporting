package com.wadhams.financials.db.app.test

import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.CategoryTrendingReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.service.CategoryListService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.time.LocalDate

class FinancialReportingSQLTestApp {
		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
	static main(args) {
		println 'FinancialReportingSQLTestApp started...'
		println ''

		FinancialReportingSQLTestApp app = new FinancialReportingSQLTestApp()
		app.execute()
		app.execute2()
		
		println 'FinancialReportingSQLTestApp ended.'
	}
	
	def execute() {
//		String query = "select max(transaction_dt) as MD from financial"
		String query = "select max(transaction_dt) as MD from financial where transaction_dt <= '${LocalDate.now().format(h2DTF)}'"
		println query

		GroovyRowResult grr = sql.firstRow(query)
		def maxDate = grr.getProperty('MD')
		println maxDate
		
		println ''
	}
	
	def execute2() {
		String query = "select count(*) TOTAL from financial where category is null"
		println query

		GroovyRowResult grr = sql.firstRow(query)
		def total = grr.getProperty('TOTAL')
		println total
		
		println ''
	}
}
