package com.wadhams.financials.db.dto


import groovy.transform.ToString

@ToString(includeNames=true)
class FinancialDTO {
	Date transactionDt
	BigDecimal amount
	String description
	String payee
	
	String asset
	String category
	String subCategory
	Date startDt
	Date endDt
}
