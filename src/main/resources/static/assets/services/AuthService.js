angular.module('leaveManagementApp').service('AuthService', ['$http', '$q', function($http, $q) {
    this.logout = function() {
        var deferred = $q.defer();
        $http.post('/login/logout').then(function(response) {
            deferred.resolve(response.data);
        }, function(error) {
            deferred.reject(error);
        });
        return deferred.promise;
    };
}]);
