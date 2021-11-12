package com.wadhams.financials.db.app

import com.wadhams.financials.db.report.BudgetReportService

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

		PrintWriter pw = new PrintWriter(System.out, true)
		
		BudgetReportService budgetReportService = new BudgetReportService()
		budgetReportService.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
