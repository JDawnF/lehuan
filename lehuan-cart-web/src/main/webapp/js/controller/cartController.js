//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;     //返回购物车列表
                $scope.totalValue = cartService.sum($scope.cartList);//求合计数,参数是购物车列表
            }
        );
    }
    //添加商品到购物车,数量加减
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();//刷新列表
                } else {
                    alert(response.message);//弹出错误提示
                }
            }
        );
    }

    //根据用户名，获取当前登录账号用户的收货地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;  // 得到用户地址列表
                // 设置默认地址,1是数据库中设定的值，表示默认地址
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }

    // 选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }
    // 判断是否是当前选中的地址
    $scope.isSelectedAddress = function (address) {
        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }
    //默认付款方式为1，表示微信付款
    //paymentType是order表里面的一个字段，设置默认值为1
    $scope.order = {paymentType: '1'}
    //选择支付方式,用户选中传递type
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }
    //保存订单
    // 这里的order是js的service层传递过来的对象，将address对象的值对order对象进行
    // 重新赋值，order的属性是根据数据库的字段进行去掉下划线，然后变成大写拼接而成,这样可以对应数据库的字段
    $scope.submitOrder = function () {
        // 根据地址表对订单地址相关信息进行赋值
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    //页面跳转
                    if ($scope.order.paymentType == '1') {
                        //如果是微信支付，跳转到支付页面
                        location.href = "pay.html";
                    } else {//如果货到付款，跳转到提示页面
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.message);	//也可以跳转到提示页面
                }
            }
        );
    }
});
