package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class AssetDetailReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute() {
		File f = new File("out/asset-detail-report.txt")
		
		f.withPrintWriter {pw ->
			pw.println 'ASSET DETAIL REPORT'
			pw.println '----------------------'
	
			List<String> assetList = databaseQueryService.buildAllAssetList()
			//println assetList
			
			assetList.each {asset ->
				String query = buildQuery(asset)
				//println query
				//println ''
		
				List<FinancialDTO> financialList = databaseQueryService.buildList(query)
				
				report(financialList, asset, pw)
			}
		}
	}
	
	def report(List<FinancialDTO> financialList, String category, PrintWriter pw) {
		pw.println "Asset: $category"
		
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		int maxCategorySize = commonReportingService.maxCategorySize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		financialList.each {dto ->
			total = total.add(dto.amount)
			String s1 = nf.format(dto.amount).padLeft(12, ' ')
			String s2 = dto.payee.padRight(maxPayeeSize, ' ')
			String s3 = dto.category.padRight(maxCategorySize, ' ')
			String s4 = (dto.description == 'null') ? '' : dto.description
			
			pw.println "\t${dto.transactionDt.format(dtf)}  $s1  $s2  $s3  $s4"
		}
		
		pw.println "\tTotal: ${nf.format(total)}"
		pw.println ''
	}
	
	String buildQuery(String asset) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET as ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE ASSET = '")
		sb.append(asset)
		sb.append("' ")
		sb.append("ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
