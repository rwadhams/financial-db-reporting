package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.BudgetReportService
import com.wadhams.financials.db.report.TrendingReportService

class TrendingReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Trending Report'
		pw.println '==============='
		pw.println ''
		
		TrendingReportService trendingReportService = new TrendingReportService()
		trendingReportService.execute(pw)
	}
}
