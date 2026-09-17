package com.wadhams.financials.db.type

enum SQLOrdering {
	Ascending('ASC'),
	Decsending('DESC');
	
	private final String sqlText

	SQLOrdering(String sqlText) {
		this.sqlText = sqlText
	}

	public String getSqlText() {
		return sqlText;
	}
	
}
