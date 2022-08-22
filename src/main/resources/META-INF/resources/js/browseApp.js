var app = angular.module("Browse", ["ngRoute"]);

//Controller Part
app.controller("BrowseController", function ($scope, $location, $http) {
    function getFilename ( path ) {
      return path.substring( path.lastIndexOf('/') + 1 );
    }

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

    /*function downloadFile( url ) {
        const link = document.createElement('a');
        link.setAttribute('target', '_blank');
        link.setAttribute('href', url);
        link.setAttribute('download', getFilename(url) );
        document.body.appendChild(link);
        link.click();
        link.remove();
    }*/

    function setDirsAndFiles( data ) {
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

    $scope.onLocationChange = function() {
        path = $location.path()
        if ( path === undefined || path === "" ) {
          $scope.listPath = "/";
        } else {
          $scope.listPath = path;
        }
        $scope.refreshPageData( $scope.listPath );
    }

    $scope.refreshPageData = function( p ) {
      //console.log("refreshPageData, p: " + p);
      if ( p.endsWith("/") ) {
        $scope.listPath = p;
        $scope.parentPath = getParentPath( p );
        $location.path(p)
        //console.log("listPath: " + $scope.listPath + ", parentPath: " + $scope.parentPath);
        $http({
          method: 'GET',
          url: '/api/storage/browse' + p
        }).then(function successCallback(response) {
          setDirsAndFiles( response.data );
        }, function errorCallback(response) {
          console.log(response.statusText);
        });
      }/* else {
        downloadFile( "/api/storage/content" + p );
      }*/
    }

    $scope.$on('$locationChangeSuccess', function(event) {
      $scope.onLocationChange();
    });

    //Now load the data from server
    $scope.onLocationChange();
});