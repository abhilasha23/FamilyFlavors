angular.module('familyflavors')
    .controller('HomeChefCtrl', function HomeChefCtrl($rootScope,$scope,$window ,$routeParams,$http,$filter,$q,$sce,$timeout,NgMap) {
        'use strict';

        console.log("You are here in HomeChefCtrl - HomeChef page controller")

        /* call IsAuthenticated
        *     if return true, check if profile exists
        *          if return true GetProfile(email)
        * */

        // Creating sample HomeChef data below for testing puposes. Will call the correct /getProfile service later.
        $scope.homechef = new Object()
        $scope.usermeals = []

        //console.log("inside $scope.GetProfile "+ $rootScope.useremail);

        $http.get('/rest/getProfile?email='+$rootScope.useremail)
             .then(function(r){
             console.log("got response from rest/GetProfile ");
             console.log(r);
             $scope.homechef.firstname = r.data.firstname;
             $scope.homechef.lastname = r.data.lastname;
             $scope.homechef.profileimage = r.data.imageURL;
             $scope.homechef.email = $rootScope.useremail;
             $scope.homechef.subscribed_to = r.data.subscribed_to;

             if($scope.homechef.subscribed_to.length >0) {

                }

             });


       $http.get('/rest/getMealsforUser?email='+$rootScope.useremail)
            .then(function(response){
            console.log("got response from rest/getMeals ");
             //console.log(response);
             $scope.usermeals = response.data;

            });

            $scope.getCreateMeal = function(){
             //console.log("$scope.getCreateMeal CLICKED!!!!");
             $scope.mealformaction = '';

            $http.get('/rest/CreateMeal')
              .then(function(r){
              //console.log("response")
              //console.log(r)
              $scope.mealformaction = r.data.upload_url;
              //console.log("$scope.mealformaction  :" +$scope.mealformaction);

              })
            }


      $scope.mealspopup = function(meal){
        //console.log("Meals Popup data : ");

        //console.log(meal);
        $scope.mealpopupdata = meal;
        //console.log($scope.mealpopupdata);
      }

    });