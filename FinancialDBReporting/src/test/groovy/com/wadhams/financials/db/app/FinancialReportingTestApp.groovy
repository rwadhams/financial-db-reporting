package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.CategoryTrendingReportService
import com.wadhams.financials.db.report.MonthlyTotalsReportService
import com.wadhams.financials.db.service.CategoryListService

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

//		CategoryListService clService = new CategoryListService()
//		assert clService.verifyLists()
//		clService.printLists()
		
		PrintWriter pw = new PrintWriter(System.out, true)

//		FinancialReportingController controller1 = new FinancialReportingController()
//		controller1.execute(pw)

//		MonthlyTotalsReportService monthlyTotalsReportService = new MonthlyTotalsReportService()
//		monthlyTotalsReportService.execute(pw)
		
		CategoryTrendingReportService service = new CategoryTrendingReportService()
		service.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
