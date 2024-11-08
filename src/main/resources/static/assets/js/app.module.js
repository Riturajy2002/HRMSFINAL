var leaveManagementApp = angular.module('leaveManagementApp', [
    'ngRoute', 'angularjs-dropdown-multiselect'
]);
leaveManagementApp.config(['$routeProvider', '$locationProvider', '$httpProvider', function ($routeProvider, $locationProvider, $httpProvider) {
    
    
    // Add the interceptor to the $httpProvider
    $httpProvider.interceptors.push('myHttpResponseInterceptor');

    $routeProvider
        .when('/login', {
            templateUrl: 'views/login.template.html',
            controller: 'loginDashboardController',
            controllerAs: '$ctrl'
        })
        .when('/leave-request', {
            templateUrl: 'views/leaveRequestForm.html',
            controller: 'leaveRequestFormDashboardController',
            controllerAs: '$ctrl'
        })
		.when('/attandence-dashboard',{
			templateUrl: 'views/attendanceDashboard.html',
			controller: 'attandenceDashboardController',
		    controllerAs: '$ctrl'
		})
		.when('/leave-action', {
		           templateUrl: 'views/leaveActionDashboard.html',
		           controller: 'leaveActionDashboardController',
		           controllerAs: '$ctrl'
		       })
		.when('/add-Employee-dashboard', {
			templateUrl: 'views/addEmployeesDashboard.html',
			controller: 'addEmployeesDashboardController',
			controllerAs: '$ctrl'
		})
		.when('/operation-dashboard', {
			templateUrl: 'views/operationsDashboard.html',
			controller: 'operationsDashboardController',
			controllerAs: '$ctrl'
		})
        .otherwise({
            redirectTo: '/login'
        });
}]);

leaveManagementApp.run(function ($rootScope, $location) {
    // Any run-time logic you need
});


leaveManagementApp.factory('myHttpResponseInterceptor', function($q, $location, $window, $rootScope) {
    var service = {
        // Run this function before making requests 
        'request': function(config) {
            if ($window.sessionStorage.userId && $window.sessionStorage.token) {
                config.headers['user-name'] = $window.sessionStorage.userId;
                config.headers['auth-token'] = $window.sessionStorage.token;
                config.headers['group'] = $window.sessionStorage.group;
            } else if ($location.$$path != '/login' && $location.$$path != '/logout') {
                $location.path("/login");
                return $q.reject(config);
            }
            return config;
        },

        'response': function(response) {
            return response;
        },
        'responseError': function(response) {
            if (response.status == 401) {
                delete $window.sessionStorage.token;
                delete $window.sessionStorage.userId;
                setTimeout(function() {
                    $location.path("/login");
                }, 100);
            }
            return $q.reject(response);
        }
    };

    return service;
});

// Define the mainCtrl
leaveManagementApp.controller("mainCtrl", function($scope, $rootScope) {
    // Your main controller logic here
});
