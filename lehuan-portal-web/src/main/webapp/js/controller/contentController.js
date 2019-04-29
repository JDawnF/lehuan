app.controller('contentController',function($scope,contentService){
    //广告集合，有多组广告，因为广告分类ID不同
	$scope.contentList=[];//广告列表，有多组广告
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}

    //搜索跳转，用location传递参数到搜索页面searchController.js,AngularJS中需要用#接收参数
	// 在首页搜索之后即可跳到搜索页
    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

});