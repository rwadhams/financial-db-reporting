package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.CountDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class TrendingReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	def execute(PrintWriter pw) {
		//Reused data structures
		String query
		
		//TRENDING
		long longTrendingDays = 2 * 365L	//2 years
		long shortTrendingDays = 95L	//3.5 months
		LocalDate now = LocalDate.now()
		LocalDate longTrendingDate = now.minusDays(longTrendingDays)
		LocalDate shortTrendingDate = now.minusDays(shortTrendingDays)
		
		int havingCount = 2
		query = buildCountQueryWithGreaterTransactionDate(shortTrendingDate, havingCount)
		List<CountDTO> countDTOList = databaseQueryService.buildCountDTOList(query)
		
		//build category list
		List<String> categoryList = []
		countDTOList.each {c ->
			categoryList << c.countName
		}
		
		//longTrendingDate totals
		query = buildQueryWithGreaterTransactionDate(categoryList, longTrendingDate)
		List<TotalDTO> longTrendingList = databaseQueryService.buildTotalsList(query)
		
		//shortTrendingDate totals
		query = buildQueryWithGreaterTransactionDate(categoryList, shortTrendingDate)
		List<TotalDTO> shortTrendingList = databaseQueryService.buildTotalsList(query)

		assert longTrendingList.size() == shortTrendingList.size()
		
		//average each list
		int overseasAdjustmentDays = 35		//7 weeks
		long longTrendingDaysAdjusted = longTrendingDays - overseasAdjustmentDays
		List<TotalDTO> longTrendingAverageList = averageTotalsList(longTrendingList, longTrendingDaysAdjusted as int)
		List<TotalDTO> shortTrendingAverageList = averageTotalsList(shortTrendingList, shortTrendingDays as int)
		assert longTrendingAverageList.size() == shortTrendingAverageList.size()

		reportTrending(longTrendingAverageList, longTrendingDaysAdjusted, shortTrendingAverageList, shortTrendingDays, pw)
	}
	
	def reportTrending(List<TotalDTO> longTrendingList, long longTrendingDays, List<TotalDTO> shortTrendingList, long shortTrendingDays, PrintWriter pw) {
		String heading = 'TRENDING AVERAGES REPORT'
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		String s1 = longTrendingDays.toString().padLeft(4, ' ')
		String s2 = shortTrendingDays.toString().padLeft(4, ' ')
		pw.println '                                 Days      Days     Trend'
		pw.println "                                 $s1      $s2"
		
		for (int i=0; i < longTrendingList.size(); i++) {
			assert longTrendingList[i].totalName == shortTrendingList[i].totalName
			BigDecimal shortTrendingAmount = shortTrendingList[i].totalAmount.subtract(longTrendingList[i].totalAmount)
			pw.println "${commonReportingService.buildFixedWidthLabel(longTrendingList[i].totalName, 25)}${cf.format(longTrendingList[i].totalAmount).padLeft(10, ' ')}${cf.format(shortTrendingList[i].totalAmount).padLeft(10, ' ')}   ${cf.format(shortTrendingAmount).padLeft(7, ' ')}"
		}
	}
	
	List<TotalDTO> averageTotalsList(List<TotalDTO> tl, int days) {
		List<TotalDTO> totalList = []
		
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		BigDecimal categoryDays = new BigDecimal(days)
		
		tl.each {t ->
			BigDecimal average = t.totalAmount.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
			
			TotalDTO dto = new TotalDTO()
			dto.totalName = t.totalName
			dto.totalAmount = average
			totalList << dto
		}
		
		return totalList
	}
	
	String buildCountQueryWithGreaterTransactionDate(LocalDate startDate, int havingCount) {
		DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as CAT, COUNT(*) as COUNT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT >= '")
		sb.append(startDate.format(h2dtf))
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("HAVING COUNT(*) > ${havingCount} ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQueryWithGreaterTransactionDate(List<String> categoryList, LocalDate startDate) {
		DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(startDate.format(h2dtf))
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

}
