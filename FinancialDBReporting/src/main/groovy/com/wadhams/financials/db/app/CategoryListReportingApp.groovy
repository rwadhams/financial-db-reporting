package com.wadhams.financials.db.app

import com.wadhams.financials.db.service.CategoryListService

class CategoryListReportingApp {
	CategoryListService categoryListService = new CategoryListService()
	
	static main(args) {
		println 'CategoryListReportingApp started...'
		println ''

		CategoryListReportingApp app = new CategoryListReportingApp()
		app.execute()
		
		println 'CategoryListReportingApp ended.'
	}
	
	def execute() {
		Boolean result = categoryListService.verifyLists()
		if (result) {
			println 'CategoryList verified'
			println ''
			categoryListService.printLists()
			categoryListService.printAllCategories()	//vertical list of Categories
		}
		else {
			println 'CategoryList unverified'
			println ''
		}
	}
	
}
