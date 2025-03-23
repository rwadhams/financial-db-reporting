package com.wadhams.financials.db.service

import com.wadhams.financials.db.dto.FinancialDTO

class CategoryListService {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	
	List<String> allCategoryList
	List<String> startingCategoryList
	
	List<String> obsoleteCategoryList
	List<String> campHillCategoryList
	List<String> campHillRenoCategoryList
	List<String> fingalCategoryList
	List<String> assetRelatedCostCategoryList
	List<String> runningCostCategoryList
	List<String> dayToDayCategoryList
	List<String> unbudgetedCategoryList
	List<String> otherCategoryList
	
	def CategoryListService() {
		allCategoryList = databaseQueryService.buildAllCategoryList()
		//copy
		startingCategoryList = allCategoryList.collect()
		
		//Obsolete
		obsoleteCategoryList = [
			'CASH',
			'PHONE_AND_DATA_PLAN'
			]
		startingCategoryList -= obsoleteCategoryList
		
		//Camp Hill Reno
		campHillRenoCategoryList = startingCategoryList.findAll {cat ->
			cat.startsWith('CH_RENO_')
		}
		startingCategoryList -= campHillRenoCategoryList
		
		//Camp Hill
		campHillCategoryList = startingCategoryList.findAll {cat ->
			cat.startsWith('CH_')
		}
		startingCategoryList -= campHillCategoryList
		
		//Fingal
		fingalCategoryList = startingCategoryList.findAll {cat ->
			cat.startsWith('FINGAL_')
		}
		startingCategoryList -= fingalCategoryList

		//Asset Relared Cost
		assetRelatedCostCategoryList = [
			'ASSET_RELATED_COST'
			]
		startingCategoryList -= assetRelatedCostCategoryList
		
		//Time Spanned. Items with a start and end date, except FUEL
		runningCostCategoryList = [
			'CARAVAN_INSURANCE',
			'CARAVAN_REGISTRATION',
			'CARAVAN_SERVICING',
			'CAR_INSURANCE',
			'CAR_REGISTRATION',
			'CAR_SERVICING',
			'DATA_PLAN',
			'DRIVERS_LICENSE_MOLLY',
			'DRIVERS_LICENSE_ROB',
			'MEMBERSHIP',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB'
			]
			startingCategoryList -= runningCostCategoryList
			
			//DayToDay
		dayToDayCategoryList = [
			'ALCOHOL',
			'CAMPING_FEES',
			'CAMPING_SUPPLIES',
			'CARAVAN_EQUIPMENT',
			'CARAVAN_SUPPLIES',
			'CLEANING',
			'CLOTHING',
			'CLOUD_STORAGE',
			'DRINKS',
			'ENTERTAINMENT',
			'FOOD',
			'FUEL',
			'GIFTS',
			'LAUNDRY',
			'MEDICAL',
			'OFFICE',
			'PARKING',
			'PHARMACY',
			'PREPARED_FOOD'
			]
			startingCategoryList -= dayToDayCategoryList
			
			//Unbudgeted
			unbudgetedCategoryList = [
				'CAMPING_EQUIPMENT',
				'CAR_EQUIPMENT',
				'CAR_MAINTENANCE',
				'CAR_REPAIR',
				'CARAVAN_MAINTENANCE',
				'CARAVAN_REPAIR',
				'ELECTRONICS',
				'PARKS_PASS',
				'SPECIAL_ACTIVITY',
				'TECHNOLOGY'
				]
			startingCategoryList -= unbudgetedCategoryList
			
			//Other
			otherCategoryList = startingCategoryList
	}
	
	boolean verifyLists() {
//		if (allCategoryList.size() != 78) {
//			println 'allCategoryList is not equal to 78'
//			return false
//		}
		int subListSize = 	 obsoleteCategoryList.size() +
							 campHillRenoCategoryList.size() +
							 campHillCategoryList.size() +
							 fingalCategoryList.size() +
							 assetRelatedCostCategoryList.size() +
							 runningCostCategoryList.size() +
							 dayToDayCategoryList.size() + 
							 unbudgetedCategoryList.size() +
							 otherCategoryList.size()
		if (allCategoryList.size() != subListSize) {
			println "allCategoryList.size(): ${allCategoryList.size()} not equal to subListSize: $subListSize"
			return false
		}
		
		return true
	}
	
	def printLists() {
		List<String> reportLineList
		int numberOfColumns = 4
		
		reportLineList = buildReportLines(allCategoryList, numberOfColumns)
		println 'allCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''

		reportLineList = buildReportLines(obsoleteCategoryList, numberOfColumns)
		println 'obsoleteCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(campHillRenoCategoryList, numberOfColumns)
		println 'campHillRenoCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(campHillCategoryList, numberOfColumns)
		println 'campHillCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(fingalCategoryList, numberOfColumns)
		println 'fingalCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(assetRelatedCostCategoryList, numberOfColumns)
		println 'assetRelatedCostCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(runningCostCategoryList, numberOfColumns)
		println 'runningCostCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(dayToDayCategoryList, numberOfColumns)
		println 'dayToDayCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(unbudgetedCategoryList, numberOfColumns)
		println 'unbudgetedCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(otherCategoryList, numberOfColumns)
		println 'otherCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
	}
	
	def printAllCategories() {
		List<String> reportLineList = buildReportLines(allCategoryList, 1)
		println 'allCategoryList:'
		reportLineList.each {line-> println "\t$line"}
		println ''
	}
	
	List<String> buildReportLines(List<String> categoryList, int numberOfColumns) {
		List<String> reportLineList = []
		
		int maxTextSize = commonReportingService.maxTextSize(categoryList)
		
		int count = 1
		String reportLine = ''
		categoryList.each {cat ->
			String s = cat.padRight(maxTextSize, ' ') + '  '
			reportLine += s
			if (count < numberOfColumns) {
				count++
			}
			else {
				reportLineList << reportLine
				//reset
				reportLine = ''
				count = 1
			}
		}
		//add last line to list
		reportLineList << reportLine
		
		return reportLineList
	}
}
