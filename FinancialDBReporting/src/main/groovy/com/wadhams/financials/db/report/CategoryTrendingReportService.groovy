package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.Month
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.CountDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.dto.TrendingRangeDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

import groovy.sql.GroovyRowResult

class CategoryTrendingReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	DateTimeFormatter reportDTF = DateTimeFormatter.ofPattern("LLL yyyy")
	
	def execute(PrintWriter pw) {
		int monthRange = 6
		YearMonth firstYM = YearMonth.of(2020, Month.MAY)
		YearMonth lastYM = YearMonth.of(2023, Month.APRIL)
//		println "monthRange...: $monthRange"
//		println "firstYM......: $firstYM"
//		println "lastYM.......: $lastYM"
//		println ''
		
		List<TrendingRangeDTO> trendingRangeDTOList = buildTrendingRangeDTOList(monthRange, firstYM, lastYM)
//		println "trendingRangeDTOList size: ${trendingRangeDTOList.size()}"
//		trendingRangeDTOList.each {dto ->
//			println dto
//		}
//		println ''

		reportHeading(monthRange, pw)
		
		List<String> categoryList = ['CARAVAN_EQUIPMENT']
//		List<String> categoryList = ['FOOD', 'ALCOHOL', 'FUEL', 'DRINKS', 'PREPARED_FOOD', 'CAMPING_FEES', 'ENTERTAINMENT', 'MEDICAL', 'PHARMACY']
		categoryList.each {category ->
			trendingRangeDTOList.each {dto ->
				augmentWithCategoryTotal(dto, category)
			}
			reportTrending(monthRange, category, trendingRangeDTOList, pw)
		}
		
		
	}
	
	def reportTrending(int monthRange, String category, List<TrendingRangeDTO> trendingRangeDTOList, PrintWriter pw) {
		pw.println "$category"
		
		TrendingRangeDTO firstDTO = trendingRangeDTOList[0]
		pw.println "\t${reportDTF.format(firstDTO.startYM)} - ${reportDTF.format(firstDTO.endYM)} ${cf.format(firstDTO.amount).padLeft(10, ' ')}"
		BigDecimal total = firstDTO.amount
		
		BigDecimal prevAmount = firstDTO.amount
		trendingRangeDTOList[1..-1].each {dto ->
			pw.println "\t${reportDTF.format(dto.startYM)} - ${reportDTF.format(dto.endYM)} ${cf.format(dto.amount).padLeft(10, ' ')}\t${cf.format(dto.amount.subtract(prevAmount)).padLeft(10, ' ')}"
			total = total.add(dto.amount)
			prevAmount = dto.amount
		}
		BigDecimal average = total.divide(trendingRangeDTOList.size(), 2)
		pw.println "\t           Average: ${cf.format(average).padLeft(10, ' ')}"
		pw.println ''
	}
	
	def reportHeading(int monthRange, PrintWriter pw) {
		String heading = "CATEGORY TRENDING REPORT OVER $monthRange MONTHS"
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
	}
	
	def augmentWithCategoryTotal(TrendingRangeDTO dto, String category) {
		String query = buildQuery(category, dto.startRangeDate, dto.endRangeDate)
//		println query
//		println ''
		
		GroovyRowResult grr = databaseQueryService.firstRow(query)
		def amount = grr.getProperty('AMT')
		if (amount) {
			dto.amount = amount
		}
//		println dto
	}
	
	List<TrendingRangeDTO> buildTrendingRangeDTOList(int monthRange, YearMonth firstYM, YearMonth lastYM) {
		List<TrendingRangeDTO> dtoList = []
		
		//initialise startYM and endYM
		long monthSpan = monthRange - 1L
		YearMonth endYM = lastYM
		YearMonth startYM = endYM.minus(monthSpan)
		//println "Initial: startYM: $startYM endYM: $endYM"
		
		//while (startYM.isAfter(firstYM)) {
		while (startYM >= firstYM) {
			TrendingRangeDTO dto = new TrendingRangeDTO(startYM, endYM)
			dtoList << dto
			endYM = startYM.minus(1L)
			startYM = endYM.minus(monthSpan)
		}
		
		return dtoList.reverse()
	}
	
	String buildQuery(String category, LocalDate startRangeDate, LocalDate endRangeDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '")
		sb.append(category)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(startRangeDate.format(h2DTF))
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(endRangeDate.format(h2DTF))
		sb.append("'")
		
		return sb.toString()
	}
}
