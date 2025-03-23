package com.wadhams.financials.db.service

import com.wadhams.financials.db.dto.FinancialDTO

class CommonReportingService {
	String horizonalRule
	
	public CommonReportingService() {
		String baseString = ''
		horizonalRule = baseString.padRight(100, '+')
	}

	int maxTextSize(List<String> textList) {
		int maxTextSize = 0
		textList.each {t ->
			if (t.size() > maxTextSize) {
				maxTextSize = t.size()
			}
		}
		return maxTextSize
	}
	
	int maxPayeeSize(List<FinancialDTO> financialList) {
		int maxPayeeSize = 0
		financialList.each {dto ->
			if (dto.payee.size() > maxPayeeSize) {
				maxPayeeSize = dto.payee.size()
			}
		}
		return maxPayeeSize
	}
	
	int maxCategorySize(List<FinancialDTO> financialList) {
		int maxCategorySize = 0
		financialList.each {dto ->
			if (dto.category.size() > maxCategorySize) {
				maxCategorySize = dto.category.size()
			}
		}
		return maxCategorySize
	}
	
	String buildFixedWidthLabel(String label, int width) {
		int padLength = width - label.size()
		
		StringBuilder sb = new StringBuilder()
		sb.append(label)
		padLength.times {
			sb.append('.')
		}
		sb.append(': ')
		
		return sb.toString()
	}
}
