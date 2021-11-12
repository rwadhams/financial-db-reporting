package com.wadhams.financials.db.dto


import groovy.transform.ToString
import java.time.LocalDate

@ToString(includeNames=true)
class FinancialDTO {
	LocalDate transactionDt
	BigDecimal amount
	String payee
	String description
	
	String asset
	String category
	String subCategory
	LocalDate startDt
	LocalDate endDt
	String rg1
	String rg2
	String rg3
}
