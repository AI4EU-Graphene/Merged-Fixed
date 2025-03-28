/*
===============LICENSE_START=======================================================
Acumos Apache-2.0
===================================================================================
Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
===================================================================================
This Acumos software file is distributed by AT&T and Tech Mahindra
under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
This file is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
===============LICENSE_END=========================================================
*/

'use strict';


app.config(function($stateProvider, $urlRouterProvider, $httpProvider, $locationProvider, ngQuillConfigProvider, $authProvider, tagsInputConfigProvider){
	tagsInputConfigProvider.setDefaults('tagsInput', {
        placeholder: ''
    });
	ngQuillConfigProvider.set();
	$urlRouterProvider.when('','/home');
	$urlRouterProvider.otherwise('/404Error');
	$httpProvider.interceptors.push('authenticationInterceptor');
	$stateProvider
		 .state('404Error', {
	         url: '/404Error',
	         templateUrl: '/app/error-page/error-404.template.html',
	     })
	     .state('403Error', {
	         url: '/403Error',
	         templateUrl: '/app/error-page/error-403.template.html',
	     })
		.state('home', {
			url: '/home',
			component: 'marketHome'
		})
		.state('admin', {
			url: '/admin',
			component: 'admin'
		})
		.state('userDetail', {
			url: '/userDetail',
			component: 'userDetail',
			params: {
				isCatalogSelected : null,
			}
		})
		.state('forgotPswd', {
			url: '/forgotPswd',
			component: 'forgotPswd'
		})
		.state('marketPlace', {
			url: '/marketPlace',
			component: 'marketPlace',
			params : {
				isMyFavCatalogSelected: null
			}
			
		})
		.state('manageModule', {
			url: '/manageModule',
			component: 'manageModule',
			
		})
		.state('marketSolutions', {
			url: '/marketSolutions?solutionId&revisionId&publishRequestId&requestUserId&parentUrl&requestStatusCode&catalogId&catalogName',
			component: 'modelDetails',
			params: {
				solutionId : null,
				revisionId : null,
				parentUrl: null,
				publishRequestId: null,
				requestUserId: null,
				requestStatusCode: null,
				catalogId: null,
				catalogName: null
				}
         })
          .state('modelEdit', {
			url: '/modelEdit',
			component: 'modelEdit',
			params: {solutionId: null,
				revisionId: null,
				deployStatus:null,
				deployValue:null,
				deployFromDS: null}
         })
		.state('modularResource', {
			url: '/modelerResource?ONAP=?solutionId=?revisionId=?',
			component: 'modelResource'
		})
		.state('resetPswd', {
			url: '/resetPswd',
			component: 'resetPswd'
		})
		.state('adminConfig', {
			url: '/adminConfig',
			component: 'adminConfig'
		})
		.state('peerConfig', {
			url: '/peerConfig',
			component: 'peerConfig'
		})
		.state('notificationModule', {
			url: '/notificationModule',
			component: 'notificationModule'
		})
		.state('termsCondition', {
			url: '/termsCondition',
			component: 'termsCondition'
		})
		.state('confirm_verification', {
			url: '/confirm_verification?user&token',
			component: 'confirmVerification',
			params: {
				user : null,
				token : null
				}
		})
		.state('publishRequest',{
			url: '/publishRequest',
			component: 'publishRequest'
		})
		.state('onboardingHistory',{
			url: '/onboardingHistory',
			component: 'onboardingHistory'
		})
		.state('catalog',{
          url: '/catalog',
          component: 'catalog'
         })
         .state('acuCompose', {
			url: '/acuCompose',
			component: 'acuCompose'
		})
		.state('manageLicense', {
			url: '/manageLicense',
			component: 'manageLicense'
		})
		.state('managePeer', {
		url: '/managePeer?catalogName&catalogId',
		component: 'managePeer',
			params: {
				catalogName : null,
				catalogId : null
			}
		});
	
		$authProvider.facebook({
			 clientId: '1013632248810824',
	    	  authorizationEndpoint: 'https://www.facebook.com/dialog/oauth',
	    	  responseType: 'token',
	    	  redirectUri: window.location.origin,
	          requiredUrlParams: ['scope'],
	          optionalUrlParams: ['display'],
	          scope: ['profile', 'email'],
	          scopePrefix: 'openid',
	          scopeDelimiter: ' ',
	          popupOptions: { width: 500, height: 600 },
	          accessType: 'offline'

	    });

	    $authProvider.google({
	    	  clientId:'805276053124-mohnn1eltto7phq51mko2runo7aa04n6.apps.googleusercontent.com',
	          authorizationEndpoint: 'https://accounts.google.com/o/oauth2/auth',
	    	  url: 'http://localhost:8083/oauth/login',
	          responseType: 'token',
	          requiredUrlParams: ['scope'],
	          optionalUrlParams: ['display'],
	          scope: ['profile', 'email'],
	          scopePrefix: 'openid',
	          scopeDelimiter: ' ',
	          popupOptions: { width: 500, height: 600 },
	          accessType: 'offline'
	        });
	    $authProvider.github({
	    	clientId:'1587275085f20e9a68bc',
    		redirectUri: window.location.origin,
          authorizationEndpoint:'https://github.com/login/oauth/authorize',
          scope: ['user'],
          url:'http://localhost:8085',
          requiredUrlParams: ['user'],
          optionalUrlParams: ['scope'],
          popupOptions: { width: 500, height: 600 },
          responseType: 'token'
	    	});
	    
}).run(function($rootScope, $state, $location, browserStorageService) {
    $rootScope.$state = $state;
    $rootScope.$on("$locationChangeStart", function(event, next, current) { 
    		componentHandler.upgradeAllRegistered();
    		$rootScope.showNetworkError = true;
    		$rootScope.sidebarHeader = false;
    	 	
    	 	$rootScope.isMobile = false;
			(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) $rootScope.isMobile = true;})(navigator.userAgent||navigator.vendor||window.opera);
 
		   if (browserStorageService.getUserDetail()) {
				$rootScope.sidebarHeader = true;
			}
		   else{
			   $rootScope.sidebarHeader = false;
		   }
		    angular.element('.mdl-textfield').removeClass('is-focused');
			angular.element('.mdl-textfield').removeClass('is-upgraded');
			angular.element('.mdl-textfield').removeClass('is-dirty');
			angular.element('.mdl-textfield__expandable-holder input').val('');
        
    });
})
