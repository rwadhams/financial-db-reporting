package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.BudgetReportingController
import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.type.Run

class FinancialReportingApp {
	static main(args) {
		println 'FinancialReportingApp started...'
		println ''

		if (args.size() > 0) {
			Run run = Run.findByName(args[0])
			println "Run parameter: $run"
			println ''
			if (run == Run.TimestampReport) {
				String datetime = (new Date()).format('yyyy-MM-dd-HH-mm-ss')
				PrintWriter pw = (new File("out/financial-report-${datetime}.txt")).newPrintWriter()
				
				FinancialReportingController controller = new FinancialReportingController()
				controller.execute(pw)
				
				pw.close()
			}
			else if (run == Run.OverWriteReport) {
				PrintWriter pw1 = (new File('out/financial-report.txt')).newPrintWriter()
				FinancialReportingController controller1 = new FinancialReportingController()
				controller1.execute(pw1)
				pw1.close()
				
				PrintWriter pw2 = (new File('out/budget-report.txt')).newPrintWriter()
				BudgetReportingController controller2 = new BudgetReportingController()
				controller2.execute(pw2)
				pw2.close()
			}
			else {
				println 'Unknown parameter. Application did not run.'
			}
		}
		else {
			println 'Missing parameter(s). Application did not run.'
		}

		println ''
		println 'FinancialReportingApp ended.'
	}
}
