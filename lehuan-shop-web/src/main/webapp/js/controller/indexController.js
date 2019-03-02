app.controller('indexController',function ($scope,loginService) {
    //读取当前登录人
    $scope.showLoginName=function () {
        loginService.loginName().success(
            function (response) {
                // loginName是LoginController中放回的map中的key
                $scope.loginName=response.loginName;
            }
        );
    }
});