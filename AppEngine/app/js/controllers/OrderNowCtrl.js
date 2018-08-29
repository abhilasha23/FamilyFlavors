angular.module('familyflavors')
    .controller('OrderNowCtrl', function OrderNowCtrl($scope,$rootScope, $routeParams,$http, $filter,$q,$sce,$timeout,$window,NgMap) {



        console.log("order : "+$rootScope.useremail);
        $scope.homechef = [];
        $http.get('/rest/getProfile?email='+$rootScope.useremail)
             .then(function(r){
             console.log("got response from rest/GetProfile ");

             $scope.homechef.firstname = r.data.firstname;
             $scope.homechef.lastname = r.data.lastname;
             $scope.homechef.profileimage = r.data.imageURL;
             $scope.homechef.email = $rootScope.useremail;
             $scope.homechef.subscribed_to = r.data.subscribed_to;

             //console.log($scope.homechef);

             });




 $scope.subscribeToChef = function(chef_to_subscribe){
          if(!$rootScope.useremail){
             alert("Please sign in before you Subscribe to a chef");
          }
          else{
              console.log("Subscribe to chef : "+chef_to_subscribe.chef_email);


                //homechef1_email should subscribe to homechef2_email
              $http.get('/rest/subscribeToHomeChef?homechef1_email='+$rootScope.useremail+'&homechef2_email='+chef_to_subscribe.chef_email)
                   .then(function(resp){
                      //console.log(resp);

                      $scope.homechef.subscribed_to.push(chef_to_subscribe.chef_email);

                      console.log('/rest/SendSingleEmail?emailto='+chef_to_subscribe.chef_email+'&subscribedBy='+$rootScope.useremail)
                      $http.get('/rest/SendSingleEmail?emailto='+chef_to_subscribe.chef_email+'&subscribedBy='+$rootScope.useremail)
                           .then(function(r){
                           console.log("inside send email");
                           console.log(r);
                           });
                   })
          }

        };
    })
