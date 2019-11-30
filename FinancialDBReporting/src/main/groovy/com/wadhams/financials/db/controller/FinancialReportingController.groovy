package com.wadhams.financials.db.controller

import com.wadhams.financials.db.service.MajorPurchaseReportService

class FinancialReportingController {
	
	def execute(PrintWriter pw) {
		println 'execute() invoked'
		println ''
		
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
//		RenovationReportService renovationReportService = new RenovationReportService()
//		renovationReportService.reportSummary(pw)
//		renovationReportService.reportDetail()
		
		MajorPurchaseReportService majorPurchaseReportService = new MajorPurchaseReportService()
		majorPurchaseReportService.execute(pw)
		
//		MajorExpenseReportService majorExpenseReportService = new MajorExpenseReportService()
//		majorExpenseReportService.report(pw)
		
	}
}
