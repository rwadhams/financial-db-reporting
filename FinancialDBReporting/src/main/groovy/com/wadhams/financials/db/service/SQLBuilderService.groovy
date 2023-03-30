package com.wadhams.financials.db.service

import java.time.format.DateTimeFormatter

class SQLBuilderService {
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
	
	String buildDistinctCategorySubCategorySelect() {
		StringBuilder sb = new StringBuilder()
		
		sb.append('SELECT DISTINCT CATEGORY AS CAT, SUB_CATEGORY AS SUBCAT ')
		sb.append('FROM FINANCIAL ')
		sb.append('ORDER BY 1,2')
		
		return sb.toString()
	}
	
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
	
}
