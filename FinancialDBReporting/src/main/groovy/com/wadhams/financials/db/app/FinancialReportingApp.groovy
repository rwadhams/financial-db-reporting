package com.wadhams.financials.db.app

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.controller.BudgetReportingController
import com.wadhams.financials.db.controller.ExtraReportingController
import com.wadhams.financials.db.controller.FinancialReportingController
import com.wadhams.financials.db.controller.Last365DaysReportingController
import com.wadhams.financials.db.controller.NoLongerRequiredReportingController
import com.wadhams.financials.db.type.Run

class FinancialReportingApp {
	static main(args) {
		println 'FinancialReportingApp started...'
		println ''

		if (args.size() > 0) {
			Run run = Run.findByName(args[0])
			println "Run parameter: $run"
			println ''
			String filenameSuffix = ''
			if (run == Run.TimestampReport) {
				DateTimeFormatter suffixDTF = DateTimeFormatter.ofPattern('yyyy-MM-dd-HH-mm-ss')
				LocalDateTime ldt = LocalDateTime.now()
				filenameSuffix = '-' + ldt.format(suffixDTF)
			}

			if (run == Run.OverWriteReport || run == Run.TimestampReport) {
				PrintWriter pw1 = (new File("out/financial-report${filenameSuffix}.txt")).newPrintWriter()
				FinancialReportingController controller1 = new FinancialReportingController()
				controller1.execute(pw1)
				pw1.close()
				
				PrintWriter pw2 = (new File("out/budget-report${filenameSuffix}.txt")).newPrintWriter()
				BudgetReportingController controller2 = new BudgetReportingController()
				controller2.execute(pw2)
				pw2.close()
				
				PrintWriter pw3 = (new File("out/no-longer-required-report${filenameSuffix}.txt")).newPrintWriter()
				NoLongerRequiredReportingController controller3 = new NoLongerRequiredReportingController()
				controller3.execute(pw3)
				pw3.close()
				
				PrintWriter pw4 = (new File("out/last-365-days-detail-report${filenameSuffix}.txt")).newPrintWriter()
				Last365DaysReportingController controller4 = new Last365DaysReportingController()
				controller4.execute(pw4)
				pw4.close()
				
				ExtraReportingController controller9 = new ExtraReportingController()
				controller9.execute()
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
