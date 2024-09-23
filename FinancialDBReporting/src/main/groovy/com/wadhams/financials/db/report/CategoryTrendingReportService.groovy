package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.TrendingRangeDTO
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService

import groovy.sql.GroovyRowResult

class CategoryTrendingReportService {
	CategoryListService categoryListService
	CommonReportingService commonReportingService
	DatabaseQueryService databaseQueryService
	DateService dateService
	
	DateTimeFormatter h2DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	DateTimeFormatter reportDTF = DateTimeFormatter.ofPattern("LLL yyyy")
	
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	def execute(PrintWriter pw) {
		//Day to Day Categories
		List<String> dayToDayCategoryList = categoryListService.dayToDayCategoryList
		
		int trendingMonths = 12
		reportHeading("DAY TO DAY CATEGORY TRENDING REPORT OVER $trendingMonths MONTHS", pw)
		report(trendingMonths, dayToDayCategoryList, pw)
		
		trendingMonths = 3
		reportHeading("DAY TO DAY CATEGORY TRENDING REPORT OVER $trendingMonths MONTHS", pw)
		report(trendingMonths, dayToDayCategoryList, pw)
		
		//Unbudgeted Categories
		List<String> unbudgetedCategoryList = categoryListService.unbudgetedCategoryList
		
		trendingMonths = 12
		reportHeading("UNBUDGETED CATEGORY TRENDING REPORT OVER $trendingMonths MONTHS", pw)
		report(trendingMonths, unbudgetedCategoryList, pw)
		
		//Other Categories
		List<String> otherCategoryList = categoryListService.otherCategoryList
		
		trendingMonths = 12
		reportHeading("OTHER CATEGORY TRENDING REPORT OVER $trendingMonths MONTHS", pw)
		report(trendingMonths, otherCategoryList, pw)
	}
	
	def report(int trendingMonths, List<String> reportCategoryList, PrintWriter pw) {
		YearMonth firstYM = YearMonth.from(dateService.caravanStartDate)
		YearMonth lastYM = dateService.latestYearMonth
//		println "trendingMonths...: $trendingMonths"
//		println "firstYM..........: $firstYM"
//		println "lastYM...........: $lastYM"
//		println ''
		
		List<TrendingRangeDTO> trendingRangeDTOList = buildTrendingRangeDTOList(trendingMonths, firstYM, lastYM)
//		println "trendingRangeDTOList size: ${trendingRangeDTOList.size()}"
//		trendingRangeDTOList.each {dto ->
//			println dto
//		}
//		println ''

		List<String> categoryList = databaseQueryService.orderCategoryList(dateService.caravanStartDate, reportCategoryList /*categoryListService.dayToDayCategoryList*/)
//		List<String> categoryList = categoryListService.dayToDayCategoryList
//		List<String> categoryList = ['CARAVAN_EQUIPMENT']
//		List<String> categoryList = ['FOOD', 'ALCOHOL', 'FUEL', 'DRINKS', 'PREPARED_FOOD', 'CAMPING_FEES', 'ENTERTAINMENT', 'MEDICAL', 'PHARMACY']
		categoryList.each {category ->
			trendingRangeDTOList.each {dto ->
				augmentWithCategoryTotal(dto, category)
			}
			reportTrending(trendingMonths, category, trendingRangeDTOList, pw)
		}
	}
	
	def reportHeading(String heading, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
	}
	
	def reportTrending(int trendingMonths, String category, List<TrendingRangeDTO> trendingRangeDTOList, PrintWriter pw) {
		pw.println "$category"
		
		TrendingRangeDTO firstDTO = trendingRangeDTOList[0]
		pw.println "\t${reportDTF.format(firstDTO.startYM)} - ${reportDTF.format(firstDTO.endYM)} ${cf.format(firstDTO.amount).padLeft(10, ' ')}"
		BigDecimal total = firstDTO.amount
		
		BigDecimal prevAmount = firstDTO.amount
		trendingRangeDTOList[1..-1].each {dto ->
			//pw.println "\t${reportDTF.format(dto.startYM)} - ${reportDTF.format(dto.endYM)} ${cf.format(dto.amount).padLeft(10, ' ')}\t${cf.format(dto.amount.subtract(prevAmount)).padLeft(10, ' ')}"
			pw.println "\t${reportDTF.format(dto.startYM)} - ${reportDTF.format(dto.endYM)} ${cf.format(dto.amount).padLeft(10, ' ')}"
			total = total.add(dto.amount)
			prevAmount = dto.amount
		}
		BigDecimal average = total.divide(trendingRangeDTOList.size(), 2)
		pw.println "\t           Average: ${cf.format(average).padLeft(10, ' ')} every ${trendingMonths} months"
		pw.println ''
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
		else {
			dto.amount = BigDecimal.ZERO
		}
//		println dto
	}
	
	List<TrendingRangeDTO> buildTrendingRangeDTOList(int trendingMonths, YearMonth firstYM, YearMonth lastYM) {
		List<TrendingRangeDTO> dtoList = []
		
		//initialise startYM and endYM
		long monthSpan = trendingMonths - 1L
		YearMonth endYM = lastYM
		YearMonth startYM = endYM.minus(monthSpan)
		//println "Initial: startYM: $startYM endYM: $endYM"
		
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
