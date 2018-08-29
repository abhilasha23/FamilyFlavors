angular.module('familyflavors')
    .controller('CreateProfileCtrl', function CreateProfileCtrl($scope, $routeParams,$http, $filter,$q,$sce,$timeout,$rootScope,NgMap) {
// 'use strict';
    console.log("Inside CreateProfileCtrl ")

        $scope.user = new Object();
        $scope.user.email = $rootScope.useremail;
        $scope.user.lat = 30.2851042;
        $scope.user.lon = -97.737554;
        $scope.newformaction = '';
        $scope.newformaction123 = '';


         $http.get('/rest/CreateProfile')
              .then(function(r){
              //console.log("response")
              //console.log(r)
              $scope.newformaction = r.data.upload_url;
              //console.log("$scope.newformaction  :" +$scope.newformaction);
              var str1 = $scope.newformaction;
                var pattern = "8080";
                str2 = str1.substr(str1.indexOf(pattern)+pattern.length, str1.length);
               //console.log("str2: "+str2);
               $scope.newformaction123= str2; //notused right now

              })

        $scope.createProfile  = function(user){

        //console.log("inside createProfile()");
        //console.log(user);

        /*Call '/rest/createProfile' rest api end point next
        * */

        var fd = new FormData(); //Send data as formData
            fd.append('firstname', $scope.user.firstname);
            fd.append('lastname',$scope.user.lastname);
            fd.append('email',$scope.user.email);
        var config = {
                headers : {
                    'Content-Type': 'multipart/form-data'
                }
            }


        $http.post($scope.newformaction,fd,config)
            .then(function(data, status, headers, config){
               //console.log("got response from Create Profile post");
               //console.log(data);

            });

        }
    });