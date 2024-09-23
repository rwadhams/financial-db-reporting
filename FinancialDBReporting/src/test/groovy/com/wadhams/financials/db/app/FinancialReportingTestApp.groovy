package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.CategoryTrendingReportingController

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

		PrintWriter pw = new PrintWriter(System.out, true)
		CategoryTrendingReportingController controller = new CategoryTrendingReportingController()
		controller.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
