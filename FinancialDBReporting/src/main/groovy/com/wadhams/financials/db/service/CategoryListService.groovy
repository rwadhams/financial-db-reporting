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
//		campHillRenoCategoryList = [
//			'CH_RENO_COST',
//			'CH_RENO_SERVICES'
//			]
		startingCategoryList -= campHillRenoCategoryList
		
		//Camp Hill
		campHillCategoryList = startingCategoryList.findAll {cat ->
			cat.startsWith('CH_')
		}
//		campHillCategoryList = [
//			'CH_ELECTRIC_UTILITIES',
//			'CH_FURNITURE',
//			'CH_GAS_UTILITIES',
//			'CH_HOUSEWARES',
//			'CH_INSURANCE',
//			'CH_MAINTENANCE',
//			'CH_RATES',
//			'CH_SUPPLIES',
//			'CH_WATER_UTILITIES'
//			]
		startingCategoryList -= campHillCategoryList
		
		//Fingal
		fingalCategoryList = startingCategoryList.findAll {cat ->
			cat.startsWith('FINGAL_')
		}
//		fingalCategoryList = [
//			'FINGAL_EQUIPMENT',
//			'FINGAL_PLANNING',
//			'FINGAL_SUPPLIES',
//			'FINGAL_WORK'
//			]
		startingCategoryList -= fingalCategoryList

		//Asset Relared Cost
		assetRelatedCostCategoryList = [
			'ASSET_RELATED_COST'
			]
		startingCategoryList -= assetRelatedCostCategoryList
		
		//Time Spanned. Items with a start and end date
		runningCostCategoryList = [
			'CARAVAN_INSURANCE',
			'CARAVAN_REGISTRATION',
			'CARAVAN_SERVICING',
			'CARAVAN_TYRES',
			'CAR_INSURANCE',
			'CAR_REGISTRATION',
			'CAR_SERVICING',
			'CAR_TYRES',
			'DATA_PLAN',
			'DRIVERS_LICENSE_MOLLY',
			'DRIVERS_LICENSE_ROB',
			'MEMBERSHIP',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB',
			'TRANSMISSION_SERVICING'
			]
			startingCategoryList -= runningCostCategoryList
			
			//DayToDay
			dayToDayCategoryList = [
				'ALCOHOL',
				'CAMPING_FEES',
				'CAMPING_SUPPLIES',
				'CARAVAN_EQUIPMENT',
				'CARAVAN_SUPPLIES',
				'CAR_SUPPLIES',
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
				'ACCOMODATION',
				'ACCOUNTING_FEES',
				'BASS_STRAIT_FERRY',
				'CARAVAN_STORAGE',
				'DOMESTIC_TRAVEL',
				'ELECTRONICS',
				'MAJOR_EQUIPMENT',
				'MAJOR_WORK',
				'OVERSEAS_TRAVEL',
				'RENTAL_CAR'
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
//		println 'allCategoryList:'
//		allCategoryList.each {cat-> println "\t$cat"}
//		println ''
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
	}
}
