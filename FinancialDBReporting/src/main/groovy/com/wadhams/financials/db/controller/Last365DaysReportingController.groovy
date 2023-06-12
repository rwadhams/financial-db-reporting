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

class Last365DaysReportingController {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	
	def execute(PrintWriter pw) {
		Last365DaysReportService last365DaysReportService = new Last365DaysReportService()
		//inject services
		last365DaysReportService.commonReportingService = commonReportingService
		last365DaysReportService.databaseQueryService = databaseQueryService
		//execute
		last365DaysReportService.execute(pw)
	}
}
