package com.wadhams.financials.db.report

import com.wadhams.financials.db.service.SQLBuilderService

import groovy.sql.Sql

class DataValueReportingService {
	SQLBuilderService sqlBuilderService = new SQLBuilderService()
	
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
		String payeeCountTwoPlusQuery = sqlBuilderService.buildDistinctPayeeCountTwoPlus()
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
		multiColumnPrinting(payeeCountTwoPlusList, 12, pw)
		pw.println ''
		
		
		//DistinctPayeeCountOne
		String payeeCountOneQuery = sqlBuilderService.buildDistinctPayeeCountOne()
//		println payeeCountOneQuery
//		println ''
		
		List<String> payeeCountOneList = []
		sql.eachRow(payeeCountOneQuery) {row ->
			String c01 = row.PAYEE
			payeeCountOneList << "$c01".padRight(30)
		}
		
		pw.println 'Distinct Payee Values (Count = 1)'
		pw.println '---------------------------------'
		multiColumnPrinting(payeeCountOneList, 12, pw)
		pw.println ''
		
		
		//DistinctCategorySubCategorySelect
		String categorySubCategoryQuery = sqlBuilderService.buildDistinctCategorySubCategorySelect()
//		println categorySubCategoryQuery
//		println ''
		
		List<String> catSubcatList = []
		sql.eachRow(categorySubCategoryQuery) {row ->
			String c01 = row.CAT
			String c02 = row.SUBCAT
			if (!c02) {
				catSubcatList << "$c01".padRight(30)
			}
			else {
				catSubcatList << "$c01 / $c02".padRight(30)
			}
		}
		
		pw.println 'Distinct Category/SubCategory Values'
		pw.println '------------------------------------'
		multiColumnPrinting(catSubcatList, 25, pw)
		pw.println ''
		
		
		//AssetSelectWithoutReportGrouping
		String assetQuery = sqlBuilderService.buildAssetSelectWithoutReportGrouping()
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
		String specificRunningCostQuery = sqlBuilderService.buildSpecificRunningCostSelect()
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
		String ongoingRunningCostQuery = sqlBuilderService.buildOngoingRunningCostSelect()
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
		String reportGrouping1Query = sqlBuilderService.buildDistinctReportGrouping1Select()
//		println reportGrouping1Query
//		println ''
		pw.println 'Distinct Report Grouping 1'
		pw.println '--------------------------'
		sql.eachRow(reportGrouping1Query) {row ->
			String c01 = row.RG1
			pw.println c01
		}
		pw.println ''
		
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
}
