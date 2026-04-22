package com.wadhams.financials.db.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class CategoryCountDTO {
	String category
	int count
}
