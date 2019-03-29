//控制层
app.controller('userController', function ($scope, $controller, userService) {

    //注册用户
    $scope.reg = function () {
//在HTML页面上前后两个输入密码input框绑定不同的变量名，然后进行比较
        if ($scope.password != $scope.entity.password) {
            alert("两次输入密码不一致，请重新输入");
            //两次密码输入不同则清空
            $scope.entity.password = "";
            $scope.password = "";
            return;
        }
        //新增，绑定两个变量
        userService.add($scope.entity, $scope.smsCode).success(
            function (response) {
                alert(response.message);
            }
        );
    }

    //发送验证码,先判断手机号是否为空
    $scope.sendCode = function () {
        if ($scope.entity.phone == null || $scope.entity.phone == "") {
            alert("请填写手机号码");
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        );
    }
});	
