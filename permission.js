angular.module('ngPermissions', ['ui.router'])
    .factory("permissionConstant", function () {

        return {
            loginRouter: "login",
            loginHref: "http://localhost:8088/#/login",
            indexHref: "http://localhost:8088/#/eagle/index"
        };

    })
    .factory("permissionHelper", function () {

        return {
            expiredHour: 8,

            getCurrentTime: function () {
                return new Date().getTime();
            },

            isExpired: function (loginTime) {
                var expired = false;
                var currentTime = this.getCurrentTime();
                var subTime = currentTime - loginTime;
                var subHour = subTime / 1000 / 3600;
                if (subHour > this.expiredHour) {
                    expired = true;
                }
                return expired;
            }

        }

    })
    .run(function ($rootScope, $location, $state, $http, $cookies, permissionConstant, permissionHelper) {

        $rootScope.$on('$stateChangeStart', function (event, toState) {
            // 设置消息头loginTime
            if (!$http.defaults.headers.common.AuthTime || $http.defaults.headers.common.AuthTime == null) {
                $http.defaults.headers.common.AuthTime = $cookies.get("loginTime");
            }
            // 验证是否过期
            var loginTime = $cookies.get("loginTime");
            var expired = !!loginTime ? permissionHelper.isExpired(loginTime) : true;
            var isLoginUrl = (toState.name == permissionConstant.loginRouter);

            if (isLoginUrl) {
                if (!expired) {
                    // window.location.href = permissionConstant.indexHref;

                    $state.go("eagle.index");

                }
            } else {
                if (expired) {
                    // window.location.href = permissionConstant.loginHref;

                    $state.go("login");
                }
            }

            if (null == $state.get(toState.name)) {
                // window.location.href = permissionConstant.loginHref;

                $state.go("login");
            }

        });

        $rootScope.$state = $state;

    })
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push('httpPermissionInterceptor');
    }])
    .factory('httpPermissionInterceptor', ['$q', '$cookies', '$location', 'permissionConstant', function ($q, $cookies, $location, permissionHelper) {
        return {
            request: function (config) {

                /*var loginTime = $cookies.get("loginTime");
                var expired = !!loginTime ? permissionHelper.isExpired(loginTime) : true;

                if (expired) {
                    window.location.href = "http://localhost:8088";
                }*/

                if (!config.headers.AuthTime || config.headers.AuthTime == null) {
                    config.headers.AuthTime = $cookies.get("loginTime");
                }
                return config;
            },

            response: function (response) {
                return response;
            }

        }
    }])
/*.run(function ($http, $cookies, permissionConstant) {
 hookAjax({
 send: function (arg, xhr) {
 console.info(arg);
 console.info(xhr);
 console.info($http);
 console.info($cookies);
 console.info(permissionConstant);

 if ($http.defaults.headers.common.AuthTime) {
 xhr.setRequestHeader("AuthorizationTime", $http.defaults.headers.common.AuthTime);
 }
 },
 onload: function (xhr) {
 console.info(xhr);
 }
 })
 })*/;
