angular.module('leaveManagementApp').directive('leaveRequestFormComponent', function() {
	return {
		templateUrl: 'views/leaveRequestForm.html',
		controller: 'leaveRequestFormDashboardController',
		controllerAs: '$ctrl'
	};
});
