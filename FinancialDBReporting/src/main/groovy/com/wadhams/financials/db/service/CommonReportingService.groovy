package com.wadhams.financials.db.service

import java.text.NumberFormat
import java.text.SimpleDateFormat
import groovy.sql.Sql

import com.wadhams.financials.db.dto.FinancialDTO

class CommonReportingService {
	String horizonalRule
	
	public CommonReportingService() {
		String baseString = ''
		horizonalRule = baseString.padRight(100, '+')
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
