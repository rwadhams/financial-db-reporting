package com.wadhams.financials.db.controller

import com.wadhams.financials.db.service.LargeTransactionReportService
import com.wadhams.financials.db.service.RenovationReportService

class FinancialReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
		largeTransactionReportService.execute(pw)
		
		RenovationReportService renovationReportService = new RenovationReportService()
		renovationReportService.execute(pw)
		
	}
}
