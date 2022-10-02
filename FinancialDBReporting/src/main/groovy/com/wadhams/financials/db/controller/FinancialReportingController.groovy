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
	
	def execute(PrintWriter pw1, PrintWriter pw2) {
		//report headings
		pw1.println 'Financial Report'
		pw1.println '================'
		pw1.println ''
		
		Last365DaysReportService last365DaysReportService = new Last365DaysReportService()
		last365DaysReportService.execute(pw1)
		
		SmallMediumLargeReportService smallMediumLargeReportService = new SmallMediumLargeReportService()
		smallMediumLargeReportService.execute(pw1)

		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
		largeTransactionReportService.execute(pw1)
		
		CategoryByMonthReportService categoryByMonthReportService = new CategoryByMonthReportService()
		categoryByMonthReportService.execute(pw1)
		
		CategoryTotalReportService categoryTotalReportService = new CategoryTotalReportService()
		categoryTotalReportService.execute(pw1)

		//Extra reports
		CategoryDetailReportService categoryDetailReportService = new CategoryDetailReportService()
		categoryDetailReportService.execute()

		AssetDetailReportService assetDetailReportService = new AssetDetailReportService()
		assetDetailReportService.execute()
		
//		No longer required.
		pw2.println 'No Longer Required Reports'
		pw2.println '=========================='
		pw2.println ''
		
		MonthlyRunningCostReportService monthlyRunningCostReportService = new MonthlyRunningCostReportService()
		monthlyRunningCostReportService.execute(pw2)
				
		MonthlyAverageCampingCostReportService monthlyAverageCampingCostReportService = new MonthlyAverageCampingCostReportService()
		monthlyAverageCampingCostReportService.execute(pw2)
		
		MonthlyTotalsReportService monthlyTotalsReportService = new MonthlyTotalsReportService()
		monthlyTotalsReportService.execute(pw2)
		
		MajorEquipmentReportService majorEquipmentReportService = new MajorEquipmentReportService()
		majorEquipmentReportService.execute(pw2)

		RenovationReportService renovationReportService = new RenovationReportService()
		renovationReportService.execute(pw2)
		
		FurnitureReportService furnitureReportService = new FurnitureReportService()
		furnitureReportService.execute(pw2)
	}
}
