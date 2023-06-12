package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class Last365DaysReportService {
	DatabaseQueryService databaseQueryService
	CommonReportingService commonReportingService
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern('dd/MM/yyyy')
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	def execute(PrintWriter pw) {
		YearMonth now = YearMonth.now()
		YearMonth start = now.minusYears(1L)	//1 year
		LocalDate startDate = start.atDay(1)
		YearMonth end = now.minusMonths(1L)		//1 month
		LocalDate endDate = end.atEndOfMonth()
		
		String query = buildQuery(startDate, endDate)
//		println query
//		println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		
		String reportingDateRangeText = buildReportingDateRangeText(startDate, endDate)
		
		reportDetail(financialList, reportingDateRangeText, pw)
	}
	
	def reportDetail(List<FinancialDTO> financialList, String reportingDateRangeText, PrintWriter pw) {
		int maxCategorySize = commonReportingService.maxCategorySize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		financialList.each {dto ->
			total = total.add(dto.amount)
		}

		pw.println "LAST 365 DAYS REPORT ($reportingDateRangeText)"
		pw.println '-----------------------------------------------'
		pw.println ''
		pw.println "Total...: ${cf.format(total)}"
		pw.println ''
					
		financialList.each {dto ->
			String col2 = cf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.category.padRight(maxCategorySize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "${dto.transactionDt.format(dtf)}  $col2  $col3  $col4"
		}

		pw.println ''
		pw.println "Total: ${cf.format(total)}"
	}
	
	String buildReportingDateRangeText(LocalDate startDate, LocalDate endDate) {
		return "${startDate.format(dtf)} to ${endDate.format(dtf)}"
	}
	
	String buildQuery(LocalDate startDate, LocalDate endDate) {
		StringBuilder sb = new StringBuilder()

		String start = startDate.toString()
		//println "start date...: $start"
		
		String end = endDate.toString()
		//println "end date.....: $end"
		
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT >= '")
		sb.append(start)
		sb.append("' AND ")
		sb.append("TRANSACTION_DT <= '")
		sb.append(end)
		sb.append("' ")
		sb.append("ORDER BY AMOUNT DESC")
		
		return sb.toString()
	}

}
