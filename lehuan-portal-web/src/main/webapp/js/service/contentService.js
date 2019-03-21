app.service('contentService' ,function($http){
	
	//根据广告分类ID查询广告,这里要注意目录不用../
	// 因为页面是在根目录下的，get括号中是请求url
	this.findByCategoryId=function(categoryId){
		return $http.get('content/findByCategoryId.do?categoryId='+categoryId);
	}
	
});