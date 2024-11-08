angular.module('leaveManagementApp')
	.directive('customGrid', function() {
		return {
			restrict: 'AE',  // Can be used as an element
			scope: {
				screen: "=",
				pageData: "=",
				totalRecords: "=",
				pageSize: "=",
				onUpdate: "&",
				callBack: "&",
				getSelectedItem: "&",
				filterObj: "=",
				yearMinDate: "=",
				yearMaxDate: "="
			},
			// Isolate scope (optional)
			controller: function($scope, $timeout, $http, $window, $document) {
				$scope.parentIndex = 0;
				$scope.gridData = $scope.pageData;
				$scope.isFilterDropdownVisible = false;
				$scope.currentPage = 1;
				$scope.listOfActions = [];
				$scope.searchItem = '';
				$scope.filterGrid = function() {
					if ($scope.searchItem.length > 0) {
						filterGridData = $scope.pageData.filter(item => {
							//The some method checks if any of the keys in the item contain the search term.
							return Object.keys(item).some(key => {
								const value = item[key];
								return value !== null && value.toString().toLowerCase().includes($scope.searchItem.toLowerCase());
							}
							);
						});
					} else {
						filterGridData = $scope.gridData;
					}
					$scope.pageData = filterGridData;
				}
				if ($scope.filterObj) {
					for (let val in $scope.filterObj.actions) {
						$scope.listOfActions.push($scope.filterObj.actions[val]);
					}
					$scope.filterObjs = $scope.filterObj.params.filter;
					for (let [key, value] of Object.entries($scope.filterObjs)) {
					if (value.type == 'multi_select') {
						value.options = [];
						value.value = [];
						for (let val of $scope.filterObj.refData[value.sourceId]) {
							value.options.push({ label: val.refName, id: val.refName });
						}
					}
				}
				}
				
				
				

				// Initialize scope variables
				$scope.toggleFilterDropdown = function() {
					$scope.isFilterDropdownVisible = !$scope.isFilterDropdownVisible;
				};

				// Close the dropdown when clicking outside
				/*angular.element($document).on('click', function(event) {
					const filterButton = angular.element(document.getElementById('filterDropdownButton'));
					const filterDropdown = angular.element(document.getElementsByClassName('filter-home-drop')[0]);
	
					if ($scope.isFilterDropdownVisible) {
						// If clicked outside the filter button and dropdown, close the dropdown
						if (!filterButton[0].contains(event.target) && !filterDropdown[0].contains(event.target)) {
							$scope.$apply(function() {
								$scope.isFilterDropdownVisible = false;
							});
						}
					}
				});*/

				$scope.$watch('totalRecords', function(newVal) {
					if (newVal) {
						$scope.totalPages = Math.ceil($scope.totalRecords / $scope.pageSize);
					}
				}, true);



				$scope.updatePageData = function() {
					if ($scope.pageData.length) {
					} else {
						$scope.pageData = [];
					}
				};


				var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
				var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
					return new bootstrap.Tooltip(tooltipTriggerEl)
				});


				// Function to change the page
				$scope.changePage = function(page) {
					if (page < 1 || page > $scope.totalPages) {
						return;
					}
					$scope.currentPage = page;
					$scope.actionObj = {};
					$scope.sendCallBack();
				};
				$scope.updateItemsPerPage = function() {
					$scope.currentPage = 1;
					$scope.totalPages = Math.ceil($scope.totalRecords / $scope.pageSize);
					$scope.actionObj = {};
					$scope.sendCallBack();
				}

				$scope.allChecked = false;
				var isAllSelected = function(obj) {
					for (let val of obj) {
						if (!val.Selection) {
							return false;
						}
					}
					return true;
				}
				var updateGrid = function() {
					$scope.allChecked = false;
				}
				$scope.allCheck = function(index) {
					if (index != -1) {

						if ($scope.pageData[index].Selection) {
							$scope.pageData[index].Selection = !$scope.pageData[index].Selection;
						} else {
							$scope.pageData[index].Selection = true;
						}

						if (isAllSelected($scope.pageData)) {
							$scope.allChecked = true;
						} else {
							$scope.allChecked = false;
						}

					} else {
						$scope.allChecked = !$scope.allChecked;
						for (let val of $scope.pageData) {
							val.Selection = $scope.allChecked;

						}
					}
				}

				$scope.actionObj = {};
				$scope.actionCall = function(response, name) {
					$scope.actionObj.response = response;
					$scope.actionObj.name = name;
					$scope.sendCallBack();
				}

				$scope.applyFilters = function() {
					$scope.currentPage = 1;

					$scope.sendCallBack();
					//$scope.clearFilters();
				}
				$scope.clearFilters = function() {
					for (let [key, val] of Object.entries($scope.filterObjs)) {
						if (val.value != "") {
							if (val.type == 'multi_select') {
								val.value = [];
							} else {
								val.value = '';
							}
						}
					}
				}
				$scope.sendCallBack = function() {
					// Pass multiple arguments to the parent
					var paramValues = {};
					var item = {
						params: paramValues,
						action: $scope.actionObj,
						pageNo: $scope.currentPage,
						pageSize: $scope.pageSize

					};
					const deepCopy = structuredClone($scope.filterObj.params.filter);
					for (let [key, value] of Object.entries($scope.filterObj.params.filter)) {
						
						if (value && value.hasOwnProperty('value')) {
							if (value.value == "") {
								paramValues[key] = null;
							} else {
								if (Array.isArray(value.value)) {

									value.value = value.value.map(item => {
										// Initialize a variable to store the found value
										let foundValue = null;

										// Loop through the filterObj.refData
										for (let [key, val] of Object.entries($scope.filterObj.refData)) {
											// Find the matching item
											const matchingItem = val.find(i => i.refName === item.id);

											// If a matching item is found, store its refValue
											if (matchingItem) {
												foundValue = matchingItem.refValue;
												break; // Exit the loop once found
											}
										}

										return foundValue; // Return the found value or null if not found
									});

									paramValues[key] = value.value.map(item => `\'${item}\'`).join(',');
								} else {
									paramValues[key] = value.value;
								}
							}
						}
					}
					for (let [key, value] of Object.entries($scope.filterObj.params.filter)) {
						if (value.type == "multi_select") {
							for (let [k, val] of Object.entries(deepCopy)){
								if(k == key){
									value.value = val.value;
								}
							}
						}
					}
					console.log($scope.filterObj.params.filter); 
					$scope.callBack(
						{ item }
					);

				};



				$scope.downloadEmployeesReport = async function() {
					var employeesData = $scope.pageData || [];

					if (employeesData.length === 0) {
						alert('No data available to export');
						return;
					}
					const workbook = new ExcelJS.Workbook();
					const worksheet = workbook.addWorksheet('Report');

					// Define header row
					var headerNames = [];
					for (let data in $scope.pageData[0]) {
						if (data == 'Selection' || data == 'Action') {
							continue;
						} else {
							headerNames.push(data);
						}
					}
					const headerRow = worksheet.addRow(headerNames);
					// Style header row
					headerRow.eachCell((cell) => {
						cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
						cell.fill = {
							type: 'pattern',
							pattern: 'solid',
							fgColor: { argb: 'FF4F81BD' }
						};
					})

					// Add employee data rows
					employeesData.forEach(employee => {

						let data = [];
						headerNames.forEach(header => {
							data.push(employee[header]);
						});

						worksheet.addRow(data);
						data = [];
					});

					// Set column widths
					worksheet.columns.forEach(column => {
						let maxLength = 0;
						column.eachCell({ includeEmpty: true }, (cell) => {
							maxLength = Math.max(maxLength, cell.value ? cell.value.toString().length : 0);
						});
						column.width = maxLength + 2; // Add some padding
					});


					// Add borders to all cells
					worksheet.eachRow({ includeEmpty: true }, function(row) {
						row.eachCell({ includeEmpty: true }, function(cell) {
							cell.border = {
								top: { style: 'thin' },
								left: { style: 'thin' },
								bottom: { style: 'thin' },
								right: { style: 'thin' }
							};
						});
					});

					// Generate Excel file and trigger download
					workbook.xlsx.writeBuffer().then(function(buffer) {
						const blob = new Blob([buffer], { type: 'application/octet-stream' });
						const url = URL.createObjectURL(blob);
						const a = document.createElement('a');
						a.href = url;
						a.download = 'Report.xlsx';
						a.click();
						URL.revokeObjectURL(url);
					});
				};
			},
			templateUrl: 'views/customGrid.html'
		};
	});