package com.wadhams.financials.db.report

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO
import com.wadhams.financials.db.service.CommonReportingService
import com.wadhams.financials.db.service.DatabaseQueryService
import com.wadhams.financials.db.type.MonthDateRange

class CategoryByMonthReportService {
	
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	CommonReportingService commonReportingService = new CommonReportingService()
	
	def execute(PrintWriter pw) {
		pw.println 'CATEGORY EXPENSE TOTALS BY MONTH REPORT'
		pw.println '---------------------------------------'

		List<String> blackList = ['4WD', 'CAR_INSURANCE', 'CAR_SERVICING', 'DRIVERS_LICENSE', 'ELECTRIC_UTILITIES', 'FURNITURE', 'GAS_UTILITIES', 'HOUSE_INSURANCE', 'INCOME', 'PURCHASE', 'RACQ_MEMBERSHIP', 'RATES', 'RENO', 'RENTAL_CAR', 'TELSTRA', 'WATER_UTILITIES']
		
		List<String> categoryList = buildCategoryWhiteList(blackList)
		//println categoryList
		
		int maxCategorySize = 0
		categoryList.each {cat ->
			if (cat.size()>maxCategorySize) {
				maxCategorySize = cat.size()
			}
		}
		//println "maxCategorySize: $maxCategorySize"
		maxCategorySize = maxCategorySize + 5	//add margin for report

		List<MonthDateRange> mdrList = [MonthDateRange.Sept2019, MonthDateRange.Oct2019, MonthDateRange.Nov2019, MonthDateRange.Dec2019]
		
		NumberFormat nf = NumberFormat.getCurrencyInstance()
		int maxAmountSize = 12	//used for padding amount
		
		//Print heading
		pw.print 'Categories'.padRight(maxCategorySize)
		mdrList.each {mdr ->
			pw.print mdr.name().padLeft(maxAmountSize)
		}
		pw.println ''

		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		
		categoryList.each {cat ->
			//println cat
			pw.print cat.padRight(maxCategorySize)
			mdrList.each {mdr ->
				String query = buildCategoryMonthQuery(cat, mdr.firstDate, mdr.lastDate)
				//println query
				GroovyRowResult grr = sql.firstRow(query)
				def amount = grr.getProperty('AMT')
				//println "Amount: $amount"
				if (amount) {
					String formattedAmount = nf.format(amount)
					pw.print formattedAmount.padLeft(maxAmountSize)
				}
				else {
					pw.print '$0.00'.padLeft(maxAmountSize)
				}
			}
			pw.println ''
		}

		pw.println ''
		pw.println 'Excluded categories:'
		blackList.each {cat ->
			pw.println "\t$cat"
		}
		
		pw.println ''
		pw.println commonReportingService.horizonalRule
		pw.println ''
	}
	
	List<String> buildCategoryWhiteList(List<String> blackList) {
		List<String> categoryList = []
		
		String query = buildDistinctCategoryQuery()
		//println query
		//println ''
		
		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
		
		sql.eachRow(query) {row ->
			String category = row.CAT
			categoryList << category
		}
		
		return categoryList - blackList
	}
	
	String buildCategoryMonthQuery(String category, String firstDate, String lastDate) {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT SUM(AMOUNT) AS AMT ")
		sb.append("FROM FINANCIAL ")
		sb.append("WHERE CATEGORY = '")
		sb.append(category)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT >= '")
		sb.append(firstDate)
		sb.append("' ")
		sb.append("AND TRANSACTION_DT <= '")
		sb.append(lastDate)
		sb.append("'")

		return sb.toString()
	}
	
	String buildDistinctCategoryQuery() {
		StringBuilder sb = new StringBuilder()
		sb.append("SELECT DISTINCT CATEGORY as CAT ")
		sb.append("FROM FINANCIAL ")
		sb.append("ORDER BY CATEGORY")
		
		return sb.toString()
	}

}
