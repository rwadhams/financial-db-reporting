package com.wadhams.financials.db.controller

import com.wadhams.financials.db.report.AssetDetailReportService
import com.wadhams.financials.db.report.CategoryByMonthReportService
import com.wadhams.financials.db.report.CategoryDetailReportService
import com.wadhams.financials.db.report.CategoryTotalReportService
import com.wadhams.financials.db.report.FurnitureReportService
import com.wadhams.financials.db.report.LargeTransactionReportService
import com.wadhams.financials.db.report.MajorEquipmentReportService
import com.wadhams.financials.db.report.MonthlyCategoryDetailReportService
import com.wadhams.financials.db.report.MonthlyRunningCostReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.report.RenovationReportService
import com.wadhams.financials.db.report.SmallMediumLargeReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService
import com.wadhams.financials.db.report.Last365DaysReportService

class FinancialReportingController {
	CategoryListService categoryListService = new CategoryListService()
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateService dateService = new DateService()
	
	def execute(PrintWriter pw) {
		//report headings
		pw.println 'Financial Report'
		pw.println '================'
		pw.println ''
		
		CategoryByMonthReportService categoryByMonthReportService = new CategoryByMonthReportService()
		//inject services
		categoryByMonthReportService.categoryListService = categoryListService
		categoryByMonthReportService.commonReportingService = commonReportingService
		categoryByMonthReportService.databaseQueryService = databaseQueryService
		categoryByMonthReportService.dateService = dateService
		//execute
		categoryByMonthReportService.execute(pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		MonthlyCategoryDetailReportService monthlyCategoryDetailReportService = new MonthlyCategoryDetailReportService()
		//inject services
		monthlyCategoryDetailReportService.categoryListService = categoryListService
		monthlyCategoryDetailReportService.commonReportingService = commonReportingService
		monthlyCategoryDetailReportService.databaseQueryService = databaseQueryService
		monthlyCategoryDetailReportService.dateService = dateService
		//execute
		monthlyCategoryDetailReportService.execute(dateService.latestYearMonth, pw)

	}
}
