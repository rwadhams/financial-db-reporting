package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.BudgetReportService
import com.wadhams.financials.db.report.CategoryTrendingReportService
import com.wadhams.financials.db.report.TrendingReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService

class CategoryTrendingReportingController {
	CategoryListService categoryListService = new CategoryListService()
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateService dateService = new DateService()

	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Category Trending Report'
		pw.println '========================'
		pw.println ''
		
		CategoryTrendingReportService categoryTrendingReportService = new CategoryTrendingReportService()
		//inject services
		categoryTrendingReportService.categoryListService = categoryListService
		categoryTrendingReportService.commonReportingService = commonReportingService
		categoryTrendingReportService.databaseQueryService = databaseQueryService
		categoryTrendingReportService.dateService = dateService
		//execute
		categoryTrendingReportService.execute(pw)
	}
}
