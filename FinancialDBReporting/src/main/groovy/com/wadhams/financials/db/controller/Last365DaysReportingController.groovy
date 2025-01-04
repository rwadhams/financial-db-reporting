package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.Last365DaysReportService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

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
