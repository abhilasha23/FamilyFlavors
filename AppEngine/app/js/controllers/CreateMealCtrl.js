angular.module('familyflavors')
    .controller('CreateMealCtrl', function CreateMealCtrl($scope, $routeParams,$http, $filter,$q,$sce,$timeout,$rootScope,NgMap) {
// 'use strict';
    console.log("Inside CreateMealCtrl ")
     $scope.mealformaction = '';

        $http.get('/rest/CreateMeal')
              .then(function(r){
              //console.log("response")
              console.log(r)
              $scope.mealformaction = r.data.upload_url;
              //console.log("$scope.mealformaction  :" +$scope.mealformaction);

              })
    });