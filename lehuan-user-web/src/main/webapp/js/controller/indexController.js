//首页控制器
app.controller('indexController',function($scope,loginService){
    // 显示用户名
    $scope.showName=function(){
        loginService.showName().success(
            function(response){
                //绑定用户名，后端是一个map，key是loginName
                $scope.loginName=response.loginName;
            }
        );
    }
});
