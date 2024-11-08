angular.module('leaveManagementApp')
	.directive('loginComponent', function() {
		return {
			templateUrl: 'views/login.template.html',
			controller: 'loginDashboardcontroller',
			controllerAs: '$ctrl'
		};
	});
