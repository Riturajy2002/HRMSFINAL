angular.module('leaveManagementApp')
	.controller('addEmployeesDashboardController', ['$scope', '$sce', '$http', '$timeout', '$location', 'AuthService', '$window', function($scope, $sce, $http, $timeout, $location, AuthService, $window) {

		// Initialize User Data object
		$scope.userData = {
			id: '',
			contactNo: '',
			emailId_id: '',
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

		
		$scope.isEditMode = false;
		
		// Function to switch between modes
		$scope.switchToRegisterMode = function() {
			$scope.resetForm(); 
			$scope.isEditMode = false;
		};	
		
		$scope.switchToEditMode = function(employeeId) {
			$scope.resetForm();
			$scope.isEditMode = true;
		    $scope.loadEmployeeData(employeeId);  
		};
		
		// Function to load employee data in edit mode
		$scope.loadEmployeeData = function(userId) {
			$scope.isEditMode = true;
		    if (!userId) return;
		    
		    $http.get('/api/employeeDetails', {
				params: {userId : userId },
		        headers: {'auth-token': $scope.userData.token }
		    }).then(function(response) {
			$scope.selectedRoles =[];
		        $scope.newUser = response.data; 
		        let roles = $scope.newUser.role.split(',');
				for (let i = 0; i < roles.length; i++) {
					$scope.selectedRoles.push({ id: roles[i], label: roles[i] });
				}

		        //$scope.newUser.role = $scope.newUser.role.split(',');
				$scope.getDesignations($scope.newUser.department);
				$scope.newUser.birthDate = $scope.parseDate($scope.newUser.birthDate);
				$scope.newUser.anniversaryDate = $scope.parseDate($scope.newUser.anniversaryDate);
		    });
		}
		
		// Parse a date string (YYYY-MM-DD) to a Date object
		$scope.parseDate = function(dateString) {
			if (dateString) {
				return new Date(dateString);
			}
			return null;
		};
		
		$scope.showPassword = false;
		$scope.togglePasswordVisibility = function() {
			$scope.showPassword = !$scope.showPassword;
		};
		
		// Controller Initialization
			$scope.newUser = {
				id: "",
				organization: "",
				userId: "",
				name: "",
				department: "",
				designation: "",
				role: [],
				reportManager: "",
				location: "",
				gender: "",
				emailId: "",
				contactNo: null,
				password: "",
				birthDate: null,
				anniversaryDate: null,
				userKey: "",
				active: true

			};
		$scope.roleChoices = [{id:"User",label:"User"},{id:"Admin",label:"Admin"},{id:"Manager",label:"Manager"}];
		$scope.selectedRoles = [];
		$scope.submitForm = function() {
			if ($scope.registrationForm.$valid) {
				if ($scope.selectedRoles.length < 1) {
					alert("Role should not be empety !!");

				} else {
					var randomKey = generateRandomKey(6, 8);
					var roleData = $scope.selectedRoles.map(obj => obj.id).join(',');
					let newUserData = {
						id: $scope.newUser.id,
						organization: $scope.newUser.organization,
						userId: $scope.newUser.userId,
						name: $scope.newUser.name,
						department: $scope.newUser.department,
						designation: $scope.newUser.designation,
						role: roleData,
						reportManager: $scope.newUser.reportManager,
						location: $scope.newUser.location,
						gender: $scope.newUser.gender,
						emailId: $scope.newUser.emailId,
						contactNo: $scope.newUser.contactNo,
						password: $scope.newUser.password,
						birthDate: $scope.newUser.birthDate,
						anniversaryDate: $scope.newUser.anniversaryDate,
						userKey: randomKey,
						active: true
					};


					$scope.registrationMessage = '';
					$scope.userExistsError = '';

					// Call the respective API based on the mode
					if ($scope.registrationForm.$valid) {
						if ($scope.isEditMode) {
							$http.put('/api/super-admin/update', newUserData, {
								headers: { 'auth-token': $scope.userData.token }
							}).then(function(response) {
								$scope.registrationMessage = 'Employee records updated successfully!';
								$scope.resetForm();
								$scope.userExistsError = '';
							}, function(error) {
								let errorMessage = 'Error updating employee. Please try again.';
								if (error.data) {
									errorMessage = Object.values(error.data).join('\n');
								}
								$scope.registrationMessage = '';
								$scope.userExistsError = errorMessage;
							});
						} else {
							let randomKey = generateRandomKey(6, 8);
							newUserData.userKey = randomKey;

							$http.post('/api/super-admin/register', newUserData, {
								headers: { 'auth-token': $scope.userData.token }
							}).then(function(response) {
								$scope.registrationMessage = 'Employee registered successfully!';
								$scope.resetForm();
								$scope.userExistsError = '';
							}, function(error) {
								let errorMessage = 'Error registering employee. Please try again.';
								if (error.data) {
									errorMessage = Object.values(error.data).join('\n');
								}
								$scope.registrationMessage = '';
								$scope.userExistsError = errorMessage;
							});
						}
					} else {
						$scope.userExistsError = 'Please fill out the form correctly.';
						$scope.registrationMessage = '';
					}
				}
			}

		}

		// Function to reset form
		$scope.resetForm = function() {
			$scope.newUser = {
				id: "",
				organizatio: "",
				userId: "",
				name: "",
				department: "",
				designation: "",
				role: [],
				reportingManager: "",
				location: "",
				gender: "",
				emailId: "",
				contactNo: null,
				password: "",
				birthDate: null,
				aniversaryDate: null,
				userKey: "",
				active: true
			};
			$scope.selectedRoles =[];
			$scope.registrationForm.$setPristine();
			$scope.registrationForm.$setUntouched();
			$scope.submitted = false;
			$scope.getGridConfig($scope.customscreen);
		}
		
		// Function to generate userId based on id and organization
		$scope.generateUserId = function(isEditMode) {
			if(isEditMode) {
				return;
			}
			var id = $scope.newUser.id;
			var organization = $scope.newUser.organization;
			if (id && organization) {
				$scope.newUser.userId = id + '@' + organization;
			} else {
				$scope.newUser.userId = "";
			}
		};
			
		// Function to generate a random key
		function generateRandomKey(minLength, maxLength) {
			var length = Math.floor(Math.random() * (maxLength - minLength + 1)) + minLength;
			var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
			var result = '';
			for (var i = 0; i < length; i++) {
				result += characters.charAt(Math.floor(Math.random() * characters.length));
			}
			return result;
		}
		
		$scope.departments = [];
		$scope.departments = [];
		$scope.reportingManagers = [];
		$scope.locations = [];
		$scope.organizations = [];

		//Fetch all the organizations 
		$scope.getOrganizations = function() {
			$http.get('/api/fetchRefData', {
				params: { refId: 'organization' },
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.organizations = response.data;
			}, function(error) {
				console.error('Error fetching locations:', error);
			});
		};

		//Fetch all the locations
		$scope.getLocations = function() {
			$http.get('/api/fetchRefData', {
				params: { refId: 'location' },
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.locations = response.data;
			}, function(error) {
				console.error('Error fetching locations:', error);
			});
		};

		//Fetch Report Managers
		$scope.getReportManagers = function() {
			$http.get('/api/reportmanagers', {
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.reportingManagers = response.data;
			}, function(error) {
				console.error('Error fetching Reporting Managers:', error);
			});
		};
		
		// Fetch departments
		$scope.getDepartments = function() {
			$http.get('/api/fetchRefData', {
				params: { refId: 'department' },
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.departments = response.data;
			}, function(error) {
				console.error('Error fetching departments:', error);
			});
		};

		// Fetch designations based on selected department.
		$scope.getDesignations = function(department) {
			if (!department) return;

			$http.get('/api/fetchRefData', {
				params: { refId: 'designation',
					      parent: department
				 },
				headers: {
					'Content-Type': 'application/json',
					'auth-token': $scope.userData.token
				}
			}).then(function(response) {
				$scope.designations = response.data;
			}, function(error) {
				console.error('Error fetching designations:', error);
			});
		};

		// Fetch initial data
		$scope.getDepartments();
		$scope.getReportManagers();
		$scope.getLocations();
		$scope.getOrganizations();
		
		
		
		$scope.isEmpFilterDropdownVisible = false;
		$scope.toggleEmpFilterDropdown = function() {
			
			$scope.isEmpFilterDropdownVisible = !$scope.isEmpFilterDropdownVisible;
			if ($scope.isEmpFilterDropdownVisible) {
				$timeout(function() {
					angular.element(document).on('click', handleClickOutside);
				}, 0);
			} else {
				angular.element(document).off('click', handleClickOutside);
			}
		};

		// Handle click outside to close the dropdown
		function handleClickOutside(e) {
			if (!angular.element(e.target).closest('.Emp-filter-input').length) {
				$timeout(function() {
					$scope.$apply(function() {
						$scope.isEmpFilterDropdownVisible = false;
					});
				}, 0);
				angular.element(document).off('click', handleClickOutside);
			}
		}
		$scope.$on('$destroy', function() {
			angular.element(document).off('click', handleClickOutside);
		});
		
		$scope.pageData = [];
		$scope.pageSize = 25;
		$scope.customscreen = 'add-Employee-dashboard';
		//json to be passed in cutomGrid
		$scope.filterData;
		//action object for grid 
		$scope.actionObject = {};
		
		//Function for loading initial data to show.(Filter as well)
		$scope.loadData = function(item) {
			if (item.action) {
		
				if (item.action.name === 'edit') {
					$scope.loadEmployeeData(item.action.response['Emp User-ID']);
				}

			}
			item.params.year = null;
			item.params.userId = null;
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

		// Capture selected User IDs
		$scope.captureSelectedIds = function() {
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
						"location": null,
						"department": null,
						"organization": null,
						"searchTerm": null
					},
					"pageNo": 1,
					"pageSize": $scope.pageSize

				}
				$scope.loadData(payload);
			}).catch(function(error) {
				alert('There was an error fetching the Screen Data Please try again.');
			});
		};
		$scope.getGridConfig($scope.customscreen);
		
	}
		
	]);