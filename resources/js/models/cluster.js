/* 
 *	This module is in charge of clustering
 */

define(["radio", "jquery", "models/linkList", "models/nodeList", "models/graph"], function(radio, $, linkList, nodeList, graph) {

	
	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var cluster = {}



	//////////////////////////////////////////////
	//											//
	//               Variables					//
	//											//
	//////////////////////////////////////////////

	cluster.groups = new Object();
	
	//////////////////////////////////////////////
	//											//
	//               Graph Init 				//
	//											//
	//////////////////////////////////////////////

	cluster.init = function () {

		// What goes here?

	}


	//////////////////////////////////////////////
	//											//
	//           Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Fetch clustering from server and render
	cluster.makeClusters = function(n) {

		// Check if we already have the clustering for 'n'
		if (cluster.groups[n] == undefined) {
			$.getJSON("ajax/clusters/" + n, function(data) { 
				console.debug(data);
				cluster.groups[n] = toIndex(data);
				render(n);
			})
		}

		// If not, render straight away
		else render(n)
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////
	

	// Converts a map from ID to index
	var toIndex = function(g) {
		var m = {};

		for (var id in g) {
			m[nodeList.getIndex(id)] = g[id];
		}

		return m;
	}


	// Render a clustering
	var render = function(n) {

		// Get groups
		g = cluster.groups[n];

		// Now delete all the links that are going between two different clusters
		linkList.getAllLinks().forEach(function (l) {

			// If the source and target are between groups, hide link
			if (g[l.a] != g[l.b]) {
				console.log(l)
				radio("link:hide").broadcast(l);
			}
			
			// Else, show it
			else radio("link:show").broadcast(l);
		});

		// Render graph
		graph.set(nodeList.getNodes(), linkList.getLinks(), 300)
	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	cluster.init();
	return cluster;
})



