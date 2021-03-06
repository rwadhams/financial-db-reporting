package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.AssetDetailReportService
import com.wadhams.financials.db.report.CategoryByMonthReportService
import com.wadhams.financials.db.report.CategoryDetailReportService
import com.wadhams.financials.db.report.CategoryTotalReportService
import com.wadhams.financials.db.report.FurnitureReportService
import com.wadhams.financials.db.report.LargeTransactionReportService
import com.wadhams.financials.db.report.MajorEquipmentReportService
import com.wadhams.financials.db.report.MonthlyAverageCampingCostReportService
import com.wadhams.financials.db.report.MonthlyRunningCostReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.report.RenovationReportService
import com.wadhams.financials.db.report.SmallMediumLargeReportService
import com.wadhams.financials.db.report.Last365DaysReportService

class FinancialReportingController {
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
		Last365DaysReportService last365DaysReportService = new Last365DaysReportService()
		last365DaysReportService.execute(pw)
		
		SmallMediumLargeReportService smallMediumLargeReportService = new SmallMediumLargeReportService()
		smallMediumLargeReportService.execute(pw)

		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
		largeTransactionReportService.execute(pw)
		
		CategoryByMonthReportService categoryByMonthReportService = new CategoryByMonthReportService()
		categoryByMonthReportService.execute(pw)
		
		CategoryTotalReportService categoryTotalReportService = new CategoryTotalReportService()
		categoryTotalReportService.execute(pw)

		//Extra reports
		CategoryDetailReportService categoryDetailReportService = new CategoryDetailReportService()
		categoryDetailReportService.execute()

		AssetDetailReportService assetDetailReportService = new AssetDetailReportService()
		assetDetailReportService.execute()
		
//		No longer required.
//		MonthlyRunningCostReportService monthlyRunningCostReportService = new MonthlyRunningCostReportService()
//		monthlyRunningCostReportService.execute(pw)
				
//		MonthlyAverageCampingCostReportService monthlyAverageCampingCostReportService = new MonthlyAverageCampingCostReportService()
//		monthlyAverageCampingCostReportService.execute(pw)
		
//		MonthlyTotalsReportService monthlyTotalsReportService = new MonthlyTotalsReportService()
//		monthlyTotalsReportService.execute(pw)
		
//		MajorEquipmentReportService majorEquipmentReportService = new MajorEquipmentReportService()
//		majorEquipmentReportService.execute(pw)

//		RenovationReportService renovationReportService = new RenovationReportService()
//		renovationReportService.execute(pw)
		
//		FurnitureReportService furnitureReportService = new FurnitureReportService()
//		furnitureReportService.execute(pw)
	}
}
