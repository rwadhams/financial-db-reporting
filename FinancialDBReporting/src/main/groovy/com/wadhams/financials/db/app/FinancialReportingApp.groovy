package com.wadhams.financials.db.app

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
				PrintWriter pw = (new File('out/financial-report.txt')).newPrintWriter()
				
				FinancialReportingController controller = new FinancialReportingController()
				controller.execute(pw)
				
				pw.close()
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
