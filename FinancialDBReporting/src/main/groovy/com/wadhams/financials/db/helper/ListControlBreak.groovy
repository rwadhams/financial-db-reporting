package com.wadhams.financials.db.helper

import com.wadhams.financials.db.dto.FinancialDTO

class ListControlBreak {
	List<FinancialDTO> dtoList
	int index = 0
	boolean more = true
	
	public ListControlBreak(List<FinancialDTO> dtoList) {
		super()
		this.dtoList = dtoList
	}

	public FinancialDTO first() {
		index = 0

		if (index < dtoList.size()) {
			return dtoList.get(index++)
		}
	
		//empty list	
		more = false
		return null
	}
	
	public FinancialDTO next() {
		if (index < dtoList.size()) {
			return dtoList.get(index++)
		}
		
		more = false
		return null
	}
	
	public boolean hasMore() {
		return more
	}
	
	//package level for testing
	int numberOfFinancialDTOResults() {
		return dtoList.size()
	}
}
