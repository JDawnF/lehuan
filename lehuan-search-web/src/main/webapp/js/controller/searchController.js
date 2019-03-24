//添加location服务用于接收参数，先引入location服务,接收首页搜索框传递过来的搜索参数
app.controller('searchController', function ($scope, $location, searchService) {
    //定义搜索对象的结构，传给后端，充当搜索条件，除了spec是json，其他都是字符串，当需要新增搜索条件时，只需要在后面添加对应的key即可
    //pageNo：页码；pageSize：总页数;
    // sort是升降序，升序ASC，降序DESC；sortField是排序字段
    $scope.searchMap = {
        'keywords': '', 'category': '', 'brand': '', 'spec': {}, 'price': '',
        'pageNo': 1, 'pageSize': 40, 'sort': '', 'sortField': ''};//搜索条件封装对象

    //搜索
    $scope.search = function () {
        //前端传过来的当前页是字符串的，而后端搜索查询的方法参数中是整型的，所以要先转型
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                //搜索返回的结果，进行绑定，返回一个对象
                $scope.resultMap = response;
                buildPageLabel();   //构建分页栏
            }
        );
    }

    //添加搜索项，key是可以是多个搜索选项，根据前端页面传过来的不同值而定
    // 将页面传过来的值根据key的不同存入搜索条件对象searchMap
    $scope.addSearchItem = function (key, value) {
        //如果用户点击的是分类或者是品牌
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;  // 根据点击的选项，存入对应的列表
        } else {
            //用户点击的是规格，无法知道key是什么，所以就要用作参数传入
            $scope.searchMap.spec[key] = value;
        }
        //后端查询
        $scope.search();
    }

    //撤销搜索项,移除复合搜索条件，同理，key是可以是多个搜索选项，根据前端页面传过来的不同值而定
    //将页面传过来的值根据key的不同清空搜索条件对象searchMap中对应的值
    $scope.removeSearchItem = function (key) {
        //如果用户点击的是分类或者是品牌
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = "";
        } else {
            //用户点击的是规格，移除属性
            delete $scope.searchMap.spec[key];
        }
        //后端查询
        $scope.search();
    }
    //构建分页栏
    //构建分页标签(totalPages为总页数),如果搜索的商品太多，会导致页面太多不美观，于是设定为最多显示五页：1,2,3,4,5，
    // 然后显示下一页，以当前页为中心的5个页码，要判断当前页要大于等于3，或者小于最大页码-2
    // 则显示结果为：1 2 3 4 5或：1 2 ... 5 6 7
    buildPageLabel = function () {
        //构建分页栏，即分页码1，2，3，4，5...
        $scope.pageLabel = [];  //新增分页栏数组
        // 8 9 10 11 12,8为开始页码，12位截止页码
        var firstPage = 1;  //开始页码
        var lastPage = $scope.resultMap.totalPages;//截止页码
        $scope.firstDot = true;     //前面有省略号小点
        $scope.lastDot = true;      //后边有省略号小点
        // 总页数分为两大类：多于5页，少于等于5页，
        // 少于等于于5页的时候，前后都不设置点，直接遍历存入分页栏数组即可
        // 多于5页的时候，分为三种情况，一种是当前页面少于等于3页的时候，前面不设置点；
                                // 当前页多于总页数-2的时候，从总页数-4开始，后面不设置点；
                                // 当前页大于3小于总页数-2的时候，显示当前页的前两页和当前页的后五页
        if ($scope.resultMap.totalPages > 5) {       //如果总页数大于5页,显示部分页码
            if ($scope.searchMap.pageNo <= 3) {     //如果当前页页码小于等于3
                lastPage = 5; //前5页
                $scope.firstDot = false;    //前面没点
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {   //如果当前页大于等于最大页码-2
                firstPage = $scope.resultMap.totalPages - 4;		 //后5页
                $scope.lastDot = false;//后边没点
            } else { //显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点

        }
        //循环产生页码标签,从开始页码到截止页码，如果总页数小于5则全显示
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //分页查询,即跳到选中的页码
    $scope.queryByPage = function (pageNo) {
        //页码验证,pageNo表示当前页
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }
    //判断当前页是否为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //设置排序规则,需要字段和升序/降序这两个参数
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();    // 设置完之后调用查询方法
    }

    //判断关键字是不是品牌,是的话要隐藏搜索栏的品牌列表
    $scope.keywordsIsBrand = function () {
        //通过判断searchMap中的关键字是否包含resultMap品牌列表
        // 注意品牌列表格式是:{id:1,text:’’}，所以要.text，表示选择text这个属性对应的值
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;    // 如果包含返回true
            }
        }
        return false;
    }
    //加载首页传递过来的查询字符串，获取location传递的参数
    $scope.loadkeywords = function () {
        //'keywords'与location传递的参数名一样
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
});