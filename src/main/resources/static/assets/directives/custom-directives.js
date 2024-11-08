angular.module('leaveManagementApp')
	.directive('calendar', function($timeout, $http, $window, YearService) {
		return {
			restrict: 'A',
			link: function(scope, element) {
				$timeout(function() {

					// Initialize User Data object directly from sessionStorage
					scope.userData = {
						id: $window.sessionStorage.getItem('id'),
						userId: $window.sessionStorage.getItem('userId'),
						token: $window.sessionStorage.getItem('token'),
						contactNo: $window.sessionStorage.getItem('contactNo'),
						email_id: $window.sessionStorage.getItem('email_id'),
						designation: $window.sessionStorage.getItem('designation'),
						username: $window.sessionStorage.getItem('username'),
						report_manager: $window.sessionStorage.getItem('report_manager'),
						profilePicUrl: $window.sessionStorage.getItem('profilePicUrl'),
						gender: $window.sessionStorage.getItem('gender'),
						roles: $window.sessionStorage.getItem('roles') ? $window.sessionStorage.getItem('roles').split(',') : []
					};

					const daysTag = element[0].querySelector(".days"),
					currentDate = element[0].querySelector(".current-date"),
					prevNextIcon = element[0].querySelectorAll(".icons span");

					let date = new Date(),
					currYear = date.getFullYear();
					currMonth = date.getMonth();

					const months = [
						"January", "February", "March", "April", "May", "June", "July",
						"August", "September", "October", "November", "December"
					];

					scope.flexiLeaveDay = [];
					scope.fixedHolidaysAvailed = [];
					scope.pendingLeaveDates = [];
					scope.approvedLeavesDates = [];
					scope.declinedLeavesDates = [];

					scope.leaveAppliedDates = []; 

					let dateRangeClasses = {};

					function validateAndFormatDate1(dateObj) {
						// Check if dateObj is an object and has the date property
						let obj= {};
						if (typeof dateObj === 'object' && dateObj.holidayDate) {
							obj.name = dateObj.name;
							const momentDate = moment(dateObj.holidayDate);
							const formattedDate = momentDate.isValid() ? momentDate.format('YYYY-MM-DD') : null;
							obj.date = formattedDate;
							return obj;
						} else {
							return null;
						}
					}

					// Fetch and process leave dates by status
					function fetchLeaveDates(year, status, arrayToUpdate) {
					    $http.get(`/api/leaveDates`, {
					        params: { year: year, status: status, userId: scope.userData.userId },
					        headers: { 'auth-token': scope.userData.token }
					    })
					    .then(function(response) {
					        response.data.forEach(date => {
					            arrayToUpdate.push(date); 
					        });
					        renderCalendar();
					    })
					    .catch(function(error) {
					        alert(`There was an error fetching the ${status} leave days. Please try again.`);
					    });
					}


					// Fetch leave dates for pending, approved, and declined statuses
					function fetchAllLeaveDates(year) {
						scope.pendingLeaveDates = [];
						scope.approvedLeavesDates = [];
						dateRangeClasses = {}; 

						fetchLeaveDates(year, 'pending', scope.pendingLeaveDates);
						fetchLeaveDates(year, 'approved', scope.approvedLeavesDates);
					}

					// Fetch flexi leaves
					function fetchFlexiLeaves(year) {
						$http.get('/api/allFlexiLeaves', {
							params: { year: year },
							headers: { 'auth-token': scope.userData.token }
						})
							.then(function(response) {
								const formattedDates = response.data.map(dateObj => validateAndFormatDate1(dateObj));
								scope.flexiLeaveDay = [...new Set(formattedDates.filter(date => date.date !== null))];
								renderCalendar();
							})
							.catch(function(error) {
								alert('There was an error fetching the flexi leave days. Please try again.');
							});
					}

					// Fetch fixed holidays
					function fetchFixedHolidays(year) {
						$http.get('/api/fixed-holidays', {
							params: { year: year }, 
							headers: { 'auth-token': scope.userData.token }
						})
							.then(function(response) {
								const formattedDates = response.data.map(dateObj => validateAndFormatDate1(dateObj));
								scope.fixedHolidaysAvailed = [...new Set(formattedDates.filter(date => date.date !== null))];
								renderCalendar();
							})
							.catch(function(error) {
								console.error('Error fetching fixed holidays:', error);
							});
					}

                      
					// Render the calendar
					const renderCalendar = () => {
						const today = new Date();
						let firstDayofMonth = new Date(currYear, currMonth, 1).getDay(),
							lastDateofMonth = new Date(currYear, currMonth + 1, 0).getDate(),
							lastDayofMonth = new Date(currYear, currMonth, lastDateofMonth).getDay(),
							lastDateofLastMonth = new Date(currYear, currMonth, 0).getDate();
						let liTag = "";

						// Adding previous month's last days as inactive
						for (let i = firstDayofMonth; i > 0; i--) {
							liTag += `<li class="inactive">${lastDateofLastMonth - i + 1}</li>`;
						}

						// Adding current month's days
						for (let i = 1; i <= lastDateofMonth; i++) {
						    let day = new Date(currYear, currMonth, i).getDay();
						    let dateStr = moment(new Date(currYear, currMonth, i)).format('YYYY-MM-DD');
						    let classes = [];
						    let title = [];  // Array to store titles for holidays, flexi leaves, etc.

						    // Check if date is part of any leave date range and assign corresponding classes
						    for (let rangeClass in dateRangeClasses) {
						        if (dateRangeClasses[rangeClass].includes(dateStr)) {
						            classes.push(rangeClass);
						        }
						    }

						    // Assign static classes for status - pending, approved, declined
						    if (scope.pendingLeaveDates.some(range => range.includes(dateStr))) {
						        classes.push('pending');
								title.push('Pending');
						    }

						    if (scope.approvedLeavesDates.some(range => range.includes(dateStr))) {
						        classes.push('approved');
								title.push('Approved');
						    }

						    // Highlight the current day
						    if (i === today.getDate() && currMonth === today.getMonth() && currYear === today.getFullYear()) {
						        classes.push("active");
								
						    }

						    // Fixed holidays and flexi leaves with titles
						    if (scope.fixedHolidaysAvailed && scope.fixedHolidaysAvailed.length > 0) {
						        scope.fixedHolidaysAvailed.forEach(item => {
						            if (item.date && item.date.includes(dateStr)) {
						                classes.push("fixed-holiday");
						                title.push(item.name);
						            }
						        });
						    }

						    if (scope.flexiLeaveDay && scope.flexiLeaveDay.length > 0) {
						        scope.flexiLeaveDay.forEach(item => {
						            if (item.date && item.date.includes(dateStr)) {
						                classes.push("flexi-leave");
						                title.push(item.name);  // Add the flexi leave name to the title array
						            }
						        });
						    }

						    // Highlight leave applied dates
						    if (scope.leaveAppliedDates.some(date => date.date === dateStr)) {
						        classes.push("highlighted");
						    }

						    // Add weekend classes
						    if (day === 6) {
						        classes.push("saturday");
						    } else if (day === 0) {
						        classes.push("sunday");
						    }

						    // Construct the <li> tag with class and title attributes
						    liTag += `<li class="${classes.join(' ')}" title="${title.join(', ')}">${i}</li>`;
						}
						// Adding next month's starting days as inactive
						for (let i = lastDayofMonth; i < 6; i++) {
							liTag += `<li class="inactive">${i - lastDayofMonth + 1}</li>`;
						}

						if (currentDate) {
							currentDate.innerText = `${months[currMonth]} ${currYear}`;
						} else {
							console.error("Element with class 'current-date' not found.");
						}

						if (daysTag) {
							daysTag.innerHTML = liTag;
						} else {
							console.error("Element with class 'days' not found.");
						}
					};
					function fetchAndRenderCalendar(month, year) {
						currMonth = month;
						currYear = year;
						renderCalendar();
					}


					// Initialize calendar on load
					fetchFixedHolidays(currYear);
					fetchFlexiLeaves(currYear);
					fetchAllLeaveDates(currYear);

					prevNextIcon.forEach(icon => {
					    icon.addEventListener("click", () => {
					        let selectedYear = parseInt(scope.selectedYear);
					        if (icon.id === "prev") {
					            currMonth--;
					            if (currMonth < 0) {
					                if (currYear > selectedYear || selectedYear < new Date().getFullYear()) { 
					                    currYear--;      
					                } else {
					                    currMonth = 0; 
					                }
					            }
					        } else if (icon.id === "next") {
					            currMonth++;
					            if (currMonth > 11) {
					                if (currYear < selectedYear || selectedYear > new Date().getFullYear()) { 
					                    currMonth = 0; 
					                    currYear++;  
					                } else {
					                    currMonth = 11;
					                }
					            }
					        }

					        // Fetch updated data for the new month and year
					        fetchFlexiLeaves(currYear);
					        fetchAllLeaveDates(currYear);
					        fetchFixedHolidays(currYear);
					        fetchAndRenderCalendar(currMonth, currYear);
					    });
					});


					scope.$on('updateLeaveDates', function(event, data) {
						if (data.year === currYear) {
						        scope.leaveAppliedDates = data.leaveDates;
						        fetchAndRenderCalendar(data.month, data.year);
						    } 
					});
					
					scope.$on('refreshCalendar', function(event, data) {
						fetchAllLeaveDates(data.year);
						fetchAndRenderCalendar(data.month, data.year);
					});

					fetchAndRenderCalendar(currMonth, currYear);
					scope.$watch('selectedYear', function(newValue) {
						if (newValue) {
							currYear = parseInt(newValue);
							if (new Date().getFullYear() === currYear) {
							           currMonth = new Date().getMonth(); 
							       } else {
							           currMonth = 0; 
							       }
							fetchFlexiLeaves(currYear);
							fetchAllLeaveDates(currYear);
							fetchFixedHolidays(currYear);
							fetchAndRenderCalendar(currMonth, currYear);
						}
					});

				}, 0);
			}
		};
	});
