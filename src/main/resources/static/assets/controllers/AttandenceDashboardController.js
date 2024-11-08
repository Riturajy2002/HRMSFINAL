angular.module('leaveManagementApp')
	.controller('attandenceDashboardController', ['$scope', '$sce', '$http', '$location', 'AuthService', '$window', function($scope, $sce, $http, $location, AuthService, $window) {

		// Initialize User Data object
		$scope.userData = {
			id: '',
			contactNo: '',
			email_id: '',
			password: '',
			designation: '',
			username: '',
			report_manager: '',
			profilePicUrl: '',
			token: '',
			gender: '',
			roles: []
		};

		// Function to fetch user data from local storage and initialize $scope.userData
		function initializeUserData() {
			$scope.userData.id = $window.sessionStorage.id;
			$scope.userData.userId = $window.sessionStorage.userId;
			$scope.userData.token = $window.sessionStorage.token;
			$scope.userData.contactNo = $window.sessionStorage.contactNo;
			$scope.userData.email_id = $window.sessionStorage.email_id;
			$scope.userData.designation = $window.sessionStorage.designation;
			$scope.userData.username = $window.sessionStorage.username;
			$scope.userData.report_manager = $window.sessionStorage.report_manager;
			$scope.userData.profilePicUrl = $window.sessionStorage.profilePicUrl;
			$scope.userData.gender = $window.sessionStorage.gender;
			$scope.userData.roles = $window.sessionStorage.roles ? $window.sessionStorage.roles.split(',') : [];
		}

		// Call the initialization function on controller load
		initializeUserData();

		// Back To User Dashbaord
		$scope.toggleUserDashboard = function() {
			$location.path('/leave-request');
		};
        

		$scope.openProfileModal = function() {
			var openProfileModal = new bootstrap.Modal(document.getElementById('profileModal'));
			openProfileModal.show();
		}

		//Close Profile modal
		$scope.closeProfileModal = function() {
			var openProfileModal = bootstrap.Modal.getInstance(document.getElementById('profileModal'));
			openProfileModal.hide();
		};

		
		// Log out with the use of auth-token
		$scope.logout = function() {
			var headers = {
				'auth-token': $window.sessionStorage.token
			};
			var requestConfig = {
				headers: headers
			};
			$http.get('/api/logout', requestConfig)
				.then(function successCallback(response) {
					$window.sessionStorage.clear();
					$location.path("/login");
				}, function errorCallback(response) {
					alert('Error logging out:', response);
				});
		};

		// Function to upload Excel file
		$scope.uploadExcel = function() {
			if (!$scope.file || !$scope.file.name.match(/\.(xls|xlsx)$/)) {
				alert('Please upload a valid Excel file.');
				return;
			}
			var formData = new FormData();
			formData.append('file', $scope.file);

			$http.post('/api/upload-attendanceSheet', formData, {
				transformRequest: angular.identity,
				headers: {
					'Content-Type': undefined,
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				alert(response.data.message);
			}, function(error) {
				alert('Error uploading file: ' + (error.data && error.data.message ? error.data.message : 'Unknown error'));
			});
		};

		// Initialize months and years for dropdowns
		$scope.months = [
			{ value: 1, name: 'January' },
			{ value: 2, name: 'February' },
			{ value: 3, name: 'March' },
			{ value: 4, name: 'April' },
			{ value: 5, name: 'May' },
			{ value: 6, name: 'June' },
			{ value: 7, name: 'July' },
			{ value: 8, name: 'August' },
			{ value: 9, name: 'September' },
			{ value: 10, name: 'October' },
			{ value: 11, name: 'November' },
			{ value: 12, name: 'December' }
		];

		// Initialize years for dropdown in descending order
		$scope.years = [];
		for (var i = new Date().getFullYear(); i >= 2018; i--) {
			$scope.years.push(i);
		}

		// Set default selected month and year to current
		var currentDate = new Date();
		$scope.selectedMonth = $scope.months[currentDate.getMonth()];
		$scope.selectedYear = currentDate.getFullYear();

		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.totalPages = 0;

		// Function to fetch and display the report based on selected month and year
		$scope.fetchReport = function() {
			if (!$scope.selectedMonth || !$scope.selectedYear) {
				$scope.selectedMonth = $scope.months[currentDate.getMonth()];
				$scope.selectedYear = currentDate.getFullYear();
			}
			var url = '/api/super-admin/fetch-report?month=' + $scope.selectedMonth.value + '&year=' + $scope.selectedYear;
			var config = {
				headers: {
					'auth-token': $scope.userData.token
				}
			};
			$http.get(url, config)
				.then(function(response) {
					$scope.allData = response.data;
					$scope.totalPages = Math.ceil($scope.allData.length / $scope.itemsPerPage);
					$scope.updatePageData();
				})
				.catch(function(error) {
					console.error('Error fetching report', error);
					alert('Error fetching report');
				});
		};

		// Fetch the report when the controller is initialized
		$scope.fetchReport();
		$scope.updatePageData = function() {
			var startIndex = ($scope.currentPage - 1) * $scope.itemsPerPage;
			var endIndex = startIndex + $scope.itemsPerPage;
			$scope.filteredData = $scope.allData.slice(startIndex, endIndex);
		};
		$scope.changePage = function(page) {
			if (page < 1 || page > $scope.totalPages) {
				return;
			}
			$scope.currentPage = page;
			$scope.updatePageData();
		};

		// Watch for changes in selectedMonth and selectedYear to update the report
		$scope.$watch('selectedMonth', $scope.fetchReport);
		$scope.$watch('selectedYear', $scope.fetchReport);


		// Function to download the report based on selected month and year
		$scope.downloadReport = function() {
			if (!$scope.selectedMonth || !$scope.selectedYear) {
				alert("Please select both a month and a year");
				return;
			}
			var url = '/api/super-admin/download-report?month=' + $scope.selectedMonth.value + '&year=' + $scope.selectedYear;
			var config = {
				headers: {
					'auth-token': $scope.userData.token
				},
				responseType: 'arraybuffer'
			};
			$http.get(url, config)
				.then(function(response) {
					var blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
					var link = document.createElement('a');
					link.href = window.URL.createObjectURL(blob);
					link.download = 'Employees_Report_' + $scope.selectedYear + '_' + $scope.selectedMonth.name + '.xlsx';
					link.click();
				})
				.catch(function(error) {
					console.error('Error downloading report', error);
					alert('Error downloading report');
				});
		};
	}
	]);