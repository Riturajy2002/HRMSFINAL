angular.module('leaveManagementApp')
  .service('YearService', function() {
    var selectedYear = new Date().getFullYear().toString(); 

    this.getSelectedYear = function() {
      return selectedYear;
    };

    this.setSelectedYear = function(year) {
      selectedYear = year;
    };
  });
