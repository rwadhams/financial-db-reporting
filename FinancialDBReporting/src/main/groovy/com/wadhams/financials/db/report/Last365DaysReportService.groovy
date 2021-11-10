package com.wadhams.financials.db.report

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth

class Last365DaysReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String query = buildQuery()
		println query
		println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		
		reportSummary(financialList, pw)
		
		reportDetail(financialList)
	}
	
	def reportSummary(List<FinancialDTO> financialList, PrintWriter pw) {
		BigDecimal total = new BigDecimal(0.0)
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		pw.println 'LAST 365 DAYS REPORT'
		pw.println '--------------------'

		financialList.each {dto ->
			total = total.add(dto.amount)
		}
		
		pw.println "Total...: ${nf.format(total)}"
		pw.println ''
		pw.println '\t(See \'last-365-days-detail-report.txt\' for specific details)'
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def reportDetail(List<FinancialDTO> financialList) {
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		File f = new File("out/last-365-days-detail-report.txt")
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		f.withPrintWriter {pw ->
			pw.println "Last 365 Days Details:"
			pw.println ''
			
			financialList.each {dto ->
				total = total.add(dto.amount)
				String col2 = nf.format(dto.amount).padLeft(12, ' ')
				String col3 = dto.payee.padRight(maxPayeeSize, ' ')
				String col4 = (dto.description == 'null') ? '' : dto.description
				pw.println "${sdf.format(dto.transactionDt)}  $col2  $col3  $col4"
			}
	
			pw.println ''
			pw.println "Total: ${nf.format(total)}"
		}
	}
	
	String buildQuery() {
		StringBuilder sb = new StringBuilder()
		YearMonth now = YearMonth.now()
		
		YearMonth start1 = now.minusYears(1L)	//1 year
		String start = start1.atDay(1).toString()
		//println "start date...: $start"
		
		YearMonth end1 = now.minusMonths(1L)	//1 month
		String end = end1.atEndOfMonth().toString()
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
