angular.module('leaveManagementApp')
	.directive('define', function() {
		return {
			restrict: 'E',  // Can be used as an element
			scope: {
				screen: "=",
				token: "="
			},
			controller: function($scope, $timeout, $http, $window, $document) {
				$scope.actionObj = {};
				$scope.typeNames = {};
				$scope.pageSize = 25;
				// For getting the defined types to show in the grid.
				$scope.getAllTypes = function() {
					$http.get('/api/get-types')
						.then(function(response) {
							$scope.gridData = response.data;
							response.data.forEach(function(item) {
								$scope.typeNames[item.typeId] = item.typeName;
							});
							console.log($scope.typeNames);
						}, function(error) {
							console.error('Error fetching employee operations:', error);
						});
				};

				$scope.getAllTypes();
				// Initialize the form object
				let fieldList = [];
				let dataSet = [];
				let data = {
					fields: fieldList
				}
				$scope.obj = {
					dataModel:false,
					active: false,
					fields:dataSet
				};
				$scope.modelConfigObj ={
					dataType:"text",
					showInGrid:false,
					active: false
				}; 
				$scope.gridfieldList = [];
				$scope.showGridInModelConfig = false;
				$scope.addFields = function(){
					
					dataSet.push($scope.modelConfigObj);
					$scope.gridfieldList.push($scope.modelConfigObj);
					$scope.modelConfigObj = {
						dataType: "text",
						showInGrid:false,
						active: false
					}; 
					if($scope.gridfieldList.length >0 ){
						$scope.showGridInModelConfig = true;
					}
				}
				//Fetching Grid Config based on screen Name.
				$scope.getGridConfigForDefineType = function(screenName) {
					$http.get('api/getGridConfig', {
						headers: {
							'Content-Type': 'application/json',
							'auth-token': $scope.token
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
								"searchTypeName": null,
								"parentType": null,
							},
							"pageNo": 1,
							"pageSize": $scope.pageSize

						}
						$scope.loadDataForDefineType(payload);
					}).catch(function(error) {
						alert('There was an error fetching the Screen Data Please try again.');
					});
				};

				//Fetching the data based on the filter(Initial load as well)
				$scope.loadDataForDefineType = function(item) {

					var args = {
						"screenName": $scope.screen,
						"paramsValues": JSON.stringify(item.params),
						"pageNo": item.pageNo,
						"pageSize": item.pageSize
					};

					$http({
						method: 'POST',
						url: '/api/getGridData',
						data: args,
						headers: {
							'auth-token': $scope.token
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

				$scope.getGridConfigForReference = function(screenName) {
					$http.get('api/getGridConfig', {
						headers: {
							'Content-Type': 'application/json',
							'auth-token': $scope.token
						},
						params: {
							screenName: screenName
						}
					}).then(function(response) {
						$scope.filterDataForReference = response.data;
						$scope.pageTitle = response.data.pageTitle;
						var payload = {
							"screenName": $scope.customscreen,
							"params": {
								"refernceType": null,
								"refernceName": null,
								"refernceValue": null
							},
							"pageNo": 1,
							"pageSize": $scope.pageSize

						}
						$scope.loadDataForReference(payload);
					}).catch(function(error) {
						alert('There was an error fetching the Screen Data Please try again.');
					});
				};

				//Fetching the data based on the filter(Initial load as well)
				$scope.loadDataForReference = function(item) {

					var args = {
						"screenName": $scope.screen,
						"paramsValues": JSON.stringify(item.params),
						"pageNo": item.pageNo,
						"pageSize": item.pageSize
					};

					$http({
						method: 'POST',
						url: '/api/getGridData',
						data: args,
						headers: {
							'auth-token': $scope.token
						}
					}).then(function(response) {
						if (response.data.length > 0 && Object.keys(response.data[0]).length == 1 && ("totalRecords" in response.data[0])) {
							$scope.totalRecordsOfReference = response.data.shift().totalRecords; //get and remove leftmost or 0'th elements
						} else if (response.data.length == 0) {
							$scope.totalRecordsOfReference = 0;
						}
						$scope.pageDataForReference = response.data;
					}).catch(function(error) {
						alert('There was an error fetching the filtered leave requests. Please try again.');
					});
				};
				// Function to submit the form
				$scope.submitForm = function() {
					if ($scope.screen == 'Define Type') {
						//dataSet.push(data);
						delete $scope.obj['dataModel'];
						//$scope.obj.dataModelConfig = JSON.stringify($scope.obj.dataModelConfig);
						$http.post('/api/define-type', $scope.obj)
							.then(function(response) {
								if (response.data.success) {
									alert(response.data.success);
									$scope.getGridConfigForDefineType($scope.screen);
									resetForm();
								} else{
									alert(response.data.error);
								}
							}, function(error) {
								console.error('Error submitting new type:', error);
								alert('Failed to register the type. Please try again.');
							});
					} else {
						for (let i = 0; i < $scope.gridData.length; i++) {
							if ($scope.obj.refType == $scope.gridData[i].typeId) {
								$scope.obj.parentValue = $scope.gridData[i].parentName;
							}
						}

						$http.post('/api/save-ref-data', $scope.obj)
							.then(function(response) {
								if (response.data.success) {
									alert(response.data.success);
									$scope.getGridConfigForReference($scope.screen);
									resetForm();
								}
							}, function(error) {
								alert('Failed  For saving the Ref Master Data. Please try again.');
							});
					}

				};

				$scope.getGridConfigForDefineType($scope.screen);
				
				
				
				function resetForm() {
					
					$scope.obj = {
						dataModel: false,
						active: false,
						fields: dataSet
					};
					$scope.gridfieldList = [];
				}
			},
			templateUrl: 'views/define.html'
		};
	});
