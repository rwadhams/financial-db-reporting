package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.MonthlyAverageCampingCostReportService
import com.wadhams.financials.db.type.Run

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

		PrintWriter pw = new PrintWriter(System.out, true)
		
		MonthlyAverageCampingCostReportService monthlyAverageCampingCostReportService = new MonthlyAverageCampingCostReportService()
		monthlyAverageCampingCostReportService.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
