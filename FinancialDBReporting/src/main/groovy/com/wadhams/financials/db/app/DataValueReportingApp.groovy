package com.wadhams.financials.db.app

import com.wadhams.financials.db.report.DataValueReportingService

class DataValueReportingApp {
	static main(args) {
		println 'DataValueReportingApp started...'
		println ''

		PrintWriter pw = (new File('out/data-value-report.txt')).newPrintWriter()
		
		//report headings
		pw.println 'Data Value Report'
		pw.println '================='
		pw.println ''
		
		DataValueReportingService service = new DataValueReportingService()
		service.reportDistinctValues(pw)
		
		pw.close()
		
		println 'DataValueReportingApp ended.'
	}
}
