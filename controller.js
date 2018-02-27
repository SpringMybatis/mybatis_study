/**
 *
 * Pass all functions into module
 */
angular.module('inspinia').controller('MainCtrl', function () {

}).controller('DemoCtrl', function ($scope, $cookies, $state) {

    var demo = this;

    demo.login = function () {
        var loginTime = new Date().getTime();
        console.info(loginTime);
        $cookies.put("loginTime", loginTime);
        $state.go("eagle.index");
    };

}).controller('TopCtrl', function ($scope, $cookies, $state) {

    var top = this;

    top.logout = function () {
        $cookies.remove("loginTime");
        $state.go("login");
    };

});


