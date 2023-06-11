package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.AssetDetailReportService
import com.wadhams.financials.db.report.CategoryByMonthReportService
import com.wadhams.financials.db.report.CategoryDetailReportService
import com.wadhams.financials.db.report.CategoryTotalReportService
import com.wadhams.financials.db.report.DataValueReportingService
import com.wadhams.financials.db.report.FurnitureReportService
import com.wadhams.financials.db.report.LargeTransactionReportService
import com.wadhams.financials.db.report.MajorEquipmentReportService
import com.wadhams.financials.db.report.MonthlyAverageCampingCostReportService
import com.wadhams.financials.db.report.MonthlyRunningCostReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.report.RenovationReportService
import com.wadhams.financials.db.report.SmallMediumLargeReportService
import com.wadhams.financials.db.report.Last365DaysReportService

class ExtraReportingController {
	
	def execute() {
		//Extra reports
		CategoryDetailReportService categoryDetailReportService = new CategoryDetailReportService()
		categoryDetailReportService.execute()

		AssetDetailReportService assetDetailReportService = new AssetDetailReportService()
		assetDetailReportService.execute()
		
		DataValueReportingService dataValueReportingService = new DataValueReportingService()
		dataValueReportingService.execute()
	}
}
