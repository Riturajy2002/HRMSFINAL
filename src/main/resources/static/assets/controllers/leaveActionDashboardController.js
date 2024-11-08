angular.module('leaveManagementApp')
	.controller('leaveActionDashboardController', ['$scope', '$sce', '$http', '$location', 'AuthService', 'YearService', '$timeout', '$window','$document', function($scope, $sce, $http, $location, AuthService, YearService, $timeout, $window, $document) {

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

		$scope.isDashboardSelected = true;
		$scope.isDropdownVisible = false;
		$scope.toggleDropdown = function() {
			$scope.isDropdownVisible = !$scope.isDropdownVisible;
		};
		angular.element($document).on('click', function(event) {
			const filterDropDiv = angular.element(document.getElementById('dropdown-menu'));
			const dropDownMenuDiv = angular.element(document.getElementById('menu-items'));

			if ($scope.isDropdownVisible) {
				if (!filterDropDiv[0].contains(event.target) && !dropDownMenuDiv[0].contains(event.target)) {
					$scope.$apply(function() {
						$scope.isDropdownVisible = false;
					});
				}
			}
		});

		$scope.openProfileModal = function() {
			var openProfileModal = new bootstrap.Modal(document.getElementById('profileModal'));
			openProfileModal.show();
		}

		//Close Profile modal
		$scope.closeProfileModal = function() {
			var openProfileModal = bootstrap.Modal.getInstance(document.getElementById('profileModal'));
			openProfileModal.hide();
		};
		
		// Function to handle dashboard selection
		$scope.selectDashboard = function() {
			$scope.isDashboardSelected = true;
		};
		
		// Back To User Dashbaord
		$scope.toggleUserDashboard = function() {
			$location.path('/leave-request');
		};

		// Initialize the visibility of the dropdown
		$scope.isHomeFilterDropdownVisible = false;
		$scope.hometoggleFilterDropdown = function() {
			$scope.isHomeFilterDropdownVisible = !$scope.isHomeFilterDropdownVisible;
		};

		angular.element($document).on('click', function(event) {
		    const filterDiv = angular.element(document.getElementById('homefilterDropdownButton'));
		    const dropDownDiv = angular.element(document.getElementById('filter-dropDown'));

		    if ($scope.isHomeFilterDropdownVisible) {
		        if (!filterDiv[0].contains(event.target) && !dropDownDiv[0].contains(event.target)) {
		            $scope.$apply(function() {
		                $scope.isHomeFilterDropdownVisible = false;
		            });
		        }
		    }
		});
		
		

		$scope.selectedYear = new Date().getFullYear().toString();
		$scope.yearsListForFilter = [];
		$scope.getYearListForFilter = function() {
			$http.get('api/fetchRefData', {
				params: { refId: 'year' },
				headers: { 'auth-token': $scope.userData.token }
			}).then(function(response) {
				$scope.yearsListForFilter = response.data.map((item) => item.refValue);
			}).catch(function(error) {
				console.error('Error fetching Years From Ref Data', error);
			});
		};
		$scope.getYearListForFilter();
		
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
		
		//Function to render based on the selected Year.
		$scope.fetchDataForSelectedYear = function() {
			//json to be passed in cutomGrid
			$scope.filterData;
			//action object for grid 
			$scope.actionObject = {};
			//$scope.filteredData = [];
			//$scope.pageData = [];
			$scope.pageSize = 25;
			$scope.customscreen = 'leave-action';
			
			//Initial Fetching for the Grid Configuration.
			$scope.getGridConfig($scope.customscreen);
		}

		//Function for loading initial data to show.(Filter as well)
		$scope.loadData = function(item) {
			item.params.year = $scope.selectedYear;
			item.params.userId = $scope.userData.userId;
			
			if (item.action) {
			    if (item.action.name === 'approve' || item.action.name === 'decline') {
			        $scope.openRemarkModal(item.action.response['Leave Id'], item.action.name);
			    }
				
				if (item.action.name === 'view') {
					$scope.openUserModal(item.action.response['Emp Name']);
				}
				
			}
			
			var args = {
				"screenName": $scope.customscreen,
				"paramsValues": JSON.stringify(item.params),
				"pageNo": item.pageNo,
				"pageSize": item.pageSize
			};

			$http({
				method: 'POST',
				url: '/api/getGridData',
				data: args,
				headers: {
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				if (response.data.length > 0 && Object.keys(response.data[0]).length == 1 && ("totalRecords" in response.data[0])) {
					$scope.totalRecords = response.data.shift().totalRecords; //get and remove leftmost or 0'th elements
				} else if (response.data.length == 0) {
					$scope.totalRecords = 0;
				}
				$scope.pageData = response.data;
			}).catch(function(error) {
				alert('There was an error fetching the filtered leave requests. Please try again.');
			});
		};
		

		//Function to Fetch Grid Configuration.
		$scope.getGridConfig = function(screenName) {
			$http.get('api/getGridConfig', {
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				},
				params: {
					screenName: screenName
				}
			}).then(function(response) {
				$scope.filterData = response.data;
				$scope.pageTitle = response.data.pageTitle;
				var payload = {
					"screenName": $scope.customscreen,
					"params": {
						"leaveTypes": null,
						"status": null,
						"fromDate": null,
						"toDate": null
					},
					"pageNo": 1,
					"pageSize": $scope.pageSize

				}
				$scope.loadData(payload);
			}).catch(function(error) {
				alert('There was an error fetching the Screen Data Please try again.');
			});
		};
		
		// Open the modal and set the appropriate status (Approve/Decline)
		$scope.remarksInput = '';
		$scope.leaveId = '';
		$scope.status = '';
		$scope.modalTitle = '';
		$scope.openRemarkModal = function(leaveId, actionStatus) {
		    $scope.leaveId = leaveId;  
		    $scope.status = actionStatus === 'approve' ? 'Approved' : 'Declined';  
		    $scope.modalTitle = ($scope.status === 'Approved') ? 'Approve Leave Request' : 'Decline Leave Request';  
		    $scope.remarksInput = ''; 
		    var remarksModal = new bootstrap.Modal(document.getElementById('remarksModal'));
		    remarksModal.show();
		};
		
		//Submit the remarks (Approve or Decline)
		$scope.submitRemarks = function() {
			if (!$scope.remarksInput) {
				alert('Please provide remarks before submitting.');
				return;
			}
			$http({
				method: 'POST',
				url: '/api/leave-request/update',
				params: {
					leaveId: $scope.leaveId,
					status: $scope.status,
					remarks: $scope.remarksInput
				},
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function() {
				alert('Leave request ' + $scope.status.toLowerCase() + ' successfully.');
				var remarksModal = bootstrap.Modal.getInstance(document.getElementById('remarksModal'));
				if (remarksModal) {
				      remarksModal.hide();
				   }
				$scope.fetchDataForSelectedYear();
			}).catch(function(error) {
				console.error('Error updating leave request:', error);
				alert('An error occurred while processing the leave request.');
			});
		};
		
		//Opening modal for User Details.
		$scope.openUserModal = function(empInfo) {
			$scope.empName = empInfo.match(/^(.+?)\(/)[1].trim();
			$scope.empUserId = empInfo.match(/\((.*?)\)/)[1]; 
			$scope.openUserDetails();
			var userDetailsModal = new bootstrap.Modal(document.getElementById('userDetailsModal'));
		    userDetailsModal.show();	
		}
	
		// Fetch leave requests
		$scope.openUserDetails = function() {
			$scope.customScreenForView = 'user-view-modal';
			$scope.filterDataForView;

			$scope.counts = {
				total: 0,
				available: 0,
				approved: 0,
				pending: 0,
				leaveBreakdown: []
			};
			$http({
				method: 'GET',
				url: '/api/get-leaves',
				params: {
					year: parseInt($scope.selectedYear),
					userId: $scope.empUserId
				},
				headers: {
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.counts.total = response.data.total;
				$scope.counts.available = response.data.available;
				$scope.counts.approved = response.data.approved;
				$scope.counts.pending = response.data.pending;
				$scope.counts.leaveBreakdown = response.data.leaveBreakdown;
			}).catch(function(error) {
				alert('There was an error fetching the leave status. Please try again.');
			});

			// Fetching Grid Config based on screen Name
			$scope.getGridConfigForView = function(screenNameForView) {
				$http.get('api/getGridConfig', {
					headers: {
						'Content-Type': 'application/json',
						'auth-token': $scope.userData.token
					},
					params: {
						screenName: screenNameForView
					}
				}).then(function(response) {
					$scope.filterDataForView = response.data;
					$scope.pageTitleForView = response.data.pageTitle;

					var payload = {
						"screenName": screenNameForView,
						"params": {
							"leaveTypes": null,
							"status": null,
							"fromDate": null,
							"toDate": null
						},
						"pageNo": 1,
						"pageSize": $scope.pageSize
					};
					$scope.loadDataForView(payload);
				}).catch(function(error) {
					alert('There was an error fetching the Screen Data. Please try again.');
				});
			};

			// Fetching the data based on the filter (Initial load as well)
			$scope.loadDataForView = function(item) {
				$scope.formConfirmation = function() {
					$scope.confirmCancelRequest(item.action);
				};
				item.params.year = $scope.selectedYear;
				item.params.userId = $scope.empUserId;

				var args = {
					"screenName": $scope.customScreenForView,
					"paramsValues": JSON.stringify(item.params),
					"pageNo": item.pageNo,
					"pageSize": item.pageSize
				};

				$http({
					method: 'POST',
					url: '/api/getGridData',
					data: args,
					headers: {
						'auth-token': $scope.userData.token
					}
				}).then(function(response) {
					if (response.data.length > 0 && Object.keys(response.data[0]).length == 1 && ("totalRecords" in response.data[0])) {
						$scope.totalRecordsForView = response.data.shift().totalRecords; // Get and remove leftmost element
					} else if (response.data.length == 0) {
						$scope.totalRecordsForView = 0;
					}
					$scope.pageDataForView = response.data;
				}).catch(function(error) {
					alert('There was an error fetching the filtered leave requests. Please try again.');
				});
			};
			// Call the grid config function for the initial load
			$scope.getGridConfigForView($scope.customScreenForView);
		}
		
		//Initial Fetching the data based on the selected year.
		$scope.fetchDataForSelectedYear();   
		   
	}]);
