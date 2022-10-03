package com.wadhams.financials.db.comparator

import com.wadhams.financials.db.dto.TotalDTO

class TotalDTOAmtComparator implements Comparator<TotalDTO> {

	@Override
	public int compare(TotalDTO dto1, TotalDTO dto2) {
		return dto1.getTotalAmount().compareTo(dto2.getTotalAmount()) * -1	//descending
	}
}
