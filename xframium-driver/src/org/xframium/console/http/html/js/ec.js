var xConsole = angular.module('xConsole', [ 'selectionModel', 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'ui.tree' ]);

xConsole.directive('compile', [ '$compile', function( $compile )
{
    return function( scope, element, attrs )
    {
        scope.$watch(function( scope )
        {
            return scope.$eval(attrs.compile);
        }, function( value )
        {
            element.html(value);
            $compile(element.contents())(scope);
        });
    };
} ]);

xConsole
        .controller(
                'xFramiumConsole',
                function XConsoleController( $scope, $interval, $http, $location, $uibModal, $compile, xConsoleService, $interval )
                {
                    $scope.navBarCollapsed = true;

                    $scope.htmlData = '';

                    
                    $scope.siteName;
                    $scope.modelMap;
                    $scope.fileManager;

                    $scope.currentFolder;
                    $scope.currentFile;
                    $scope.currentFolderFiles = [];
                    $scope.filterTestsBy;
                    
                    $scope.testList = [];
                    $scope.recentFiles = [];
                    $scope.applicationList;
                    $scope.cloudList;
                    $scope.deviceList;
                    $scope.globalConfig;
                    $scope.tagList = [];
                    $scope.deviceTagList = [];
                    
                    $scope.threadList = [];
                    
                    $scope.intervalPromise;
                    $scope.intervalThreadPromise;
                   
                    $scope.executionStatus = 'idle';
                   
                    $scope.getHtml = function( record )
                    {
                        var fileName = record.rootFolder.replace( /\\/g, '/' ) + "/" + record.folderName.replace( /\\/g, '/' ) + "/index.html";
                    
                        xConsoleService.getHtml( fileName ).then(function( returnValue )
                        {});
                    }
                    
                    $scope.toggle = function( scope )
                    {
                        scope.toggle();
                    }
                    
                    $scope.getElapsed = function( value )
                    {
                        return (value.stopTime - value.startTime);
                    }

                    $scope.getTotal = function( test )
                    {
                        return Object.keys(test.executions).length;
                    }
                    
                    $scope.getPassed = function( test )
                    {
                        var i=0;
                        for ( var key in test.executions ) 
                        {
                            if ( test.executions[ key ].testStatus == 'PASSED' )
                                i++;
                        }
                        
                        return i;
                    }
                    
                    $scope.getFailed = function( test )
                    {
                        var i=0;
                        for ( var key in test.executions ) 
                        {
                            if ( test.executions[ key ].testStatus == 'FAILED' )
                                i++;
                        }
                        
                        return i;
                    }
                    
                    $scope.getExecuting = function( test )
                    {
                        var i=0;
                        for ( var key in test.executions ) 
                        {
                            if ( test.executions[ key ].testStatus == 'null' )
                                i++;
                        }
                        
                        return i;
                    }
                    
                    $scope.incrementAll = function()
                    {
                        for ( var j = 0; j < $scope.testList.length; j++ ) 
                        {
                            $scope.testList[ j ].count++;
                        }
                    }
                    
                    $scope.decrementAll = function()
                    {
                        for ( var j = 0; j < $scope.testList.length; j++ ) 
                        {
                            if ( $scope.testList[ j ].count > 1 )
                                $scope.testList[ j ].count--;
                        }
                    }
                    
                    $scope.getActive = function()
                    {
                        var i = 0;
                        for ( var j = 0; j < $scope.testList.length; j++ ) 
                        {
                            if ( $scope.testList[ j ].active )
                                i++;
                        }
                        return i;
                    }

                    $scope.cancel = function()
                    {
                        $scope.modalInstance.close();
                    };
                    
                    $scope.setActive = function( active )
                    {
                        for ( var j = 0; j < $scope.testList.length; j++ ) 
                        {
                            $scope.testList[ j ].active = active;
                        }
                    }
                    
                    $scope.selectByTag = function( tag )
                    {
                        for ( var i = 0; i< $scope.testList.length; i++ ) 
                        {
                            if ( $scope.testList[i].testTags.indexOf( tag ) != -1 )
                                $scope.testList[ i ].active = true;
                        }
                    }
                    
                    $scope.setDeviceActive = function( active )
                    {
                        for ( var j = 0; j < $scope.deviceList.length; j++ ) 
                        {
                            $scope.deviceList[ j ].active = active;
                        }
                    }
                    
                    $scope.selectDeviceByTag = function( tag )
                    {
                        for ( var i = 0; i< $scope.deviceList.length; i++ ) 
                        {
                            if ( $scope.deviceList[i].tagNames.indexOf( tag ) != -1 )
                                $scope.deviceList[ i ].active = true;
                        }
                    }

                    $scope.checkThreadStatus = function()
                    {
                        xConsoleService.checkThreadStatus().then(function( returnValue )
                        {
                            $scope.threadList = returnValue.pageData;
                        });
                    }
                    
                    $scope.checkStatus = function()
                    {
                        xConsoleService.checkStatus().then(function( returnValue )
                        {
                            if ( returnValue == null )
                            {
                                $interval.cancel($scope.intervalPromise);
                                $interval.cancel($scope.intervalThreadPromise);
                            }
                            
                            if ( !returnValue.pageData.status )
                            {
                                $interval.cancel($scope.intervalPromise);
                                $interval.cancel($scope.intervalThreadPromise);
                                
                                $scope.executionStatus = 'idle';
                            }
                            
                            for ( var x=0; x<returnValue.pageData.test.length; x++ )
                            {
                                var cE = returnValue.pageData.test[ x ];
                                
                                for ( var i=0; i<$scope.testList.length; i++ )
                                {
                                    if ( $scope.testList[ i ].name == cE.testName )
                                    {
                                        var lE = $scope.testList[ i ].executions[ cE.sessionId ];
                                        if ( lE != null )
                                        {
                                            lE.stepCount = cE.stepCount;
                                            lE.testStatus = cE.testStatus;
                                            lE.startTime = cE.startTime;
                                            lE.stopTime = cE.stopTime;
                                            lE.folderName = cE.folderName;
                                        }
                                        else
                                        {
                                            $scope.testList[ i ].executions[ cE.sessionId ] = cE;
                                        }
                                    }
                                }
                            }
                        } );
                    }
                    
                    $scope.showSystemProcessing = function()
                    {
                        $scope.waitModalInstance = $uibModal.open({
                            animation : true,
                            ariaLabelledBy : 'modal-title',
                            ariaDescribedBy : 'modal-body',
                            templateUrl : 'waiting.html',
                            scope : $scope,
                            size : 'md',

                        });
                    }

                    $scope.hideSystemProcessing = function()
                    {
                        $scope.waitModalInstance.close();
                    };

                    $scope.openSuite = function( suiteName )
                    {
                        if ( $scope.dirty == true ) {
                            if ( window.confirm("Are you sure you want to discard the current work?") == false )
                                return;
                        }

                        $scope.showSystemProcessing();
                        $scope.currentStep = null;
                        $scope.currentTest = null;
                        $scope.currentContext = null;
                        $scope.selectedItem = null;

                        xConsoleService.openSuite(suiteName).then(function( returnValue )
                        {
                            $scope.testList = returnValue.pageData.sC.testList;

                            $scope.tagList = [];
                            
  
                            for ( var j = 0; j < $scope.testList.length; j++ ) 
                            {
                                var test = $scope.testList[j];
                                test.executions = {};
                                if ( test.count < 1 )
                                    test.count = 1;
                                for( var i=0; i<test.testTags.length; i++ )
                                {
                                    if ( test.testTags[ i ] != '' && $scope.tagList.indexOf( test.testTags[ i ] ) == -1 )
                                        $scope.tagList.push( test.testTags[ i ] );
                                }
                                

                                if ( test.dataProviders == null || test.dataProviders == '' || test.dataProviders == 'null' )
                                    test.dataProviders = [];
                                for ( var i = 0; i < test.dataProviders.length; i++ ) 
                                {
                                    
                                    
                                    var nameArray = test.dataProviders[i].split("=");

                                    var dpData = {};
                                    dpData['name'] = nameArray[0];

                                    if ( nameArray.length > 1 )
                                        dpData['alias'] = nameArray[1];
                                    else
                                        dpData['alias'] = '';

                                    test.dataProviders[i] = dpData;
                                }
                            }

                            $scope.siteName = returnValue.pageData.sC.siteName;
                            $scope.applications = returnValue.pageData.aC.appList;
                            
                            $scope.applicationList = returnValue.pageData.aC;
                            $scope.cloudList = returnValue.pageData.cC;

                            var favorites = returnValue.pageData.fC.favoriteList;
                            if ( favorites != null ) {
                                for ( var i = 0; i < favorites.length; i++ ) {
                                    $scope.favoriteList.push($scope.getStep(favorites[i]));
                                }
                            }

                            var newCloud = {
                                "hostName" : "127.0.0.1:4444",
                                "password" : "",
                                "provider" : {
                                    "name" : "SELENIUM",
                                    "description" : "Browsers hosted in a Selenium Grid",
                                    "id" : "1"
                                },
                                "name" : "EMBEDDED",
                                "description" : "A auto-generated EMBEDDED Selenium Grid",
                                "userName" : ""
                            }

                            $scope.cloudList.cloudList.push(newCloud);

                            $scope.deviceList = returnValue.pageData.dC.deviceList;

                            if ( $scope.deviceList != null ) {
                                for ( var i = 0; i < $scope.deviceList.length; i++ ) {
                                    
                                    var device = $scope.deviceList[i];

                                    for( var j=0; j<device.tagNames.length; j++ )
                                    {
                                        if ( device.tagNames[ j ] != '' && $scope.deviceTagList.indexOf( device.tagNames[ j ] ) == -1 )
                                            $scope.deviceTagList.push( device.tagNames[ j ] );
                                    }
                                }
                            }

                            $scope.globalConfig = returnValue.pageData.gC;

                            if ( $scope.globalConfig.testTags == 'null' )
                                $scope.globalConfig.testTags = '';

                            if ( $scope.globalConfig.stepTags == 'null' )
                                $scope.globalConfig.stepTags = '';

                            $scope.hideSystemProcessing();
                        });
                    }
                    
                    $scope.openFolderBrowser = function( suiteName )
                    {

                        $scope.modalInstance = $uibModal.open({
                            animation : true,
                            ariaLabelledBy : 'modal-title',
                            ariaDescribedBy : 'modal-body',
                            templateUrl : 'folderBrowser.html',
                            scope : $scope,
                            size : 'md',
                        });
                    }

                    $scope.getFiles = function( folderName, folderDelta )
                    {
                        xConsoleService.getFiles(folderName, folderDelta).then(function( returnValue )
                        {

                            $scope.currentFile = returnValue.pageData.fileName;

                            if ( $scope.currentFile != null ) {
                                $scope.cancel();
                                $scope.openSuite($scope.currentFile);
                            }
                            else 
                            {
                                $scope.recentFiles = returnValue.pageData.recent;
                                $scope.currentFolder = returnValue.pageData.folderName;
                                $scope.currentFolderFiles = returnValue.pageData.folderList;
                                
                            }

                        });
                    }
                    
                    $scope.executeTest = function()
                    {
                        $scope.showSystemProcessing();
                        $scope.executionStatus = 'running';
                        
                        var configData = {};
                        configData[ 'fileName' ] = $scope.currentFile;
                        var testNames = '';
                        for ( var i = 0; i < $scope.testList.length; i++ ) 
                        {
                            var test = $scope.testList[i];
                            test.executions = {};
                            
                            if ( test.active )
                            {
                                if ( testNames != '' )
                                    testNames += ',';
                                
                                testNames += test.name;
                                
                                if ( test.count > 0 )
                                    configData[ test.name + '.count' ] = test.count;
                            }
                            
                        }
                        configData[ 'driver.testNames' ] = testNames;
                        
                        var deviceNames = '';
                        for ( var i = 0; i < $scope.deviceList.length; i++ ) 
                        {
                            var device = $scope.deviceList[i];
                            
                            if ( device.active )
                            {
                                if ( deviceNames != '' )
                                    deviceNames += ',';
                                
                                deviceNames += device.key;
                            }
                        }
                        configData[ 'deviceOverrides' ] = deviceNames;
                        
                        xConsoleService.executeTest( configData ).then(function( returnValue )
                        {
                            $scope.hideSystemProcessing();
                            $scope.intervalPromise = $interval(function() {
                                $scope.checkStatus();
                            }, 1000);
                            
                            $scope.intervalThreadPromise = $interval(function() {
                                $scope.checkThreadStatus();
                            }, 3000);
                        });
                    }

                    

                    $scope.initialize = function()
                    {
                        xConsoleService.getFiles().then(function( returnValue )
                        {
                            $scope.currentFolder = returnValue.pageData.folderName;
                            $scope.currentFile = returnValue.pageData.fileName;
                            $scope.currentFolderFiles = returnValue.pageData.folderList;
                            $scope.recentFiles = returnValue.pageData.recent;
                        });

                    }

                    

                
                    
                });