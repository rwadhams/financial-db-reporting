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
		categoryListService.verifyLists()
		
		categoryListService.printLists()
	}
	
}
