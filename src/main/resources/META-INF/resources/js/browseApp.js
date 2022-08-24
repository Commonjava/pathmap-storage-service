var app = angular.module("Browse", ["ngRoute"]);

//Controller Part
app.controller("BrowseController", function ($scope, $location, $http, $cacheFactory) {

    function getParentPath ( path ) {
      if ( path.endsWith("/") ) {
        path = path.substring(0, path.length - 1) // trim the last /
      }
      parentPath = path.substring( 0, path.lastIndexOf('/') + 1 ); // e.g., "foo/bar" -> "foo/"
      if ( parentPath.length == 0 ) { // hit the root
        parentPath = "/";
      }
      return parentPath;
    }

    function render( data ) {
        $scope.dirs = []
        $scope.files = []
        for(let i = 0; i < data.length; i++) {
          item = data[i]
          if ( item.endsWith("/") ) {
            $scope.dirs.push( item );
          } else {
            $scope.files.push( item );
          }
        }
    }

    function onLocationChange() {
        path = $location.path()
        console.log("onLocationChange, path=[" + path + "]");
        if ( path === undefined || path === "" ) {
          $scope.listPath = "/";
        } else {
          $scope.listPath = path;
        }
        p = $scope.listPath;
        if ( p.endsWith("/") ) {
            $scope.parentPath = getParentPath( p );
            //console.log("listPath: " + $scope.listPath + ", parentPath: " + $scope.parentPath);
            $http({
              method: 'GET',
              url: '/api/storage/browse' + p,
              cache: true // IMPORTANT: cache the HTTP response in the default $http cache object TO avoid dup requests
            }).then(function successCallback(response) {
              render( response.data );
            }, function errorCallback(response) {
              console.log(response.statusText);
            });
        }
    }

    $scope.$on('$locationChangeSuccess', function(event) {
      onLocationChange();
    });

    $scope.refreshPageData = function( p ) {
      console.log("refreshPageData, p: " + p);
      $location.path(p)
    }

});