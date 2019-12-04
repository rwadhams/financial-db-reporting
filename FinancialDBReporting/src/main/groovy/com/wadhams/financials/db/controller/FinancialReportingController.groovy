package com.wadhams.financials.db.controller

import com.wadhams.financials.db.service.CategoryByMonthReportService
import com.wadhams.financials.db.service.FurnitureReportService
import com.wadhams.financials.db.service.LargeTransactionReportService
import com.wadhams.financials.db.service.MonthlyRunningCostReportService
import com.wadhams.financials.db.service.RenovationReportService

class FinancialReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
//		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
//		largeTransactionReportService.execute(pw)
		
//		MonthlyRunningCostReportService monthlyRunningCostReportService = new MonthlyRunningCostReportService()
//		monthlyRunningCostReportService.execute(pw)
		
		CategoryByMonthReportService categoryByMonthReportService = new CategoryByMonthReportService()
		categoryByMonthReportService.execute(pw)
		
//		RenovationReportService renovationReportService = new RenovationReportService()
//		renovationReportService.execute(pw)
		
//		FurnitureReportService furnitureReportService = new FurnitureReportService()
//		furnitureReportService.execute(pw)
		
	}
}
