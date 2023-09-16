package com.wadhams.financials.db.app

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.report.DataValueReportingService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

class DatabaseAnalysisReportingApp {
	Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')
	DateTimeFormatter h2dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	static main(args) {
		println 'DatabaseAnalysisReportingApp started...'
		println ''

		DatabaseAnalysisReportingApp app = new DatabaseAnalysisReportingApp()
		app.execute()
		
		println 'DatabaseAnalysisReportingApp ended.'
	}
	
	def execute() {
		generalAnaylsis()
		
//		research01()
//		research02()
//		research03()
//		research04()
//		categoryAmountsByList()
//		categoryCountsByList()
	}
	
	def research01() {
		String q1 = "select transaction_dt as TXN, amount as AMT, description as DESC from financial where amount > 400 and category = 'CARAVAN_EQUIPMENT' order by 1"
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		BigDecimal total = BigDecimal.ZERO
		println 'CARAVAN_EQUIPMENT items with > $400'
		sql.eachRow(q1) {row ->
			Date txn = row.TXN
			BigDecimal amt = row.AMT
			String desc = row.DESC
			println "$txn $amt\t$desc"
			total += amt
		}
		println ''
		println "Total: $total"
		println ''
	}
	
	def research02() {
		String q1 = "select transaction_dt as TXN, amount as AMT, description as DESC, category AS CAT from financial where amount > 400 order by 4,1"
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		println 'Items with > $400'
		sql.eachRow(q1) {row ->
			Date txn = row.TXN
			BigDecimal amt = row.AMT
			String desc = row.DESC
			String cat = row.CAT
			println "$txn $amt\t$cat\t\t$desc"
		}
		println ''
	}
	
	def research03() {
	}
	
	def research04() {
		String q1 = 'select asset as ASSET, category as CAT, count(*) as COUNT from financial where asset is not null group by asset,category order by 1,2'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		sql.eachRow(q1) {row ->
			String asset = row.ASSET
			String category = row.CAT
			String count = row.COUNT
			println "$asset $category $count"
		}
		println ''
	}
	
	def generalAnaylsis() {
		allCategories()
		categoryCounts()
		categoryAmounts()
		assets()
		reportGroupOne()
		reportGroupTwo()
		reportGroupThree()
		categoryStartEndDates()
		categoryRunningCosts()
		allSubCategories()
	}
	
	def allCategories() {
		String q1 = 'select distinct category as CAT from financial order by 1'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Category"
		String u1 = '--------'
		println h1
		println u1
		int categoryCount = 0
		sql.eachRow(q1) {row ->
			String category = row.CAT
			println "$category"
			categoryCount++
		}
		println ''
		println "${categoryCount} distinct categories"
		println ''
	}
	
	def allSubCategories() {
		String q1 = 'select distinct sub_category as SUBCAT from financial where sub_category is not null order by 1'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "SubCategory"
		String u1 = '-----------'
		println h1
		println u1
		int subCategoryCount = 0
		sql.eachRow(q1) {row ->
			String subCategory = row.SUBCAT
			println "$subCategory"
			subCategoryCount++
		}
		println ''
		println "${subCategoryCount} distinct subCategories"
		println ''
	}
	
