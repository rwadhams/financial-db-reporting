package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql
import com.wadhams.financials.db.comparator.TotalDTOAmtComparator
import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.dto.TotalDTO
import com.wadhams.financials.db.helper.ListControlBreak
import com.wadhams.financials.db.helper.StartEndDate
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

class MonthlyAverageCampingCostReportService {
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		pw.println 'MONTHLY AVERAGE CAMPING COSTS REPORT'
		pw.println '------------------------------------'
		
		List<TotalDTO> totalList = []	//All Totals go in here before reporting
		
		//StartEndDate
		List<StartEndDate> sedList = buildStartEndDateList()
		
		printReportingDates(sedList, pw)

		String query1 = buildStartEndDateQuery(sedList)
		println query1
		println ''

		List<TotalDTO> sedTotalListBeforeAveraging = databaseQueryService.buildTotalsList(query1)
		
		int sedListDays = calculateDays(sedList)
//		println "sedListDays: $sedListDays"
//		println ''
		
		List<TotalDTO> sedTotalList = averageTotalsList(sedTotalListBeforeAveraging, sedListDays)
		totalList.addAll(sedTotalList)

		//SpecificRunningCosts
		String query2 = buildSpecificRunningCostsQuery()
		println query2
		println ''
		List<FinancialDTO> specificRunningCostsFinancialList = databaseQueryService.buildList(query2)
		
		List<TotalDTO> specificRunningCostsTotalList = transformToTotalsList(specificRunningCostsFinancialList)
		totalList.addAll(specificRunningCostsTotalList)

		//OngoingRunningCosts
		String query3 = buildOngoingRunningCostsQuery()
		println query3
		println ''
		List<FinancialDTO> ongoingRunningCostsFinancialList = databaseQueryService.buildList(query3)
		
		List<TotalDTO> ongoingRunningCostsTotalList = transformToTotalsList(ongoingRunningCostsFinancialList)
		totalList.addAll(ongoingRunningCostsTotalList)
		
		Collections.sort(totalList, new TotalDTOAmtComparator())
		
		//Reporting
		report(totalList, pw)
		println ''

		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	def report(List<TotalDTO> totalList, PrintWriter pw) {
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		
		//Total
		BigDecimal reportTotal = BigDecimal.ZERO
			
		totalList.each {dto ->
			reportTotal = reportTotal.add(dto.amount)
			
			String col1 = dto.totalName.padRight(20, ' ')
			String col2 = nf.format(dto.amount).padLeft(12, ' ')
			//pw.println "$col1$col2"
			pw.println "${commonReportingService.buildFixedWidthLabel(dto.totalName, 25)} ${nf.format(dto.amount).padLeft(7, ' ')}"
		}
		
		pw.println ''
		pw.println "Monthly Average: ${nf.format(reportTotal)}"
	}
	
	List<StartEndDate> buildStartEndDateList() {
		List<StartEndDate> sedList = []
		
		StartEndDate sed
		sed = new StartEndDate(startDay : 10, startMonth : Calendar.JULY, startYear : 2020, endDay : 3, endMonth : Calendar.SEPTEMBER, endYear : 2020)
//		println sed.getDB2StartDate()
//		println sed.getDB2EndDate()
//		println sed.getDays()
//		println ''
		sedList << sed
		
		sed = new StartEndDate(startDay : 9, startMonth : Calendar.OCTOBER, startYear : 2020, endDay : 30, endMonth : Calendar.NOVEMBER, endYear : 2020)
//		println sed.getDB2StartDate()
//		println sed.getDB2EndDate()
//		println sed.getDays()
//		println ''
		sedList << sed
		
		return sedList
	}

	def printReportingDates(List<StartEndDate> sedList, PrintWriter pw) {
		SimpleDateFormat sdf = new SimpleDateFormat('EEE, MMM d, yyyy')
		
		pw.println 'Reporting dates:'
		sedList.each {sed ->
			pw.println "\tFrom: ${sdf.format(sed.getStartDate())}\tTo: ${sdf.format(sed.getEndDate())}"
		}
		pw.println ''
	}
	
	int calculateDays(List<StartEndDate> sedList) {
		int days = 0
		
		sedList.each {sed ->
			days += sed.getDays()
		}
		
		return days
	}
	
	List<TotalDTO> averageTotalsList(List<TotalDTO> tl, int sedListDays) {
		List<TotalDTO> totalList = []
		
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		BigDecimal categoryDays = new BigDecimal(sedListDays)
		
		tl.each {t ->
			BigDecimal categoryAverage = t.amount.multiply(daysPerYear).divide(monthsPerYear, 2).divide(categoryDays, 2)
			
			TotalDTO dto = new TotalDTO()
			dto.totalName = t.totalName
			dto.amount = categoryAverage
			totalList << dto
		}
		
		return totalList
	}
	
	List<TotalDTO> transformToTotalsList(List<FinancialDTO> financialList) {
		List<TotalDTO> totalList = []
		
		BigDecimal monthsPerYear = new BigDecimal('12')
		BigDecimal daysPerYear = new BigDecimal('365')
		
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
			
			TotalDTO dto = new TotalDTO()
			dto.totalName = savedCategory
			dto.amount = categoryAverage
			totalList << dto
		}

		return totalList
	}
	
	String buildStartEndDateQuery(List<StartEndDate> sedList) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT CATEGORY as TOTAL_NAME, SUM(AMOUNT) as AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY IN ('ALCOHOL', 'FOOD', 'FUEL', 'CAMPING_FEES', 'ENTERTAINMENT', 'PREPARED_FOOD', 'LAUNDRY', 'CAMPING_SUPPLIES') ")
		sb.append("AND (")
		sb.append("TRANSACTION_DT BETWEEN '")
		sb.append(sedList[0].getDB2StartDate())
		sb.append("' AND '")
		sb.append(sedList[0].getDB2EndDate())
		sb.append("' ")	
		sedList[1..-1].each {sed ->
			sb.append("OR TRANSACTION_DT BETWEEN '")
			sb.append(sed.getDB2StartDate())
			sb.append("' AND '")
			sb.append(sed.getDB2EndDate())
			sb.append("' ")
		}
		sb.append(") GROUP BY CATEGORY ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}
	
	String buildSpecificRunningCostsQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 = 'SPECIFIC_RUNNING_COST' ")
		sb.append("AND CATEGORY <> 'HOUSE_INSURANCE' ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}
	
	String buildOngoingRunningCostsQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT TRANSACTION_DT as TXN, AMOUNT as AMT, PAYEE, DESCRIPTION as DESC, ASSET, CATEGORY as CAT, SUB_CATEGORY as SUBCAT, START_DT as START, END_DT as END, RPT_GRP_1 as RG1, RPT_GRP_2 as RG2, RPT_GRP_3 as RG3 ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE START_DT IS NOT NULL ")
		sb.append("AND RPT_GRP_1 = 'ONGOING_RUNNING_COST' ")
		sb.append("AND CATEGORY in ('DATA_PLAN', 'PHONE_PLAN') ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}
	
}
