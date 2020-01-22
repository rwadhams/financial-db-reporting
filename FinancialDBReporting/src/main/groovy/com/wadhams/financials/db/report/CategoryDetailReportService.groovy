package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class CategoryDetailReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		pw.println 'CATEGORY DETAIL REPORT'
		pw.println '----------------------'

		//TODO: refactor to common Category enum
		List<String> catList = ['4WD', 'EQUIPMENT', 'TECHNOLOGY']
		catList.each {cat ->
			String query = buildQuery(cat)
			println query
			println ''
	
			List<FinancialDTO> financialList = databaseQueryService.buildList(query)
			
			report(financialList, cat, pw)
		}
		
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def report(List<FinancialDTO> financialList, String category, PrintWriter pw) {
		pw.println category
		
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		financialList.each {dto ->
			total = total.add(dto.amount)
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			String col3 = dto.payee.padRight(maxPayeeSize, ' ')
			String col4 = (dto.description == 'null') ? '' : dto.description
			pw.println "\t${sdf.format(dto.transactionDt)}  $col2  $col3  $col4"
		}
		
		pw.println "\tTotal: ${nf.format(total)}"
		pw.println ''
	}
	
	String buildQuery(String category) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '")
		sb.append(category)
		sb.append("' ")
		sb.append("ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}