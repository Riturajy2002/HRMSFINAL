angular.module('leaveManagementApp').service('AuthenticationService', ['$http', function($http) {
    var userId = null;
    this.login = function(credentials) {
        return $http.post('/login', credentials)
            .then(function(response) {
                userId = response.data.userId; 
                return response.data;
            });
    };
    this.getUserId = function() {
        return userId;
    };
}]);
