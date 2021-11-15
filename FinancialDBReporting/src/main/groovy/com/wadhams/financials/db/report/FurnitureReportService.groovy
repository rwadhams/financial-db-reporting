package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class FurnitureReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		String query = buildQuery()
		//println query
		//println ''

		List<FinancialDTO> financialList = databaseQueryService.buildList(query)
		
		reportSummary(financialList, pw)
		
		reportDetail(financialList)
	}
	
	def reportSummary(List<FinancialDTO> financialList, PrintWriter pw) {
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		pw.println 'CAMP HILL FURNITURE REPORT'
		pw.println '--------------------------'

		financialList.each {dto ->
			total = total.add(dto.amount)
		}
		
		pw.println "Total...: ${nf.format(total)}"
		pw.println ''
		pw.println '\t(See \'camp-hill-furniture-report.txt\' for specific details)'
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def reportDetail(List<FinancialDTO> financialList) {
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		File f = new File("out/camp-hill-furniture-report.txt")
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		f.withPrintWriter {pw ->
			pw.println "Camp Hill Furniture Details:"
			pw.println ''
			
			financialList.each {dto ->
				total = total.add(dto.amount)
				String col2 = nf.format(dto.amount).padLeft(12, ' ')
				//String col3 = dto.payee.padRight(maxPayeeSize, ' ')
				String col4 = (dto.description == 'null') ? '' : dto.description
				pw.println "${dto.transactionDt.format(dtf)}  $col2  $col4"
			}
	
			pw.println ''
			pw.println "Total: ${nf.format(total)}"
		}
	}
	
	//TODO: refactor to common Category enum
	String buildQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = 'FURNITURE' ")
		sb.append("ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
