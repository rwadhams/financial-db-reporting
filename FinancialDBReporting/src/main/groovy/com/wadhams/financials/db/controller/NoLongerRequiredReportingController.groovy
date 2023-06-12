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
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.report.Last365DaysReportService

class NoLongerRequiredReportingController {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()

	def execute(PrintWriter pw) {
		//report headings
		pw.println 'No Longer Required Reports'
		pw.println '=========================='
		pw.println ''
		
		SmallMediumLargeReportService smallMediumLargeReportService = new SmallMediumLargeReportService()
		//inject services
		smallMediumLargeReportService.commonReportingService = commonReportingService
		smallMediumLargeReportService.databaseQueryService = databaseQueryService
		//execute
		smallMediumLargeReportService.execute(pw)

		LargeTransactionReportService largeTransactionReportService = new LargeTransactionReportService()
		//inject services
		largeTransactionReportService.commonReportingService = commonReportingService
		largeTransactionReportService.databaseQueryService = databaseQueryService
		//execute
		largeTransactionReportService.execute(pw)
		
		CategoryTotalReportService categoryTotalReportService = new CategoryTotalReportService()
		//inject services
		categoryTotalReportService.commonReportingService = commonReportingService
		categoryTotalReportService.databaseQueryService = databaseQueryService
		//execute
		categoryTotalReportService.execute(pw)

		MonthlyRunningCostReportService monthlyRunningCostReportService = new MonthlyRunningCostReportService()
		//inject services
		monthlyRunningCostReportService.commonReportingService = commonReportingService
		monthlyRunningCostReportService.databaseQueryService = databaseQueryService
		//execute
		monthlyRunningCostReportService.execute(pw)
				
		MonthlyAverageCampingCostReportService monthlyAverageCampingCostReportService = new MonthlyAverageCampingCostReportService()
		//inject services
		monthlyAverageCampingCostReportService.commonReportingService = commonReportingService
		monthlyAverageCampingCostReportService.databaseQueryService = databaseQueryService
		//execute
		monthlyAverageCampingCostReportService.execute(pw)
		
		MonthlyTotalsReportService monthlyTotalsReportService = new MonthlyTotalsReportService()
		//inject services
		monthlyTotalsReportService.commonReportingService = commonReportingService
		monthlyTotalsReportService.databaseQueryService = databaseQueryService
		//execute
		monthlyTotalsReportService.execute(pw)
		
		MajorEquipmentReportService majorEquipmentReportService = new MajorEquipmentReportService()
		//inject services
		majorEquipmentReportService.commonReportingService = commonReportingService
		majorEquipmentReportService.databaseQueryService = databaseQueryService
		//execute
		majorEquipmentReportService.execute(pw)

		RenovationReportService renovationReportService = new RenovationReportService()
		//inject services
		renovationReportService.commonReportingService = commonReportingService
		renovationReportService.databaseQueryService = databaseQueryService
		//execute
		renovationReportService.execute(pw)
		
		FurnitureReportService furnitureReportService = new FurnitureReportService()
		//inject services
		furnitureReportService.commonReportingService = commonReportingService
		furnitureReportService.databaseQueryService = databaseQueryService
		//execute
		furnitureReportService.execute(pw)
	}
}
