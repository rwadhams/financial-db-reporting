package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.SmallMediumLargeDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

import groovy.sql.GroovyRowResult

class SmallMediumLargeReportService {
	DatabaseQueryService databaseQueryService
	CommonReportingService commonReportingService
	
	String smallMediumBoundary = '400'
	String mediumLargeBoundary = '5000'
	
	def execute(PrintWriter pw) {
		List<SmallMediumLargeDTO> smlList = buildSmallMediumLargeDTOList()
		smlList.each {dto ->
			augmentDTOWithAmounts(dto)
			//println dto
		}
		
		reportSummary(smlList, pw)
	}
	
	List<SmallMediumLargeDTO> buildSmallMediumLargeDTOList() {
		List<SmallMediumLargeDTO> smlList = []
		
		YearMonth now = YearMonth.now()
		//TODO hardcoded ???
		YearMonth ym = YearMonth.of(2019, 7)
		
		while (ym.isBefore(now)) {
			SmallMediumLargeDTO dto = new SmallMediumLargeDTO(ym : ym, startDate : ym.atDay(1).toString(), endDate : ym.atEndOfMonth().toString())
			smlList << dto
			
			ym = ym.plusMonths(1L)
		}
		
		return smlList
	}
	
	def augmentDTOWithAmounts(SmallMediumLargeDTO dto) {
		GroovyRowResult grr = null
		def amount = null
		
		//Small
		String sQuery = buildSmallAmountQuery(dto.startDate, dto.endDate)
		//println sQuery
		grr = databaseQueryService.firstRow(sQuery)
		amount = grr.getProperty('AMT')
		//println "Amount: $amount"
		if (amount) {
			dto.smallAmount = new BigDecimal(amount)
		}
		else {
			dto.smallAmount = new BigDecimal('0.0')
		}
		
		//Medium
		String mQuery = buildMediumAmountQuery(dto.startDate, dto.endDate)
		//println mQuery
		grr = databaseQueryService.firstRow(mQuery)
		amount = grr.getProperty('AMT')
		//println "Amount: $amount"
		if (amount) {
			dto.mediumAmount = new BigDecimal(amount)
		}
		else {
			dto.mediumAmount = new BigDecimal('0.0')
		}
		
		//Large
		String lQuery = buildLargeAmountQuery(dto.startDate, dto.endDate)
		//println lQuery
		grr = databaseQueryService.firstRow(lQuery)
		amount = grr.getProperty('AMT')
		//println "Amount: $amount"
		if (amount) {
			dto.largeAmount = new BigDecimal(amount)
		}
		else {
			dto.largeAmount = new BigDecimal('0.0')
		}
		
		//Total
		dto.totalAmount = new BigDecimal('0.0').add(dto.smallAmount).add(dto.mediumAmount).add(dto.largeAmount)
	}
	
	def reportSummary(List<SmallMediumLargeDTO> smlList, PrintWriter pw) {
		NumberFormat cf = NumberFormat.getCurrencyInstance()
		DateTimeFormatter formatterYYYYMM   = DateTimeFormatter.ofPattern('MMM yyyy')
		
		pw.println 'SMALL, MEDIUM & LARGE TRANSACTION AMOUNTS MONTHLY TOTAL REPORT'
		pw.println '--------------------------------------------------------------'
		pw.println ''
		pw.println "Small  transactions are < \$$smallMediumBoundary"
		pw.println "Medium transactions are >= \$$smallMediumBoundary and <= \$$mediumLargeBoundary"
		pw.println "Large  transactions are > \$$mediumLargeBoundary"
		pw.println ''
		pw.println 'Month     Small        Medium       Large           MONTHLY'
		pw.println '          Transaction  Transaction  Transaction     TOTAL'
		pw.println '          Totals       Totals       Totals'
		
		smlList.each {dto ->
			//println dto
			String yearMonth = formatterYYYYMM.format(dto.ym)
			String smallAmount = cf.format(dto.smallAmount).padLeft(12, ' ')
			String mediumAmount = cf.format(dto.mediumAmount).padLeft(13, ' ')
			String largeAmount = cf.format(dto.largeAmount).padLeft(13, ' ')
			String totalAmount = cf.format(dto.totalAmount).padLeft(13, ' ')
			pw.println "${yearMonth}${smallAmount}${mediumAmount}${largeAmount}${totalAmount}"
		}
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	//Small amount query
	String buildSmallAmountQuery(String firstDate, String lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT < ${smallMediumBoundary} ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(firstDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(lastDate)
		sb.append("'")

		return sb.toString()
	}

	//Medium amount query
	String buildMediumAmountQuery(String firstDate, String lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT >= ${smallMediumBoundary} ")
		sb.append("AND AMOUNT <= ${mediumLargeBoundary} ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(firstDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(lastDate)
		sb.append("'")

		return sb.toString()
	}

	//Large amount query
	String buildLargeAmountQuery(String firstDate, String lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE AMOUNT > ${mediumLargeBoundary} ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(firstDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(lastDate)
		sb.append("'")

		return sb.toString()
	}

}
