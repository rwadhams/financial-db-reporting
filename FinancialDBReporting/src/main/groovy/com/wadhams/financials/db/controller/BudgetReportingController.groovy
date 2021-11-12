package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.BudgetReportService

class BudgetReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Budget Report'
		pw.println '============='
		pw.println ''
		
		BudgetReportService budgetReportService = new BudgetReportService()
		budgetReportService.execute(pw)
	}
}
