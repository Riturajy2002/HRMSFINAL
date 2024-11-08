angular.module('leaveManagementApp')
.directive('choicesDropdown', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            ngModel: '=',            // Two-way binding with the model
            choicesOptions: '=?',    // Choices.js options object, optional
            availableItems: '=?'     // Available items for the dropdown, optional
        },
        link: function(scope, element) {
            let choicesInstance;
            let isInitialized = false;
            function initializeChoices() {
                if (!isInitialized) {
					scope.choicesOptions = scope.choicesOptions || {
                        removeItemButton: true,
                        maxItemCount: 10,
                        searchResultLimit: 10,
                        renderChoiceLimit: 10,
                        shouldSort: false,
                        duplicateItemsAllowed: false,
                        placeholder: true,
                        placeholderValue: 'Select options'
                    };
				
                    choicesInstance = new Choices(element[0], scope.choicesOptions);
                    var placeholderItem = choicesInstance._getTemplate( 'placeholder', scope.choicesOptions.placeholderValue); 
                    choicesInstance.itemList.append(placeholderItem); //show placeholder on initialization
                    isInitialized = true;
                    updateChoices();
                }
            }
            
            
	
            function updateChoices() {
                $timeout(() => {
                    if (scope.availableItems && choicesInstance) {
                        const choices = scope.availableItems.map(item => ({
                            value: item,
                            label: item
                        }));

                        // Clear existing choices and set new ones
                        choicesInstance.clearChoices();
                        choicesInstance.setChoices(choices, 'value', 'label', true);
                        updateSelectedValues();
                    }
                }, 100);
            }

            function updateSelectedValues() {
                $timeout(() => {
                    if (choicesInstance && Array.isArray(scope.ngModel)) {
                        const currentSelectedValues = scope.ngModel || [];
                        choicesInstance.removeActiveItems(); // clear previous items
                        choicesInstance.setChoiceByValue(currentSelectedValues);
                    }
                }, 100);
            }

            function syncModelToChoices() {
                if (choicesInstance) {
                    const selectedValues = choicesInstance.getValue(true);
                    scope.$applyAsync(() => {
                        scope.ngModel = selectedValues;
                    });
                }
            }

            // Watch for changes in ngModel to update selected values
            scope.$watch('ngModel', function(newVal) {
                if (choicesInstance && newVal) {
                    updateSelectedValues();
                }
            }, true);

            // Watch for changes in available items to update choices
            scope.$watch('availableItems', function(newItems) {
                if (newItems) {
                    updateChoices();
                }
            }, true);

            // Listen for Choices.js changes and sync back to model
            element.on('change', function() {
                syncModelToChoices();
            });

            $timeout(initializeChoices, 0);

            // Cleanup Choices.js instance on scope destruction
            scope.$on('$destroy', function() {
                if (choicesInstance) {
                    choicesInstance.destroy();
                    choicesInstance = null;
                }
            });
        }
    };
});
