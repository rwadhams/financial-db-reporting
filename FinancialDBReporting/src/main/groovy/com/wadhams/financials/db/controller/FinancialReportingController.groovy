package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.CategoryByMonthReportService
import com.wadhams.financials.db.report.FurnitureReportService
import com.wadhams.financials.db.report.LargeTransactionReportService
import com.wadhams.financials.db.report.MajorEquipmentReportService
import com.wadhams.financials.db.report.MonthlyRunningCostReportService
import com.wadhams.financials.db.report.RenovationReportService

class FinancialReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
		largeTransactionReportService.execute(pw)
		
		MonthlyRunningCostReportService monthlyRunningCostReportService = new MonthlyRunningCostReportService()
		monthlyRunningCostReportService.execute(pw)
		
		CategoryByMonthReportService categoryByMonthReportService = new CategoryByMonthReportService()
		categoryByMonthReportService.execute(pw)
		
		
		MajorEquipmentReportService majorEquipmentReportService = new MajorEquipmentReportService()
		majorEquipmentReportService.execute(pw)
		

		RenovationReportService renovationReportService = new RenovationReportService()
		renovationReportService.execute(pw)
		
		FurnitureReportService furnitureReportService = new FurnitureReportService()
		furnitureReportService.execute(pw)
		
	}
}
