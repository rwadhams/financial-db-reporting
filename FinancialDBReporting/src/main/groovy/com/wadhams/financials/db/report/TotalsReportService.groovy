package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.service.CategoryListService
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService

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

		File f1 = new File("out/totals-yearly-report.txt")
		f1.withPrintWriter {pw ->
			heading = 'YEARLY TOTALS REPORT'
			reportYearly(yearList, [], heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'YEARLY TOTALS REPORT (Non Camping Categories)'
			reportYearly(yearList, categoryListService.nonCampingCategoryList, heading, pw)
			
			pw.println ''
			pw.println commonReportingService.horizonalRule
			pw.println ''
	
			heading = 'YEARLY TOTALS REPORT (Camping Categories)'
			reportYearly(yearList, categoryListService.campingCategoryList, heading, pw)
		}
		
		File f2 = new File("out/totals-monthly-camping-report.txt")
		f2.withPrintWriter {pw ->
			heading = 'MONTHLY TOTALS REPORT (Camping Categories)'
			reportMonthly(yearList, categoryListService.campingCategoryList, heading, pw)
		}
	}

	def printCategories(List<String> catList, PrintWriter pw) {
		if (catList.size() > 0) {
			int maxTextSize = commonReportingService.maxTextSize(catList)
			pw.println 'Categories included:'
			List<String> reportLineList = categoryListService.buildReportLines(catList, maxTextSize, 4)
			reportLineList.each {rl ->
				pw.println "\t$rl"
			}
			pw.println ''
		}
	}

	def reportYearly(List<Integer> yearList, List<String> catList, String heading, PrintWriter pw) {
		pw.println heading
		String u1 = ''.padRight(heading.size(), '-')
		pw.println u1

		//print categories
		printCategories(catList, pw)
//		if (catList.size() > 0) {
//			int maxTextSize = commonReportingService.maxTextSize(catList)
//			pw.println 'Categories included:'
//			List<String> reportLineList = categoryListService.buildReportLines(catList, maxTextSize, 4)
//			reportLineList.each {rl ->
//				pw.println "\t$rl"
//			}
//			pw.println ''
//		}
		
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

		//print categories
		printCategories(catList, pw)

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
					pw.println ''
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
