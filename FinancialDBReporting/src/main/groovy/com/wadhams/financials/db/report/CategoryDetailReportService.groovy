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
	
	def execute() {
		File f = new File("out/category-detail-report.txt")
		
		f.withPrintWriter {pw ->
			pw.println 'CATEGORY DETAIL REPORT'
			pw.println '----------------------'
	
			//TODO: refactor to common Category enum
			List<String> catList = databaseQueryService.buildAllCategoryList()
			catList.each {cat ->
				String query = buildQuery(cat)
				println query
				println ''
		
				List<FinancialDTO> financialList = databaseQueryService.buildList(query)
				
				report(financialList, cat, pw)
			}
		}
	}
	
	def report(List<FinancialDTO> financialList, String category, PrintWriter pw) {
		pw.println category
		
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		financialList.each {dto ->
			total = total.add(dto.amount)
			String s1 = nf.format(dto.amount).padLeft(12, ' ')
			String s2 = dto.payee.padRight(maxPayeeSize, ' ')
			String s3 = (dto.description == 'null') ? '' : dto.description
			
			String s4
			boolean assetFound = false
			if (dto.asset) {
				assetFound = true
				s4 = dto.asset
			}
			
			String s5, s6
			boolean startDtFound = false
			if(dto.startDt) {
				startDtFound = true
				s5 = sdf.format(dto.startDt)
				s6 = sdf.format(dto.endDt)
			}
			
			String s7
			boolean rg1Found = false
			if (dto.rg1) {
				rg1Found = true
				s7 = dto.rg1
			}
			
			pw.println "\t${sdf.format(dto.transactionDt)}  $s1  $s2  ${(rg1Found)?s7:''}  ${(startDtFound)?("[$s5 - $s6]"):''}  ${(assetFound)?("Asset: $s4"):''}  $s3"
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
