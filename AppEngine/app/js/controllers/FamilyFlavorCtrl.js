angular.module('familyflavors')
    .controller('FamilyFlavorCtrl', function FamilyFlavorCtrl($scope,$rootScope, $routeParams,$http, $filter,$q,$sce,$timeout,$window,NgMap) {
// 'use strict';

        //console.log("You are here in FamilyFlavorCtrl - main page controller")

            NgMap.getMap().then(function(map) {
                console.log(map.getCenter());
                console.log('markers', map.markers);
                console.log('shapes', map.shapes);
              });
              $scope.googleMapsUrl="https://maps.googleapis.com/maps/api/js?key=AIzaSyDnoaoZ_ndo-JDzXoQwgnjeBOzoFaH8sYg";


        /* Just Testing if the REST API end poinst are working or not*/
        $scope.tags=[];
        $http.get('/rest/getTagsList')
             .then(function(resp){
             //console.log("got tags in system");
             //console.log(resp.data)
             tmp = resp.data;
             for (i=0;i<tmp.length;i++){
                $scope.tags.push(tmp[i].tag);
             }
             //console.log($scope.tags);

             });


        $http.get('/rest/loadMainData')
            .then(function(r){

                $scope.url = r.data.url;
                $scope.url_linktext = r.data.url_linktext;

                $rootScope.useremail = r.data.user_email;


                if($scope.url_linktext == 'Login'){
                  $rootScope.isAuthenticatedFlag = false;
                   //$window.location.href = $scope.url;
                }
                else {
                $rootScope.isAuthenticatedFlag = true;
                }

                 var str1 = $scope.url;
                  var pattern = "/rest/";
                  str2 = str1.substr(0,str1.indexOf(pattern))+"/";
                  $scope.url = str2;
            })
            .then(function(resp){
                    //console.log("Calling IsAuthenticated");
                    $http.get('/rest/IsAuthenticated?email='+$rootScope.useremail)
                     .then(function(r){
                       //console.log("RECEIVED response from IsAuthenticated");
                       //console.log(r);
                       //Commenting the below code for now
                       if(r.data.AuthenticationStatus == 'true'){

                       $http.get('/rest/checkProfileExists?email='+$rootScope.useremail)
                             .then(function(response){

                             //console.log(response);
                             $scope.CheckProfileExist = response.data.profileExist;


                            if ($scope.CheckProfileExist=="true"){
                            //console.log("$rootScope.isAuthenticatedFlag && $scope.CheckProfileExist")
                            $scope.pageflag = 'homechefs';
                            }


                               else if ($scope.CheckProfileExist=='false') {
                               //console.log("$scope.isAuthenticatedFlag && !$scope.CheckProfileExist")
                               $scope.pageflag = 'createprofile';
                               }


                       //console.log("$scope.pageflag : "+$scope.pageflag);

                             })
                       }
                       else {
                       $scope.CheckProfileExist = false;
                       }


                     })
           })

           ;



        /*End of Testing code*/

        /* Above request calls /rest/isAuthenticated and /rest/CheckProfileExists  and sets a flag.
        * If the user is Authenticated, and the homechef profile exists then redirect him to homechefs.html
        * Else if user is Authenticated, but the homechef profile is not present - redirect him to createprofile.html
        * if user is not Authenticated, send him to Google authentication page, and then send him to createprofile/ homechef page based
        * on the situation
        *
        * */


        // Below are functions specific to OrderNow page

        //gets a list of homechefs
        $http.get('/rest/HomeChefs')
        .then(function(r){

         //console.log("resp from HmeChefs");
         //console.log(r);
         $scope.homechefslist = r.data;
        })

        $scope.searchTag = '';

         $scope.onchange = function(id){

           $scope.searchTag = id;
           //console.log("onchange: "+ $scope.searchTag);
         }

        $scope.onSelect = function(item,model,label){
                $scope.searchTag = item;
                //console.log("onSelect : "+$scope.searchTag);
            }

        $scope.selectedThemesArray = [];
        //$scope.tags = ['spicy','indian','chinese','italian','pasta']

        $scope.selectThemeFilter = function(themename){
             //console.log("Theme selected : "+themename);
             if($scope.selectedThemesArray.indexOf(themename) < 0) { //means theme does not exists
                 $scope.selectedThemesArray.push(themename)
             }
             else {
             $scope.selectedThemesArray.remove(themename)
             }

                console.log(($scope.selectedThemesArray).toString());

            };


       $scope.selectLocFilter = function(loc){
          console.log("loc selected :"+loc) ;
          l = loc.split(',');
          console.log(l[0]);
          console.log(l[1]);
        };


       $scope.mealsresult = []

       $scope.searchMeals = function(){

            $scope.mealsresult = [];
            $scope.mealresultmsg = '';
            var str = '/rest/searchMeals';
            if($scope.selectedThemesArray.length>0){
              str = str+'?theme='+($scope.selectedThemesArray).toString();
            }
            if($scope.searchTag!=''){
              if(str == '/rest/searchMeals'){
              str = str+'?tags='+$scope.searchTag;
              }
              else {
              str = str+'&tags='+$scope.searchTag;
              }

            }


            $http.get(str)
                 .then(function(r){

                   $scope.mealsresult = r.data;


                   for(i=0;i<$scope.mealsresult.length;i++){

                   var obj = findObjectByKey($scope.homechefslist, 'chef_email', $scope.mealsresult[i].meal_chef);
                   $scope.mealsresult[i].chef_firstname = obj.chef_firstname;
                   $scope.mealsresult[i].chef_lastname = obj.chef_lastname;
                   }

                   if($scope.mealsresult.length == 0){
                    $scope.mealresultmsg = "No Meals found, remove some filters and try again!";
                   }

                   console.log($scope.mealsresult);

                 });


        };


        $scope.chefprofilepopup = function(chef){
          //console.log("Chef profile popup");
          $scope.chefpopupdata = chef;
          //console.log($scope.chefpopupdata);

          $http.get('/rest/getMealsforUser?email='+chef.chef_email)
            .then(function(response){
            //console.log("got response from rest/getMeals in popup ");
             //console.log(response);
             response.data;
              $scope.chefpopupdata.meals = response.data;
             $scope.chefpopupdata.noOfMeals = response.data.length;
             //console.log($scope.chefpopupdata);

            });


        };




    Array.prototype.remove = function(value) {
            if (this.indexOf(value)!==-1) {
                this.splice(this.indexOf(value), 1);
                return true;
            } else {
                return false;
            }
        };
   function findObjectByKey(array, key, value) {
    for (var i = 0; i < array.length; i++) {
        if (array[i][key] === value) {
            return array[i];
        }
    }
    return null;
}

});
