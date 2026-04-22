package com.wadhams.financials.db.comparator

import com.wadhams.financials.db.dto.CategoryCountDTO

class CategoryCountDTOComparator implements Comparator<CategoryCountDTO> {

	@Override
	public int compare(CategoryCountDTO dto1, CategoryCountDTO dto2) {
		return dto1.count.compareTo(dto2.count) * -1	//descending
	}
}
