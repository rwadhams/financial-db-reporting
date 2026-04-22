package com.wadhams.financials.db.service

import com.wadhams.financials.db.common.type.Category
import com.wadhams.financials.db.comparator.CategoryCountDTOComparator
import com.wadhams.financials.db.dto.CategoryCountDTO
import groovy.sql.GroovyRowResult

class CategoryListService {
	CommonReportingService commonReportingService = new CommonReportingService()
	DatabaseQueryService databaseQueryService = new DatabaseQueryService()
	
	List<String> commonCategoryList
	
	List<String> allCategoryList
	List<String> startingCategoryList
	
	List<String> obsoleteCategoryList
	List<String> assetRelatedCostCategoryList
	List<String> campHillCategoryList
	List<String> campHillRenoCategoryList
	List<String> fingalCategoryList
	List<String> runningCostCategoryList
	List<String> dayToDayCategoryList
	List<String> unbudgetedCategoryList
	List<String> otherCategoryList

	List<String> nonAssetRelatedCostCategoryList
	
	List<String> campingCategoryList
	List<String> nonCampingCategoryList
	
	def CategoryListService() {
		//Category enum
		commonCategoryList = Category.buildCategoryList()
	
		allCategoryList = databaseQueryService.buildAllCategoryList()
		//copy
		startingCategoryList = allCategoryList.collect()
		
		//Obsolete
		obsoleteCategoryList = [
			'PHONE_AND_DATA_PLAN'
			]
		startingCategoryList -= obsoleteCategoryList
		
		//Asset Relared Cost
		assetRelatedCostCategoryList = [
			'CARAVAN_PURCHASE',
			'CAR_PURCHASE',
			'CH_SALE',
			'FINGAL_LAND',
			'FINGAL_SHED',
			'FINGAL_STUDIO',
			'KK_PURCHASE'
			]
		startingCategoryList -= assetRelatedCostCategoryList
		
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
			'KK_INSURANCE',
			'KK_REGISTRATION',
			'KK_SERVICING',
			'MEMBERSHIP',
			'PHONE_PLAN_MOLLY',
			'PHONE_PLAN_ROB',
			'STAR_LINK_DATA'
			]
			startingCategoryList -= runningCostCategoryList
			
			//DayToDay
		dayToDayCategoryList = [
			'ALCOHOL',
			'CAMPING_FEES',
			'CAMPING_SUPPLIES',
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
				'CARAVAN_EQUIPMENT',
				'CARAVAN_MAINTENANCE',
				'CARAVAN_REPAIR',
				'ELECTRONICS',
				'KK_EQUIPMENT',
				'PARKS_PASS',
				'SPECIAL_ACTIVITY',
				'TECHNOLOGY'
				]
			startingCategoryList -= unbudgetedCategoryList
			
			//Other
			otherCategoryList = startingCategoryList
			
			//copy for nonAssetRelatedCostCategoryList
			nonAssetRelatedCostCategoryList = allCategoryList.collect()
			nonAssetRelatedCostCategoryList -= assetRelatedCostCategoryList
			
			//Camping and NonCamping
			campingCategoryList = allCategoryList.collect()		//copy
			nonCampingCategoryList = allCategoryList.collect()	//copy
			
			campingCategoryList -= obsoleteCategoryList
			campingCategoryList -= assetRelatedCostCategoryList
			campingCategoryList -= campHillCategoryList
			campingCategoryList -= campHillRenoCategoryList
			campingCategoryList -= fingalCategoryList
			
