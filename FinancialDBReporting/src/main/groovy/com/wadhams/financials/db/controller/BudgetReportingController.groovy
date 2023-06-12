package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.BudgetReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService

class BudgetReportingController {
	CategoryListService categoryListService = new CategoryListService()
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateService dateService = new DateService()
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Budget Report'
		pw.println '============='
		pw.println ''
		
		BudgetReportService budgetReportService = new BudgetReportService()
		//inject services
		budgetReportService.categoryListService = categoryListService
		budgetReportService.commonReportingService = commonReportingService
		budgetReportService.databaseQueryService = databaseQueryService
		budgetReportService.dateService = dateService
		//execute
		budgetReportService.execute(pw)
	}
}
