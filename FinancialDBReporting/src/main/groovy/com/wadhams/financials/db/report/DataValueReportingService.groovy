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
}
