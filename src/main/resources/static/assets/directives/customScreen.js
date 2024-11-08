angular.module('leaveManagementApp')
	.directive('customScreen', function() {
		return {
			restrict: 'E',  // Can be used as an element
			scope: {
				screen: "=",
				params:"=",
				pageData:"=",
				totalRecords:"=",
				pageSize:"=",
				filterObj:"=",
				callBack: "&",
				token: "="
			},
			controller: function($scope, $timeout, $http, $window, $document) {
				console.log($scope.screen);
				$scope.obj = {};
				$scope.submitForm = function(){
					console.log();
					var item = {
						response: $scope.obj}
					$scope.callBack({item});

				}
			},
			templateUrl: 'views/customScreen.html'
		};
	});