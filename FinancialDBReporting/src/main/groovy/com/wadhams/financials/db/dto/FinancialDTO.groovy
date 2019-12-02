package com.wadhams.financials.db.dto


import groovy.transform.ToString

@ToString(includeNames=true)
class FinancialDTO {
	Date transactionDt
	BigDecimal amount
	String payee
	String description
	
	String asset
	String category
	String subCategory
	Date startDt
	Date endDt
}
