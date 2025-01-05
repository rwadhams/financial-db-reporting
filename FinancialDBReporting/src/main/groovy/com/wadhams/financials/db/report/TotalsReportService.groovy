package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.type.Residence
import groovy.sql.GroovyRowResult

class TotalsReportService {
	DatabaseQueryService databaseQueryService
	CommonReportingService commonReportingService
	CategoryListService categoryListService
	
	NumberFormat nf = NumberFormat.getCurrencyInstance()
	DateTimeFormatter reportDTF = DateTimeFormatter.ofPattern("LLL yyyy")
	
	def execute() {
		String heading
		
		List<Integer> yearList = buildYearRange()
//		println yearList

		List<String> catListMinusAssetCosts = buildCatListMinusAssetCosts()
		List<String> catListLivingCosts = buildCatListLivingCosts()
		
		File f1 = new File("out/totals-yearly-report.txt")
		f1.withPrintWriter {pw ->
			heading = 'YEARLY TOTALS REPORT'
			reportYearly(yearList, [], heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'YEARLY TOTALS MINUS ASSET COSTS REPORT'
			reportYearly(yearList, catListMinusAssetCosts, heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'YEARLY LIVING TOTALS REPORT'
			reportYearly(yearList, catListLivingCosts, heading, pw)
		}
		
		File f2 = new File("out/totals-monthly-report.txt")
		f2.withPrintWriter {pw ->
			heading = 'MONTHLY TOTALS REPORT'
			reportMonthly(yearList, [], heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'MONTHLY TOTALS MINUS ASSET COSTS REPORT'
			reportMonthly(yearList, catListMinusAssetCosts, heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'MONTHLY LIVING TOTALS REPORT'
			reportMonthly(yearList, catListLivingCosts, heading, pw)
		}
	}

	def reportYearly(List<Integer> yearList, List<String> catList, String heading, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		BigDecimal grandTotal = BigDecimal.ZERO

		yearList.each {year ->
			String query = buildYearlyQuery(year, catList)
//			println query
//			println ''

			pw.print "$year: "

			GroovyRowResult grr = databaseQueryService.firstRow(query)
			def amount = grr.getProperty('AMT')
			//println "Amount: $amount"
			if (amount) {
				String formattedAmount = nf.format(amount)
				pw.println formattedAmount.padLeft(12)
				grandTotal += amount
			}
			else {
				pw.println '$0.00'.padLeft(12)
			}
		}
		pw.println ''
		pw.println "Grand Total...: ${nf.format(grandTotal)}"
	}

	def reportMonthly(List<Integer> yearList, List<String> catList, String heading, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		yearList.each {year ->
			12.times {zeroBasedMonth ->
				YearMonth ym = YearMonth.of(year, zeroBasedMonth+1)
				String month = ym.getMonthValue().toString().padLeft(2, '0')
				String startDate = "$year-$month-01"
				String endDate = "$year-$month-${ym.lengthOfMonth()}"

				String query = buildMonthlyQuery(startDate, endDate, catList)
//				println query
//				println ''

				pw.print "${reportDTF.format(ym)}:"
				GroovyRowResult grr = databaseQueryService.firstRow(query)
				def amount = grr.getProperty('AMT')
				//println "Amount: $amount"
				if (amount) {
					String formattedAmount = nf.format(amount)
					pw.println formattedAmount.padLeft(12)
				}
				else {
					pw.println '$0.00'.padLeft(12)
				}
			}
		}
	}

	List<Integer> buildYearRange() {
		int startYear = 2019
		LocalDate ld = LocalDate.now()
		int lastYear = ld.year
		List<Integer> yearList = []
		int year = startYear
		while (year <= lastYear) {
			yearList << year
			year++
		}
		return yearList
	}
	
	List<String> buildCatListMinusAssetCosts() {
		List<String> allCategoryList = categoryListService.allCategoryList
		List<String> startingCategoryList = allCategoryList.collect()	//copy
		startingCategoryList -= categoryListService.obsoleteCategoryList
		startingCategoryList -= categoryListService.assetRelatedCostCategoryList
		startingCategoryList -= ['FINGAL_SHED']
		return startingCategoryList
	}
	
	List<String> buildCatListLivingCosts() {
		List<String> allCategoryList = categoryListService.allCategoryList
		List<String> startingCategoryList = allCategoryList.collect()	//copy
		startingCategoryList -= categoryListService.obsoleteCategoryList
		startingCategoryList -= categoryListService.assetRelatedCostCategoryList
		startingCategoryList -= categoryListService.campHillRenoCategoryList
		startingCategoryList -= categoryListService.campHillCategoryList
		startingCategoryList -= categoryListService.fingalCategoryList
		
		return startingCategoryList
	}
	
	String buildYearlyQuery(Integer year, List<String> catList) {
		String startDate = "$year-01-01"
		String endDate = "$year-12-31"
		
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT >= '")
		sb.append(startDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(endDate)
		sb.append("' ")
		if (catList.size() > 0) {
			StringBuilder sb2 = new StringBuilder()
			sb2.append("'${catList[0]}'")
			catList[1..-1].each {s ->
				sb2.append(", '$s'")
			}
			sb.append("AND CATEGORY IN (${sb2.toString()})")
		}
		
		return sb.toString()
	}
	
	String buildMonthlyQuery(String startDate, String endDate, List<String> catList) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE TRANSACTION_DT >= '")
		sb.append(startDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(endDate)
		sb.append("' ")
		if (catList.size() > 0) {
			StringBuilder sb2 = new StringBuilder()
			sb2.append("'${catList[0]}'")
			catList[1..-1].each {s ->
				sb2.append(", '$s'")
			}
			sb.append("AND CATEGORY IN (${sb2.toString()})")
		}

		return sb.toString()
	}
	
}