			nonCampingCategoryList -= campingCategoryList
	}
	
	boolean verifyLists() {
		//verify no duplicates
		assert obsoleteCategoryList == obsoleteCategoryList.unique(false)
		assert assetRelatedCostCategoryList == assetRelatedCostCategoryList.unique(false)
		assert campHillRenoCategoryList == campHillRenoCategoryList.unique(false)
		assert campHillCategoryList == campHillCategoryList.unique(false)
		assert fingalCategoryList == fingalCategoryList.unique(false)
		assert runningCostCategoryList == runningCostCategoryList.unique(false)
		assert dayToDayCategoryList == dayToDayCategoryList.unique(false)
		assert unbudgetedCategoryList == unbudgetedCategoryList.unique(false)
		assert otherCategoryList == otherCategoryList.unique(false)
		
		//verify total size
		int subListSizeTotal = 	 obsoleteCategoryList.size() +
								 assetRelatedCostCategoryList.size() +
								 campHillRenoCategoryList.size() +
								 campHillCategoryList.size() +
								 fingalCategoryList.size() +
								 runningCostCategoryList.size() +
								 dayToDayCategoryList.size() + 
								 unbudgetedCategoryList.size() +
								 otherCategoryList.size()
		if (allCategoryList.size() != subListSizeTotal) {
			println "allCategoryList.size(): ${allCategoryList.size()} not equal to subListSizeTotal: $subListSizeTotal"
			return false
		}
		
		return true
	}
	
	def printCommonCategoryDetails() {
		println "commonCategoryList size: ${commonCategoryList.size()}"
		println ''
		List<String> diffCategoryList = commonCategoryList.collect()	//copy
		diffCategoryList -= allCategoryList
		println "Common categories not currently used (${diffCategoryList.size()}):"
		diffCategoryList.each {cat-> println "\t$cat"}
		println ''
	}
	
	def printLists() {
		List<String> reportLineList
		int numberOfColumns = 4
		
		int maxTextSize = commonReportingService.maxTextSize(allCategoryList)	//use same width for all categories
		
		reportLineList = buildReportLines(allCategoryList, maxTextSize, numberOfColumns)
		println "allCategoryList (${allCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''

		reportLineList = buildReportLines(obsoleteCategoryList, maxTextSize, numberOfColumns)
		println "obsoleteCategoryList (${obsoleteCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(assetRelatedCostCategoryList, maxTextSize, numberOfColumns)
		println "assetRelatedCostCategoryList (${assetRelatedCostCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(campHillRenoCategoryList, maxTextSize, numberOfColumns)
		println "campHillRenoCategoryList (${campHillRenoCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(campHillCategoryList, maxTextSize, numberOfColumns)
		println "campHillCategoryList (${campHillCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(fingalCategoryList, maxTextSize, numberOfColumns)
		println "fingalCategoryList (${fingalCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(runningCostCategoryList, maxTextSize, numberOfColumns)
		println "runningCostCategoryList (${runningCostCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(dayToDayCategoryList, maxTextSize, numberOfColumns)
		println "dayToDayCategoryList (${dayToDayCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(unbudgetedCategoryList, maxTextSize, numberOfColumns)
		println "unbudgetedCategoryList (${unbudgetedCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(otherCategoryList, maxTextSize, numberOfColumns)
		println "otherCategoryList (${otherCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
		
		reportLineList = buildReportLines(nonAssetRelatedCostCategoryList, maxTextSize, numberOfColumns)
		println "nonAssetRelatedCostCategoryList (${nonAssetRelatedCostCategoryList.size()}):"
		reportLineList.each {line-> println "\t$line"}
		println ''
	}
	
	def printCommonCategoriesWithCounts() {
		List<CategoryCountDTO> categoryCountDTOList = []
		
		commonCategoryList.each {cat->
			String query = "select count(*) as COUNT from financial where category = '$cat'"
//			println query
			GroovyRowResult grr = databaseQueryService.firstRow(query)
			def count = grr.getProperty('COUNT')
//			println "Count: $count"
			if (count) {
				categoryCountDTOList << new CategoryCountDTO(category: cat, count: count)
			}
			else {
				categoryCountDTOList << new CategoryCountDTO(category: cat, count: 0)
			}
		}
		
		println "commonCategoryList sorted by category (${commonCategoryList.size()}) with counts:"
		categoryCountDTOList.each {dto->
			String s = dto.count as String
			println "\t${s.padLeft(4)}  ${dto.category}"
		}
		println ''

		categoryCountDTOList.sort(new CategoryCountDTOComparator())
		println "commonCategoryList sorted by count (${commonCategoryList.size()}) with counts:"
		categoryCountDTOList.each {dto->
			String s = dto.count as String
			println "\t${s.padLeft(4)}  ${dto.category}"
		}
		println ''
	}
	
	List<String> buildReportLines(List<String> categoryList, int numberOfColumns) {
		int maxTextSize = commonReportingService.maxTextSize(categoryList)
		
		return buildReportLines(categoryList, maxTextSize, numberOfColumns)
	}
	
	List<String> buildReportLines(List<String> categoryList, int maxTextSize, int numberOfColumns) {
		List<String> reportLineList = []
		
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
