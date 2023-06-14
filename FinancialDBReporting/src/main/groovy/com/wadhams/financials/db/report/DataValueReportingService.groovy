package com.wadhams.financials.db.report

import groovy.sql.Sql

class DataValueReportingService {
	
	def execute() {
		File f = new File("out/data-value-report.txt")
		
		f.withPrintWriter {pw ->
			//report headings
			pw.println 'Data Value Report'
			pw.println '================='
			pw.println ''
			
			reportDistinctValues(pw)
		}
	}
	
	def reportDistinctValues(PrintWriter pw) {
		Sql sql = Sql.newInstance('jdbc:h2:~/financial', 'sa', '', 'org.h2.Driver')

		//DistinctPayeeCountTwoPlus
		String payeeCountTwoPlusQuery = buildDistinctPayeeCountTwoPlus()
//		println payeeCountTwoPlusQuery
//		println ''
		
		List<String> payeeCountTwoPlusList = []
		sql.eachRow(payeeCountTwoPlusQuery) {row ->
			String c01 = row.COUNT
			String c02 = row.PAYEE
			payeeCountTwoPlusList << "$c02 ($c01)".padRight(30)
		}

		pw.println 'Distinct Payee Values (Count > 1)'
		pw.println '---------------------------------'
		multiColumnPrinting(payeeCountTwoPlusList, 25, pw)
		pw.println ''
		
		
		//DistinctPayeeCountOne
		String payeeCountOneQuery = buildDistinctPayeeCountOne()
//		println payeeCountOneQuery
//		println ''
		
		List<String> payeeCountOneList = []
		sql.eachRow(payeeCountOneQuery) {row ->
			String c01 = row.PAYEE
			payeeCountOneList << "$c01".padRight(30)
		}
		
		pw.println 'Distinct Payee Values (Count = 1)'
		pw.println '---------------------------------'
		multiColumnPrinting(payeeCountOneList, 15, pw)
		pw.println ''
		
		
		//DistinctCategorySelect
		String categoryQuery = buildDistinctCategorySelect()
//		println categoryQuery
//		println ''
		
		List<String> catList = []
		sql.eachRow(categoryQuery) {row ->
			String c01 = row.CAT
			catList << "$c01".padRight(30)
		}
		
		pw.println 'Distinct Category Values'
		pw.println '------------------------'
		multiColumnPrinting(catList, 24, pw)
		pw.println ''
		
/*		
		//AssetSelectWithoutReportGrouping
		String assetQuery = buildAssetSelectWithoutReportGrouping()
//		println assetQuery
//		println ''
		pw.println 'Asset Category SubCategory (not part of report grouping)'
		pw.println '--------------------------------------------------------'
		sql.eachRow(assetQuery) {row ->
			String c01 = row.PAYEE
			String c02 = row.DESC
			String c03 = row.ASSET
			String c04 = row.CAT
			String c05 = row.SUBCAT
			pw.println "$c03|$c04|$c05\t\t$c01, $c02"
		}
		pw.println ''

		
		//SpecificRunningCostSelect
		String specificRunningCostQuery = buildSpecificRunningCostSelect()
//		println specificRunningCostQuery
//		println ''
		pw.println "Specific Running Costs (Asset|Category)\t\t<rg1>SPECIFIC_RUNNING_COST</rg1>"
		pw.println '---------------------------------------'
		sql.eachRow(specificRunningCostQuery) {row ->
			String c01 = row.PAYEE
			String c02 = row.DESC
			String c03 = row.ASSET
			String c04 = row.CAT
			String c05 = row.START
			String c06 = row.END
			pw.println "$c03|$c04|$c05|$c06\t\t$c01, $c02"
		}
		pw.println ''

		
		//OngoingRunningCostSelect
		String ongoingRunningCostQuery = buildOngoingRunningCostSelect()
//		println ongoingRunningCostQuery
//		println ''
		pw.println "Ongoing Running Costs (Asset|Category)\t\t<rg1>ONGOING_RUNNING_COST</rg1>"
		pw.println '--------------------------------------'
		sql.eachRow(ongoingRunningCostQuery) {row ->
			String c01 = row.PAYEE
			String c02 = row.DESC
			String c03 = row.ASSET
			String c04 = row.CAT
			String c05 = row.START
			String c06 = row.END
			pw.println "$c03|$c04|$c05|$c06\t\t$c01, $c02"
		}
		pw.println ''

		
		//DistinctReportGrouping1Select
		String reportGrouping1Query = buildDistinctReportGrouping1Select()
//		println reportGrouping1Query
//		println ''
		pw.println 'Distinct Report Grouping 1'
		pw.println '--------------------------'
		sql.eachRow(reportGrouping1Query) {row ->
			String c01 = row.RG1
			pw.println c01
		}
		pw.println ''
*/		
	}
	
