package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class LargeTransactionReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String largeAmount = '400'
		String query = buildQuery(largeAmount)
		println query
		println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		
		report(financialList, largeAmount, pw)
	}
	
	def report(List<FinancialDTO> financialList, String largeAmount, PrintWriter pw) {
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal grandTotal = new BigDecimal(0.0)
		BigDecimal oneYearTotal = new BigDecimal(0.0)
		
		YearMonth now = YearMonth.now()
		YearMonth nowMinusOneYear1 = now.minusYears(1L)	//minus 1 year
		YearMonth nowMinusOneYear2 = nowMinusOneYear1.minusMonths(1L)	//minus 1 month
		LocalDate oneYearAgo = nowMinusOneYear2.atEndOfMonth()	//end of month
		//println "zzz one year ago date...: $oneYearAgo"
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		String heading = "LARGE TRANSACTION REPORT (> \$$largeAmount)"
		pw.println heading
		pw.println ''.padLeft(heading.size(), '-')

		financialList.each {dto ->
			grandTotal = grandTotal.add(dto.amount)
			LocalDate txnDate = LocalDate.parse(dto.transactionDt.toString())
			if (txnDate.isAfter(oneYearAgo)) {
				oneYearTotal = oneYearTotal.add(dto.amount)
			}
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.payee.padRight(maxPayeeSize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "${dto.transactionDt.format(dtf)}  $col2  $col3  $col4"
		}
		
		pw.println ''
		pw.println "Large Expense Transaction Grand Total......: ${nf.format(grandTotal).padLeft(13, ' ')}"
		pw.println "Large Expense Transaction Last Yearly Total: ${nf.format(oneYearTotal).padLeft(13, ' ')}"
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	String buildQuery(String largeAmount) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE ABS(AMOUNT) > ")
		sb.append(largeAmount)
		sb.append(" ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
