/**
 * Created by abhilashakanitkar on 27/July/2018.
 */
/**
 * The main familyflavors app module
 *
 * @type {angular.Module}
 */

angular.module('familyflavors', ['ngRoute','ui.bootstrap','ng.httpLoader','ui.router','ngSanitize','bootstrap.fileField','ngMap'])
    .config(function ($routeProvider,$stateProvider, $urlRouterProvider) {
        'use strict';

        console.log("Interested in knowing how this works? Contact abhilasha.kanitkar@utexas.edu / jackson.ross@utexas.edu /hnoueilaty@utexas.edu / bingqing.li@utexas.edu");
        //console.log("testing");



        $urlRouterProvider.otherwise('/home');

        $stateProvider
        // HOME STATES AND NESTED VIEWS ========================================
            .state('home', {
                url: '/home',
                templateUrl: 'frontend/partial-index.html'
            })

            .state('whoarehomechefs', {
                url: '/who-are-homechefs',
                templateUrl: 'frontend/who-are-homechefs.html'
            })
            .state('createprofile', {
                url: '/createprofile',
                templateUrl: 'frontend/createprofile.html',
            })
            .state('homechefs', {

               url: '/homechefs',
                templateUrl: 'frontend/homechefs.html'
            })
            .state('homechefs.createmeal', {

                url: '/createmeal',
                templateUrl: 'frontend/createmeal.html'
            })
            .state('home.ordernow', {
                url: '/ordernow',
                templateUrl: 'frontend/ordernow.html'
            })
            .state('contact', {
                url: '/contact',
                templateUrl: 'frontend/contact.html'
            })
            .state('mobapp', {
                url: '/mobapp',
                templateUrl: 'frontend/mobapp.html'
            })
            .state('becomehomechef', {
                url: '/become-home-chef',
                templateUrl: 'frontend/become-homechef.html'
            })
        ;
    });