	def multiColumnPrinting(List<String> list, int height, PrintWriter pw) {
		int total = list.size()
		int prev = total / height
		int columns = prev + 1
		height.times {row ->
			prev.times {col ->
				pw.print "${list[col*height+row]}"
			}
			if (height*prev+row >= total) {
				pw.println ''
			}
			else {
				pw.println "${list[prev*height+row]}"
			}
		}
	}
	
	String buildDistinctPayeeCountOne() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT PAYEE AS PAYEE ')
		sb.append('FROM FINANCIAL ')
		sb.append('GROUP BY PAYEE ')
		sb.append('HAVING COUNT(PAYEE) = 1 ')
		sb.append('ORDER BY 1')
		
		return sb.toString()
	}
	
	String buildDistinctPayeeCountTwoPlus() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT PAYEE AS PAYEE, COUNT(PAYEE) AS COUNT ')
		sb.append('FROM FINANCIAL ')
		sb.append('GROUP BY PAYEE ')
		sb.append('HAVING COUNT(PAYEE) > 1 ')
		sb.append('ORDER BY 1')
		
		return sb.toString()
	}
	
	String buildDistinctCategorySelect() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT DISTINCT CATEGORY AS CAT ')
		sb.append('FROM FINANCIAL ')
		sb.append('ORDER BY 1')
		
		return sb.toString()
	}

/*		
	String buildDistinctReportGrouping1Select() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT DISTINCT RPT_GRP_1 AS RG1 ')
		sb.append('FROM FINANCIAL ')
		sb.append('WHERE RPT_GRP_1 IS NOT NULL ')
		sb.append('ORDER BY 1')
		
		return sb.toString()
	}
	
	String buildAssetSelectWithoutReportGrouping() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT PAYEE AS PAYEE, DESCRIPTION AS DESC, ASSET AS ASSET, CATEGORY AS CAT, SUB_CATEGORY AS SUBCAT ')
		sb.append('FROM FINANCIAL ')
		sb.append('WHERE ASSET IS NOT NULL ')
		sb.append('AND RPT_GRP_1 IS NULL ')
		sb.append('ORDER BY ASSET, CATEGORY, SUB_CATEGORY')
		
		return sb.toString()
	}
	
	String buildSpecificRunningCostSelect() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT PAYEE AS PAYEE, DESCRIPTION AS DESC, ASSET AS ASSET, CATEGORY AS CAT, START_DT AS START, END_DT AS END ')
		sb.append('FROM FINANCIAL ')
		sb.append('WHERE RPT_GRP_1 = \'SPECIFIC_RUNNING_COST\' ')
		sb.append('ORDER BY ASSET, CATEGORY, START_DT')
		
		return sb.toString()
	}
	
	String buildOngoingRunningCostSelect() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT PAYEE AS PAYEE, DESCRIPTION AS DESC, ASSET AS ASSET, CATEGORY AS CAT, START_DT AS START, END_DT AS END ')
		sb.append('FROM FINANCIAL ')
		sb.append('WHERE RPT_GRP_1 = \'ONGOING_RUNNING_COST\' ')
		sb.append('ORDER BY ASSET, CATEGORY, START_DT')
		
		return sb.toString()
	}
*/	
}
