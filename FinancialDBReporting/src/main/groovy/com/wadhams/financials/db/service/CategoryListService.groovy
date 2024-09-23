package com.wadhams.financials.db.service

import com.wadhams.financials.db.dto.FinancialDTO

class CategoryListService {
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
		println 'allCategoryList:'
		allCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'obsoleteCategoryList:'
		obsoleteCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'campHillRenoCategoryList:'
		campHillRenoCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'campHillCategoryList:'
		campHillCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'fingalCategoryList:'
		fingalCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'assetRelatedCostCategoryList:'
		assetRelatedCostCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'runningCostCategoryList:'
		runningCostCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'dayToDayCategoryList:'
		dayToDayCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'unbudgetedCategoryList:'
		unbudgetedCategoryList.each {cat-> println "\t$cat"}
		println ''
		println 'otherCategoryList:'
		otherCategoryList.each {cat-> println "\t$cat"}
		println ''
	}
}
