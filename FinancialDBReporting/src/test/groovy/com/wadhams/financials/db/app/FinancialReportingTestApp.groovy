package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.report.MonthlyAverageCampingCostReportService
import com.wadhams.financials.db.report.SmallMediumLargeReportService
import com.wadhams.financials.db.report.Last365DaysReportService
import com.wadhams.financials.db.type.Run

class FinancialReportingTestApp {
	static main(args) {
		println 'FinancialReportingTestApp started...'
		println ''

		PrintWriter pw = new PrintWriter(System.out, true)
		
		SmallMediumLargeReportService smallMediumLargeReportService = new SmallMediumLargeReportService()
		smallMediumLargeReportService.execute(pw)

		println ''
		println 'FinancialReportingTestApp ended.'
	}
}
