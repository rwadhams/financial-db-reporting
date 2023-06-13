package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService

class MonthlyCategoryDetailReportService {
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	DatabaseQueryService databaseQueryService
	DateService dateService
	
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	
	def execute(YearMonth latestYearMonth, PrintWriter pw) {
		pw.println 'MONTHLY CATEGORY DETAIL REPORT'
		pw.println '------------------------------'
			
		//TODO: refactor to common Category enum
		List<String> catList = categoryListService.dayToDayCategoryList
		catList.each {cat ->
			String query = buildQuery(cat, latestYearMonth.atDay(1), latestYearMonth.atEndOfMonth())
			//println query
			//println ''
	
			List<FinancialDTO> financialList = databaseQueryService.buildList(query)
			
			report(financialList, cat, pw)
		}
		
	}
	
	def report(List<FinancialDTO> financialList, String category, PrintWriter pw) {
		pw.println category
		
		int maxPayeeSize = commonReportingService.maxPayeeSize(financialList)
		
		BigDecimal total = new BigDecimal(0.0)
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		financialList.each {dto ->
			total = total.add(dto.amount)
			String s1 = nf.format(dto.amount).padLeft(12, ' ')
			String s2 = dto.payee.padRight(maxPayeeSize, ' ')
			String s3 = (dto.description == 'null') ? '' : dto.description
			
			String s5, s6
			boolean startDtFound = false
			if(dto.startDt) {
				startDtFound = true
				s5 = dto.startDt.format(dtf)
				s6 = dto.endDt.format(dtf)
			}
			
			pw.println "\t${dto.transactionDt.format(dtf)}  $s1  $s2  ${(startDtFound)?("[$s5 - $s6]"):''}  $s3"
		}
		
		pw.println "\tTotal: ${nf.format(total)}"
		pw.println ''
	}
	
	String buildQuery(String category, LocalDate firstDate, LocalDate lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '")
		sb.append(category)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(h2DTF.format(firstDate))
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(h2DTF.format(lastDate))
		sb.append("'")
		sb.append("ORDER BY TRANSACTION_DT, AMOUNT DESC")
		
		return sb.toString()
	}
	
}
