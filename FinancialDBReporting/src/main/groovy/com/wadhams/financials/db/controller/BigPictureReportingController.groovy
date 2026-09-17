package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.BigPictureSummaryReportService
import com.wadhams.financials.db.report.CategoryByMonthReportService
import com.wadhams.financials.db.report.MonthlyCategoryDetailReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService
import com.wadhams.financials.db.service.TimelineService

class BigPictureReportingController {
	CategoryListService categoryListService = new CategoryListService()
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateService dateService = new DateService()
	
	def execute(PrintWriter pw) {
		BigPictureSummaryReportService bigPictureSummaryReportService = new BigPictureSummaryReportService()
		//inject services
		bigPictureSummaryReportService.databaseQueryService = databaseQueryService
		bigPictureSummaryReportService.categoryListService = categoryListService
		bigPictureSummaryReportService.commonReportingService = commonReportingService
		bigPictureSummaryReportService.dateService = dateService
		//execute
		bigPictureSummaryReportService.execute(pw)
	}
}
