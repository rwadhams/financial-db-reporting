package com.wadhams.financials.db.app

import com.wadhams.financials.db.report.BudgetReportService
import com.wadhams.financials.db.report.TrendingReportService

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

		PrintWriter pw = new PrintWriter(System.out, true)
		
		TrendingReportService service = new TrendingReportService()
		service.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
