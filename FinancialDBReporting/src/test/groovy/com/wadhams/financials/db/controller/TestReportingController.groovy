package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.TotalsReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.TimelineService

class TestReportingController {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CategoryListService categoryListService = new CategoryListService()
	TimelineService timelineService = new TimelineService()
	
	def execute() {
		TotalsReportService service = new TotalsReportService()
		//inject services
		service.commonReportingService = commonReportingService
		service.databaseQueryService = databaseQueryService
		service.categoryListService = categoryListService
		//execute
		service.execute()
	}
}
