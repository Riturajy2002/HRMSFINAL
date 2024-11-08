angular.module('leaveManagementApp')
	.controller('leaveRequestFormDashboardController', ['$scope', '$sce', '$http', '$timeout', '$location', 'AuthService', 'YearService', '$window', '$document', function($scope, $sce, $http, $timeout,  $location, AuthService, YearService, $window ,$document) {

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

		$scope.roledata = [];
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

		$scope.screenName = 'Leave Request';
		$scope.isDashboardSelected = true;
		$scope.isDropdownVisible = false;
		$scope.toggleDropdown = function() {
			$scope.isDropdownVisible = !$scope.isDropdownVisible;
		};

		angular.element($document).on('click', function(event) {
			const filterDropDiv = angular.element(document.getElementById('dropdown-menu'));
			const dropDownMenuDiv = angular.element(document.getElementById('menu-items'));

			if (filterDropDiv.length && dropDownMenuDiv.length) {
				if ($scope.isDropdownVisible &&
					!filterDropDiv[0].contains(event.target) &&
					!dropDownMenuDiv[0].contains(event.target)) {
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
			$scope.screenName = 'Leave Request';
			$scope.isDashboardSelected = true;}
	
		// Function to check if user has a specific role
		$scope.hasRole = function(role) {
			var roles = $scope.userData.roles.map(function(r) {
				return r.toLowerCase().trim();
			});
			return roles.includes(role.toLowerCase());
		};

		// Function to toggle Attendance Dashboard.
		$scope.toggleAttandenceDashboard = function() {
			if ($scope.hasRole('admin')) {

				$location.path('/attandence-dashboard');
				return;
			}
		};
      
		//Function to toggle AddEmployee Dashboard.
		$scope.toggleAddEmployeeDashboard = function() {
			if ($scope.hasRole('admin')) {

				$location.path('/add-Employee-dashboard');
				return;
			}
		};
		
		// Function to toggle the dropdown visibility
		 $scope.isDropdownVisibleForOperations = false;
		 $scope.isDropdownVisibleForDefine = false;
		 $scope.isDropdownVisibleForScreens = false;
		 $scope.toggleMenuDropdown = function(value) {
			if(value == 'Employee Operations'){
				$scope.isDropdownVisibleForOperations = !$scope.isDropdownVisibleForOperations; 
			} else if(value == 'Define'){
				$scope.isDropdownVisibleForDefine =!$scope.isDropdownVisibleForDefine;
			} else if(value == 'Screens'){
				$scope.isDropdownVisibleForScreens =!$scope.isDropdownVisibleForScreens;
			}
		     
		 };
		
		
		$scope.typeNames = {};
		// For getting the defined types to show in the grid.
		$scope.getAllTypes = function() {
			$http.get('/api/get-types')
				.then(function(response) {
					response.data.forEach(function(item) {
						$scope.typeNames[item.typeId] = item.typeName;
					});
					console.log($scope.typeNames);
				}, function(error) {
					console.error('Error fetching employee operations:', error);
				});
		};
        $scope.getAllTypes();
        
         $scope.changeScreenName = function(value){
				 $scope.screenName = value;
		}
		
		$scope.callback = function(item){
			$scope.saveScreenData(item.response);
		}
		$scope.saveScreenData = function(data){
			console.log("Saving Function"+data);
		}
		
		$scope.callCustomScreen = function(data){
			$http.get("api/get-entity-type-config", {
					params: { typeId: data }
				}).then(function(response) {
					$scope.screenParams = response.data;
					$scope.customScreenPageData =[];
					var gridHeader ={};
					for(const obj of $scope.screenParams.fields){
						gridHeader[obj.fieldName]='';
					}
					$scope.customScreenPageData.push(gridHeader);
					$scope.screenParams.fields= $scope.screenParams.fields.reduce((result, item) => {
						// Get the group key based on the headingName
						const key = item.headingName;
						if (!result[key]) {
							result[key] = [];
						}
						result[key].push(item);
						return result;
					}, {});
					
					$scope.customScreenTotalRecords = 0;
					$scope.customScreenFilterData = {};
					$scope.changeScreenName(data);
				}).catch(function(error) {
					console.error('Error fetching Years From Ref Data', error);
				});
		}
		 
		
		// Function to toggle Employee Operation Dashboard and pass opCode
		$scope.handleOperationClick = function(value) {
			if ( $scope.hasRole('admin')) {
				sessionStorage.setItem('selectedOpCode', value);
				$location.path('/operation-dashboard');
			} 
			
		};
		
		// Function to toggle Approve Leave Request 
		$scope.toggleActionDashboard = function() {
			if ($scope.hasRole('manager')) {

				$location.path('/leave-action');
				return;
			}
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

		$scope.fixedHolidays = [];
		$scope.flexiLeaveDays = [];
		$scope.leaveTypes = [];
		$scope.operations = [];
		$scope.customscreen = 'leave-request';
		$scope.pageSize = 25;
		
		$scope.isHomeFilterDropdownVisible = false;
		
		// Getting the Year Selection.
		$scope.selectedYear = new Date().getFullYear().toString();
		$scope.yearsListForFilter = [];
		// Toggle function for the dropdown visibility
		$scope.hometoggleFilterDropdown = function() {
			$scope.isHomeFilterDropdownVisible = !$scope.isHomeFilterDropdownVisible;
		};
		
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
		
		// Function to load employee operations
		$scope.loadEmployeeOperations = function() {
			$http.get('/api/fetchEmployeeOps', {
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.operations = response.data;
			}, function(error) {
				console.error('Error fetching employee operations:', error);
			});
		};
		$scope.loadEmployeeOperations();

		
		
		// Function to fetch data based on selected year
		$scope.fetchDataForSelectedYear = function() {
			$http.get('/api/leave-types', {
				params: {
					year: parseInt($scope.selectedYear)
				}
			})
				.then(function(response) {
					$scope.leaveTypes = response.data.leaveTypes;
					$scope.flexiLeaveDays = response.data.flexiHolidays;
					$scope.fixedHolidays = response.data.fixedHolidays;
					$scope.leaveDesc = {};
					response.data.leaveTypes.forEach(function(leave) {
					    if (leave.code && leave.leaveDesc) {
					        $scope.leaveDesc[leave.code] = leave.leaveDesc;
					    }
					});
				})
				.catch(function(error) {
					console.error('Error fetching leave types:', error);
				});

			// Fetch leave status based on userId and selected year
			$scope.counts = {
				total: 0,
				available: 0,
				approved: 0,
				pending: 0,
				leaveBreakdown: []
			};
			$http({
				method: 'GET',
				url: '/api/leave-status',
				params: {
					year: parseInt($scope.selectedYear)
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
			$scope.resetForm();
			$scope.getGridConfig($scope.customscreen);
		};
         
		//For Getting Reporting manager Name
		$scope.loadReportManager = function() {
			$http.get('api/reportManager', {
				headers: { 'auth-token': $scope.userData.token }
			}).then(function(response) {
				const name = response.data.ApproverName && response.data.ApproverName !== 'null' ? response.data.ApproverName : '';
				const id = response.data.ApproverId  && response.data.ApproverId  !== 'null' ? response.data.ApproverId  : '';
				$scope.leaveRequest.manager = name + (id ? ' (' + id + ')' : '');
			}).catch(function(error) {
				console.error('Error fetching Reporting Manager:', error);
			});
		};
		$scope.loadReportManager();

		$scope.leaveRequest = {
			code: 'CL',
			fromDate: null,
			toDate: null,
			numberOfDays: 0,
			reason: '',
			manager: null,
			user: $scope.userData,
			appliedDate: new Date(),
			status: 'Pending'
		};

		// Initial setup
		$scope.showFlexiModal = false;

		// Initialize read-only state
		$scope.isDate
		FieldsReadOnly = false;

		// Open modal functions for the Flexi Select
		$scope.openFlexiModal = function() {
			var myModal = new bootstrap.Modal(document.getElementById('flexiModal'));
			myModal.show();
		};
        
		//Close Flexi modal
		$scope.closeFlexiModal = function() {
			var myModal = bootstrap.Modal.getInstance(document.getElementById('flexiModal'));
			myModal.hide();
		};
        

		// Function to handle date change
		$scope.handleLeaveTypeChange = function() {
			$scope.flexiModalLabel = null;
			$scope.leaveRequest.fromDate = null;
			$scope.leaveRequest.toDate = null;
			$scope.leaveRequest.numberOfDays = 0;
			$scope.leaveRequest.reason = '';
			$scope.leaveRequest.appliedDate = new Date();
			$scope.leaveRequest.status = 'Pending';
			$scope.formErrorMessage = '';
			$scope.formSuccessMessage = '';
			$scope.highlightCalender($scope.leaveRequest.fromDate, []);

			if ($scope.leaveRequest.code === 'FL') {
				$scope.isDateFieldsReadOnly = true;
				$scope.isFlexiLeave = true;
				$scope.openFlexiModal();
			} else {
				$scope.isDateFieldsReadOnly = false;
				$scope.isFlexiLeave = false;
			}
		};
        
		// Function to Show flexi Modal on the click on Dates.
		$scope.showFlexiModalOnDate = function($event) {
			if ($scope.leaveRequest.code === 'FL') {
				$event.stopPropagation();
				$scope.isFlexiLeave = true;
				$scope.openFlexiModal();
			}
		}

		// Setting the Start and end same as flexi Date
		$scope.selectFlexiDate = function(date) {
			$scope.leaveRequest.fromDate = moment(date.holidayDate).toDate();
			$scope.leaveRequest.toDate = moment(date.holidayDate).toDate();
			$scope.minDate = $scope.leaveRequest.toDate;
			$scope.checkEligibility();
			$scope.closeFlexiModal();
		};
		
		// Function to update minDate based on the selected From Date
		$scope.minDate = new Date();
		$scope.updateToDate = function() {
			if ($scope.leaveRequest.fromDate) {
				$scope.minDate = $scope.leaveRequest.fromDate;
				if (!$scope.leaveRequest.toDate || $scope.leaveRequest.toDate < $scope.leaveRequest.fromDate) {
					$scope.leaveRequest.toDate = $scope.leaveRequest.fromDate;
				}
				$scope.checkEligibility();
			} else {
				$scope.minDate = null;
				$scope.leaveRequest.toDate = null;
			}

		};
        
		//Function for checking the Eligibility critera to apply the leave.
		$scope.checkEligibility = function() {
			var fromDate = $scope.leaveRequest.fromDate;
			var toDate = $scope.leaveRequest.toDate;
			if (!fromDate || !toDate || !$scope.leaveRequest.code || toDate < fromDate) {
				return;
			}
			$scope.formErrorMessage = '';
			$scope.leaveAppliedDates = [];

			$http({
				method: 'POST',
				url: '/api/checkLeave',
				headers: {
					'auth-token': $scope.userData.token
				},
				params: {
					type: $scope.leaveRequest.code,
					fromDate: fromDate,
					toDate: toDate,
					year: YearService.getSelectedYear()
				}
			}).then(function(response) {
				var message = response.data.message;
				if (message && typeof message === 'string') {
					var match = message.match(/Total.*days applied: (\d+)/);
					if (match) {
						$scope.leaveRequest.numberOfDays = parseInt(match[1]);
					} else {
						$scope.formErrorMessage = message;
						$scope.leaveRequest.numberOfDays = 0;
					}
				} else {
					$scope.formErrorMessage = 'Invalid response format. Please try again.';
				}

				if (response.data.validLeaveDates) {
					$scope.leaveAppliedDates = response.data.validLeaveDates.map(function(date) {
						return { date: date };
					});
					$scope.highlightCalender($scope.leaveRequest.fromDate, $scope.leaveAppliedDates)
				} else {
					$scope.highlightCalender($scope.leaveRequest.fromDate, []);
				}

			}, function(error) {
				$scope.formErrorMessage = 'Error checking eligibility: ' + error.statusText;
			});
		};
        
		//Function to highlight Date selected for the leave.
		$scope.highlightCalender = function(startDate, leaveDates) {
			var date = new Date();
			if (startDate) {
				date = startDate;
			}
			var startYear = date.getFullYear();
			var startMonth = date.getMonth();
			$scope.$broadcast('updateLeaveDates', {
				leaveDates: leaveDates,
				month: startMonth,
				year: startYear
			});
		}
		
		// Submitting  form for the leave Request.
		$scope.submitForm = function() {
			$scope.formErrorMessage = '';
			$scope.checkEligibility();

			if ($scope.leaveRequest.numberOfDays <= 0) {
				$scope.formErrorMessage = 'Number of days must be greater than zero.';
				return;
			}

			if ($scope.leaveForm.$valid) {
				let leaveRequestCopy = angular.copy($scope.leaveRequest);
				let year = YearService.getSelectedYear();
				$http.post('/api/leave-request',
					leaveRequestCopy,
					{
						headers: {
							'auth-token': $scope.userData.token
						},
						params: {
							year: year
						}
					}
				).then(function(response) {
					$scope.formSuccessMessage = 'Leave request submitted successfully!';
					$scope.fetchDataForSelectedYear();
				})
					.catch(function(error) {
						let errorMessage = 'Error Submitting Leave Request';
						if (error.data && error.data.message) {
							errorMessage = error.data.message;
						}
						$scope.formErrorMessage = errorMessage;
					});
			}
		};

		// Reset form data after submitting the form
		$scope.resetForm = function() {
			$scope.leaveRequest.code = 'CL';
			$scope.leaveRequest.fromDate = null;
			$scope.leaveRequest.toDate = null;
			$scope.leaveRequest.numberOfDays = 0;
			$scope.leaveRequest.reason = '';
			$scope.leaveRequest.appliedDate = new Date();
			$scope.leaveRequest.status = 'Pending';
			$scope.highlightCalender($scope.leaveRequest.fromDate, []);
			var yearMinDate = new Date($scope.selectedYear, 0, 1);
			var yearMaxDate = new Date($scope.selectedYear, 11, 31);
			$scope.yearMaxDate = yearMaxDate;
			$scope.yearMinDate = yearMinDate;
			if($scope.leaveForm){
				$scope.leaveForm.$setPristine();
				$scope.leaveForm.$setUntouched();
			}
			
			// Broadcast an event to trigger calendar refresh
			$scope.$broadcast('refreshCalendar', {
				month: new Date().getMonth(),
				year: $scope.selectedYear || new Date().getFullYear()
			});
		};
		
		// Function to confirm and cancel the leave request
		$scope.confirmCancelRequest = function(action) {
			const leaveId = action.response['Leave Id'];
			if (action && action.response['Status'] === 'Pending') {
				$http.put('/api/leave-request/cancel-leave-request/' + leaveId, null, {
					headers: {
						'auth-token': $scope.userData.token
					}
				}).then(function(response) {
					action.response.Status = 'Cancelled';
					alert('Leave request successfully cancelled.');
					$timeout(function() {
						var cancelRequestModal = bootstrap.Modal.getInstance(document.getElementById('cancelRequestModal'));
						if (cancelRequestModal) {
							cancelRequestModal.hide();
						}
					}, 100);
					$scope.fetchDataForSelectedYear();
				}).catch(function(error) {
					console.error('Error cancelling leave request:', error);
					alert('There was an error cancelling the leave request. Please try again.');
				});
			} else {
				alert('You can only cancel leave requests with Pending status.');
			}
		};

		
		// Initialize scope variables
		$scope.toggleFilterDropdown = function() {
			$scope.isFilterDropdownVisible = !$scope.isFilterDropdownVisible;
		};
		
		//Fetching Grid Config based on screen Name.
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

		//Fetching the data based on the filter(Initial load as well)
		$scope.loadData = function(item) {
			$scope.formConfirmation = function(){
				$scope.confirmCancelRequest(item.action);
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
				$scope.pageData = response.data;
			}).catch(function(error) {
				alert('There was an error fetching the filtered leave requests. Please try again.');
			});
		};

		//Initial fetch of the Data based on the selected Year.
		$scope.fetchDataForSelectedYear();
	}]);