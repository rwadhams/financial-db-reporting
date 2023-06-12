package com.wadhams.financials.db.app.test

import com.wadhams.financials.db.controller.ExtraReportingController
import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.BudgetReportService
import com.wadhams.financials.db.report.CategoryTrendingReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.DateService

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

//		DateService dateService = new DateService()
//		dateService.printDates()
		
//		ExtraReportingController controller9 = new ExtraReportingController()
//		controller9.execute()

//		CategoryListService clService = new CategoryListService()
//		assert clService.verifyLists()
//		clService.printLists()
		
		PrintWriter pw = new PrintWriter(System.out, true)

		BudgetReportService budgetReportService = new BudgetReportService()
		budgetReportService.execute(pw)

//		FinancialReportingController controller = new FinancialReportingController()
//		controller.execute(pw)
		
//		FinancialReportingController controller1 = new FinancialReportingController()
//		controller1.execute(pw)

//		MonthlyTotalsReportService monthlyTotalsReportService = new MonthlyTotalsReportService()
//		monthlyTotalsReportService.execute(pw)
		
//		CategoryTrendingReportService service = new CategoryTrendingReportService()
//		service.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
