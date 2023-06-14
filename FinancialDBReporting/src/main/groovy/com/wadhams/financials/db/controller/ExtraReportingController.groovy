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
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.TimelineService
import com.wadhams.financials.db.report.Last365DaysReportService

class ExtraReportingController {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	TimelineService timelineService = new TimelineService()
	
	def execute() {
		//Extra reports
		CategoryDetailReportService categoryDetailReportService = new CategoryDetailReportService()
		//inject services
		categoryDetailReportService.commonReportingService = commonReportingService
		categoryDetailReportService.databaseQueryService = databaseQueryService
		categoryDetailReportService.timelineService = timelineService
		
		//execute
		categoryDetailReportService.execute()

		AssetDetailReportService assetDetailReportService = new AssetDetailReportService()
		//inject services
		assetDetailReportService.commonReportingService = commonReportingService
		assetDetailReportService.databaseQueryService = databaseQueryService
		//execute
		assetDetailReportService.execute()
		
		DataValueReportingService dataValueReportingService = new DataValueReportingService()
		//execute
		dataValueReportingService.execute()
	}
}
