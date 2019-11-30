package com.wadhams.financials.db.app

import com.wadhams.financials.db.controller.FinancialReportingController

class FinancialReportingApp {
	static main(args) {
		println 'FinancialReportingApp started...'
		println ''

		if (args.size() == 0 ) {
			//String datetime = (new Date()).format('yyyy-MM-dd-HH-mm-ss')
			//PrintWriter pw = (new File("out/financial-report-${datetime}.txt")).newPrintWriter()
			
			FinancialReportingController controller = new FinancialReportingController()
			//controller.execute(pw)
			controller.execute()
			
			//pw.close()
		}
		else {
			println 'Unknown parameter. Application did not run.'
		}
		
		println ''
		println 'FinancialReportingApp ended.'
	}
}
