package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.wadhams.financials.db.dto.CategoryTotalAverageDTO
import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.helper.ListControlBreak
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.service.DateService
import com.wadhams.financials.db.type.ReportingAmount

class BudgetReportService {
	//TODO inject from controller
	CategoryListService categoryListService = new CategoryListService()
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	DateService dateService = new DateService()
	
	DateTimeFormatter reportingDTF = DateTimeFormatter.ofPattern("dd/MM/yyyy")
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	BigDecimal monthsPerYear = new BigDecimal('12')
	BigDecimal daysPerYear = new BigDecimal('365')

	BigDecimal monthlyAverageTotal = BigDecimal.ZERO

	def execute(PrintWriter pw) {
		//Reused data structures
		String query
		String heading
		List<FinancialDTO> financialDTOList
		List<CategoryTotalAverageDTO> ctaDTOList
		
		//RUNNING COSTS
		query = buildQuerySpecificOngoing(categoryListService.runningCostCategoryList)
		financialDTOList = databaseQueryService.buildList(query)
		reportRunningCosts('RUNNING COST CATEGORIES (MONTHLY AVERAGE)', financialDTOList, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//average calculations use caravanStartDate and maxTransactionDate in it's calculations
		int elapsedDays = ChronoUnit.DAYS.between(dateService.caravanStartDate, dateService.maxTransactionDate) + 1
		
		//DAY TO DAY CATEGORIES
		query = buildQueryWithGreaterTransactionDate(categoryListService.dayToDayCategoryList, dateService.caravanStartDate)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		//average each total
		ctaDTOList.each {cta ->
			cta.average = averageBigDecimal(cta.total, elapsedDays)
		}
		heading = "DAY TO DAY CATEGORIES FROM LAST $elapsedDays DAYS (MONTHLY AVERAGE)"
		reportCategoryTotalAverageList(heading, ctaDTOList, ReportingAmount.Average, pw)

		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//UNBUDGETED
		query = buildQueryWithGreaterTransactionDate(categoryListService.unbudgetedCategoryList, dateService.caravanStartDate)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		heading = "UNBUDGETED CATEGORIES            TOTAL (Transactions after ${dateService.caravanStartDate.format(reportingDTF)})" 
		reportCategoryTotalAverageList(heading, ctaDTOList, ReportingAmount.Total, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		//OTHER
		query = buildQueryWithGreaterTransactionDate(categoryListService.otherCategoryList, dateService.caravanStartDate)
		ctaDTOList = databaseQueryService.buildCategoryTotalAverageDTOList(query)
		heading = "OTHER CATEGORIES                 TOTAL (Transactions after ${dateService.caravanStartDate.format(reportingDTF)})"
		reportCategoryTotalAverageList(heading, ctaDTOList, ReportingAmount.Total, pw)
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
		
		reportMonthlyYearlyTotal(pw)
	}
	
	def reportRunningCosts(String heading, List<FinancialDTO> financialList, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
			
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		
		//Total
		BigDecimal reportTotal = BigDecimal.ZERO
			
		ListControlBreak cb = new ListControlBreak(financialList)
		FinancialDTO current = cb.first()

		while (cb.hasMore()) {
			String savedCategory = current.category
			BigDecimal categoryTotal = BigDecimal.ZERO
			BigDecimal categoryDays = BigDecimal.ZERO
			while (cb.hasMore() && savedCategory == current.category) {
				//println savedCategory
				categoryTotal = categoryTotal.add(current.amount)
				int days = current.endDt - current.startDt + 1
				categoryDays = categoryDays.add(days)
				current = cb.next()
			}
			//println "$categoryTotal\t$categoryDays"
			BigDecimal categoryAverage = categoryTotal.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
			pw.println "${commonReportingService.buildFixedWidthLabel(savedCategory, 25)}${cf.format(categoryAverage).padLeft(11, ' ')}"
			reportTotal = reportTotal.add(categoryAverage)
		}
		pw.println ''
		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${cf.format(reportTotal).padLeft(11, ' ')}"
		
		monthlyAverageTotal += reportTotal
	}
	
	def reportCategoryTotalAverageList(String heading, List<CategoryTotalAverageDTO> list, ReportingAmount ra, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1
		
		BigDecimal total = new BigDecimal(0.0)
		list.each {dto ->
			if (ra == ReportingAmount.Average) {
				total = total.add(dto.average)
				pw.println "${commonReportingService.buildFixedWidthLabel(dto.category, 25)}${cf.format(dto.average).padLeft(11, ' ')}"
			}
			else if (ra == ReportingAmount.Total) {
				total = total.add(dto.total)
				pw.println "${commonReportingService.buildFixedWidthLabel(dto.category, 25)}${cf.format(dto.total).padLeft(11, ' ')}"
			}
		}
		
		pw.println ''
		if (ra == ReportingAmount.Average) {
			pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Average', 25)}${cf.format(total).padLeft(11, ' ')}"
			monthlyAverageTotal += total
		}
		else if (ra == ReportingAmount.Total) {
			pw.println "${commonReportingService.buildFixedWidthLabel('Grand Total', 25)}${cf.format(total).padLeft(11, ' ')}"
		}
		else {
			pw.println 'Unknown ReportingAmount Enum'
		}
	}
	
	def reportMonthlyYearlyTotal(PrintWriter pw) {
		String heading = 'MONTHLY and YEARLY TOTALS:'
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		pw.println "${commonReportingService.buildFixedWidthLabel('Monthly Total', 25)}${cf.format(monthlyAverageTotal).padLeft(11, ' ')}"
		BigDecimal yearlyAverageTotal = monthlyAverageTotal.multiply(monthsPerYear)
		pw.println "${commonReportingService.buildFixedWidthLabel('Yearly Total', 25)}${cf.format(yearlyAverageTotal).padLeft(11, ' ')}"
	}
	
	String buildQuerySpecificOngoing(List<String> categoryList) {
		StringBuilder sb = new StringBuilder()
		
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 IN ('SPECIFIC_RUNNING_COST', 'ONGOING_RUNNING_COST') ")
		sb.append("AND CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

	String buildQueryWithGreaterTransactionDate(List<String> categoryList, LocalDate startDate) {
		DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as CAT, SUM(AMOUNT) as TOTAL ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN (")
		sb.append(databaseQueryService.buildFormattedList(categoryList))
		sb.append(") ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(startDate.format(h2dtf))
		sb.append("' ")
		sb.append("GROUP BY CATEGORY ")
		sb.append("ORDER BY 2 DESC")
		
		return sb.toString()
	}

	BigDecimal averageBigDecimal(BigDecimal bd, long days) {
		BigDecimal numberOfDays = new BigDecimal(days)
		return bd.multiply(daysPerYear).divide(monthsPerYear, 2).divide(numberOfDays, 2)
	}
	
}
