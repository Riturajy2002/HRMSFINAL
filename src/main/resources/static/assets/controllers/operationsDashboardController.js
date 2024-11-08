angular.module('leaveManagementApp')
	.controller('operationsDashboardController', ['$scope', '$sce', '$http', '$location', '$timeout', 'AuthService', '$window', '$document', function($scope, $sce, $http, $location,$timeout, AuthService,  $window, $document) {

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
		$scope.isDropdownVisible = false;
		$scope.isDashboardSelected = true;
		$scope.toggleDropdown = function() {
			$scope.isDropdownVisible = !$scope.isDropdownVisible;
		};
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

		// Function to handle dashboard selection
		$scope.selectDashboard = function() {
			$scope.isDashboardSelected = true;
		}

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

		// Toggle function for the dropdown visibility
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

		// Getting the Year Selection.
		$scope.isHomeFilterDropdownVisible = false;
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

		//Function to Operation Params Based on the operation  selection.
		$scope.operationParams = [];
		$scope.formVisible = true;
		$scope.fetchOperationParams = function(opCode) {
			if (!opCode) return;
			$http.get('/api/operation', {
				params: { opCode: opCode },
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.op = response.data;
				$scope.formVisible = true;
			}, function(error) {
				console.error('Error fetching operation params:', error);
			});
		};
		

		//Function for refreshing the after year changes.
		$scope.fetchDataForSelectedYear = function() {
			$scope.opCode = sessionStorage.getItem('selectedOpCode');
			if ($scope.opCode) {
				$scope.fetchOperationParams($scope.opCode);
			}
			$scope.getGridConfig($scope.customscreen);
			$scope.getGridConfigForView($scope.customScreenForView);
		}
		$scope.countofSelectedEmp = "Select the Employees";
		$scope.closeShowDetailsModal =function(){
			if($scope.pageTitleForViewEmp == 'List of Employees'){
				$scope.captureSelectedIds();
				if($scope.selectedUserIds.length > 0){
					$scope.countofSelectedEmp = $scope.selectedUserIds.length +" employees have been selected.";
				}
				else{
					$scope.countofSelectedEmp = "Select the Employees";
				}
			}
			
		}
		
		$scope.openSelectEmpForOpModal = function() {
			var selectEmpForOpModal = new bootstrap.Modal(document.getElementById('selectEmpForOpModal'));
			selectEmpForOpModal.show();
            
			
		};
		$scope.customScreenForView = 'emp-view-modal';
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
					$scope.pageTitleForViewEmp = response.data.pageTitle;

					var payload = {
						"screenName": screenNameForView,
						"params": {
							"location": null,
							"department": null,
							"organization": null,
							"searchTerm": null
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
					$scope.pageData = response.data;
					
				}).catch(function(error) {
					alert('There was an error fetching the filtered leave requests. Please try again.');
				});
			};
			// Call the grid config function for the initial load
			$scope.getGridConfigForView($scope.customScreenForView);
		// Submit Operation form function
		$scope.submitOperationForm = function() {

			$scope.op.year = $scope.selectedYear;
			if (!$scope.validateFields($scope.op.params)) {
				return;
			}

			if ($scope.op.reason === null) {
				alert("Reason is required to proceed with the operation.");
				return;
			}

			if ($scope.op.year === null) {
				alert("Year is required to proceed with the operation.");
				return;
			}

			if ($scope.selectedUserIds.length === 0) {
				alert('Please select at least one Employee to proceed this operation.');
				return;
			}

			$scope.op.selectedUserIds = $scope.selectedUserIds;
			$http.post('/api/performOperation', $scope.op)
				.then(function successCallback(response) {
					if (!response.data.error) {
						$scope.resetOperationForm();
					}
					alert(response.data.message);
				}, function errorCallback(response) {
					alert('Error while performing the operation');
				});
		};

		//Function for checking the Params validation
		$scope.validateFields = function(params) {
			for (const key in params) {
				if (params.hasOwnProperty(key)) {
					const field = params[key];
					if (field.optional && field.value === null) {
						alert(`Please select a value for ${key}.`);
						return false;
					}
				}
			}
			return true;
		};

		// Function to reset form and unselect checkboxes
		$scope.resetOperationForm = function() {
			$scope.countofSelectedEmp = "Select the Employees";
			$scope.op = {};
			$scope.newOperation = {};
			$scope.formVisible = false;
			$scope.fetchDataForSelectedYear();
			$scope.selectedUserIds = [];
		};
        
		$scope.selectedUserIds = [];
		$scope.pageSize = 25;
		$scope.customscreen = 'operation-dashboard';
		//json to be passed in cutomGrid
		$scope.filterData;
		//action object for grid 
		$scope.actionObject = {};

		//Function for loading initial data to show.(Filter as well)
		$scope.loadData = function(item) {
			
			if (item.action) {
				if (item.action.name === 'show') {
					$scope.openEmpDetailsModal((item.action.response['Employees']));
				}
			}
			
			item.params.year = $scope.selectedYear;
			item.params.userId = $scope.userData.userId;
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
				$scope.pageDataForView = response.data;
			}).catch(function(error) {
				alert('There was an error fetching the filtered leave requests. Please try again.');
			});
		};

		// Capture selected User IDs
		$scope.captureSelectedIds = function() {
			//first make empty 
			$scope.selectedUserIds =[];
			for (let val of $scope.pageData) {
				if (val.Selection) {
					$scope.selectedUserIds.push(val['Emp User-ID']);
				}
			}

		};

		//Function for getting Grid Configuration.
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
						"action": null,
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

		//View Employees Details on which operation is performed Modal
		$scope.openEmpDetailsModal = function(users) {
			   var EmpDetailsModal = new bootstrap.Modal(document.getElementById('EmpDetailsModal'));
			   EmpDetailsModal.show();
			   $scope.customScreenForView = 'emp-details-modal'; 
		      $scope.filterDataForDetails;
			  
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
							"location": null,
							"department": null,
							"organization": null,
							"searchTerm": null
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
				
				item.params.userId = users.split(',').map(item => `\'${item.trim()}\'`).join(',');
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
						$scope.totalRecordsForDetails = response.data.shift().totalRecords; // Get and remove leftmost element
					} else if (response.data.length == 0) {
						$scope.totalRecordsForDetails = 0;
					}
					$scope.filterDataForDetails = response.data;
				}).catch(function(error) {
					alert('There was an error fetching the filtered leave requests. Please try again.');
				});
			};
			// Call the grid config function for the initial load
			$scope.getGridConfigForView($scope.customScreenForView);
		}
		//Initial Fetch According to the Year.
		 //$scope.openSelectEmpForOpModal();
		$scope.fetchDataForSelectedYear();
	}
	]);