	def categoryRunningCosts() {
		String q1 = "SELECT distinct category as CAT, rpt_grp_1 as RG1 FROM FINANCIAL where rpt_grp_1 in ('ONGOING_RUNNING_COST','SPECIFIC_RUNNING_COST') order by 1"
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1

		println ''		
		println 'Categories assocoated with ongoing and specific running costs. Category MUST NOT repeat.'
		println ''		
		String h1 = "${'Category'.padRight(25, ' ')} Report Grouping 1"
		String u1 = '------------------------- ---------------------'
		println h1
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			String rg1 = row.RG1
			println "${category.padRight(25, ' ')} $rg1"
		}
		println ''
	}
	
	def categoryStartEndDates() {
		String q1 = 'select distinct category as CAT from financial where start_dt is not null and end_dt is not null order by 1'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Category"
		String u1 = '--------'
		println h1
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			println "$category"
		}
		println ''
	}
	
	def reportGroupOne() {
		String q1 = 'select distinct rpt_grp_1 as RG1 from financial where rpt_grp_1 is not null order by 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		List<String> rg1List = []
		sql.eachRow(q1) {row ->
			String rg1 = row.RG1
			rg1List << rg1
		}

		String h1 = "Distinct Report Group One"
		String u1 = '-------------------------'
		println h1
		println u1
		rg1List.each {rg1 ->
			println rg1
		}
		println ''
		
		rg1List.each {rg1 ->
			String q2 = "SELECT distinct category as CAT FROM FINANCIAL where rpt_grp_1 = '$rg1'"
			String uq2 = ''.padRight(q1.size(), '-')
			println uq2
			println q2
			println uq2
			println "$rg1"
			sql.eachRow(q2) {row ->
				String category = row.CAT
				println "    $category"
			}
			println ''
		}
		println ''
	}
	
	def reportGroupTwo() {
		String q1 = 'select distinct rpt_grp_2 as RG2 from financial where rpt_grp_2 is not null order by 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		List<String> rg2List = []
		sql.eachRow(q1) {row ->
			String rg2 = row.RG2
			rg2List << rg2
		}

		String h1 = "Distinct Report Group Two"
		String u1 = '-------------------------'
		println h1
		println u1
		rg2List.each {rg2 ->
			println rg2
		}
		println ''
	}
	
	def reportGroupThree() {
		String q1 = 'select distinct rpt_grp_3 as RG3 from financial where rpt_grp_3 is not null order by 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		List<String> rg3List = []
		sql.eachRow(q1) {row ->
			String rg3 = row.RG3
			rg3List << rg3
		}

		String h1 = "Distinct Report Group Three"
		String u1 = '---------------------------'
		println h1
		println u1
		rg3List.each {rg3 ->
			println rg3
		}
		println ''
	}
	
	def assets() {
		String q1 = 'select distinct asset as ASSET from financial where asset is not null order by 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		List<String> assetList = []
		sql.eachRow(q1) {row ->
			String asset = row.ASSET
			assetList << asset
		}

		String h1 = "Distinct Assets"
		String u1 = '---------------'
		println h1
		println u1
		assetList.each {asset ->
			println asset
		}
		println ''
		
		assetList.each {asset ->
			String q2 = "SELECT distinct category as CAT FROM FINANCIAL where asset = '$asset'"
			String uq2 = ''.padRight(q1.size(), '-')
			println uq2
			println q2
			println uq2
			println "$asset"
			sql.eachRow(q2) {row ->
				String category = row.CAT
				println "    $category"
			}
			println ''
		}
		println ''
	}
	
	def categoryAmountsByList() {
		List<String> catList = [
			'ACCOUNTING_FEES',
			'BANKING_FEES',
			'CAMPING_EQUIPMENT',
			'CARAVAN_REPAIR',
			'CAR_EQUIPMENT',
			'CAR_REPAIR',
			'FERRY',
			'FISHING',
			'HOME_BREW',
			'MEDIA',
			'MISC',
			'PARKS_PASS',
			'PERSONAL_GROOMING',
			'SAFETY',
			'TECHNOLOGY',
			'TOLLS',
			'TOOLS',
			'TRANSIT',
			'TRAVEL_PUBLICATION'
			]
		StringBuilder sb = new StringBuilder()
		sb.append("'${catList[0]}'")
		catList[1..-1].each {s ->
			sb.append(", '$s'")
		}
		String q1 = "select category as CAT, sum(amount) as AMT from financial where category in (${sb.toString()}) group by category order by 2 desc, 1 asc"
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Total Amount Category"
		String u1 = '------------ --------'
		println h1
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			BigDecimal amount = row.AMT
			println "${cf.format(amount).padRight(12, ' ')} $category"
		}
		println ''
	}
	
	def categoryAmounts() {
		String q1 = 'select category as CAT, sum(amount) as AMT from financial group by category order by 2 desc, 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Total Amount Category"
		String u1 = '------------ --------'
		println h1
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			BigDecimal amount = row.AMT
			println "${cf.format(amount).padRight(12, ' ')} $category"
		}
		println ''
	}
	
	def categoryCountsByList() {
		List<String> catList = [
			'ACCOUNTING_FEES',
			'BANKING_FEES',
			'CAMPING_EQUIPMENT',
			'CARAVAN_REPAIR',
			'CAR_EQUIPMENT',
			'CAR_REPAIR',
			'FERRY',
			'FISHING',
			'HOME_BREW',
			'MEDIA',
			'MISC',
			'PARKS_PASS',
			'PERSONAL_GROOMING',
			'SAFETY',
			'TECHNOLOGY',
			'TOLLS',
			'TOOLS',
			'TRANSIT',
			'TRAVEL_PUBLICATION'
			]
		StringBuilder sb = new StringBuilder()
		sb.append("'${catList[0]}'")
		catList[1..-1].each {s ->
			sb.append(", '$s'")
		}
		String q1 = "select category as CAT, count(*) as COUNT from financial where category in (${sb.toString()}) group by category order by 2 desc, 1 asc"
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Txn"
		String h2 = "Count Category"
		String u1 = '----- --------'
		println h1
		println h2
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			String count = row.COUNT
			println "${count.padRight(5, ' ')} $category"
		}
		println ''
	}
	
	def categoryCounts() {
		String q1 = 'select category as CAT, count(*) as COUNT from financial group by category order by 2 desc, 1 asc'
		String uq1 = ''.padRight(q1.size(), '-')
		println uq1
		println q1
		println uq1
		
		String h1 = "Txn"
		String h2 = "Count Category"
		String u1 = '----- --------'
		println h1
		println h2
		println u1
		sql.eachRow(q1) {row ->
			String category = row.CAT
			String count = row.COUNT
			println "${count.padRight(5, ' ')} $category"
		}
		println ''
	}
}
