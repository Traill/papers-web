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
	cluster.spread = 0;
	
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
	cluster.makeClusters = function(clusterType) {

		// Check if we are unclustering:
		if (cluster.spread == 0) {
			unCluster();
		}

		// If not fetch clustering
		else {

			// Set clusterspread
			var clusterName = clusterType + (cluster.spread * 5 + 15);

			// Check if we already have the clustering for 'n'
			if (cluster.groups[clusterName] == undefined) {
				$.getJSON("ajax/clusters/" + clusterName, function(data) { 
					cluster.groups[clusterName] = toIndex(data);
					render(clusterName);
				})
			}

			// If not, render straight away
			else render(clusterName)
		}
	}


	cluster.setSpread = function(n) {
		cluster.spread = n
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


	var unCluster = function() {

		// Now delete all the links that are going between two different clusters
		linkList.getAllLinks().forEach(function (l) {

			// show link
			radio("link:show").broadcast(l);
		})

		// Render graph
		graph.set(nodeList.getNodes(), linkList.getLinks(), 300)
	}


	// Render a clustering
	var render = function(clusterType) {

		// Get groups
		g = cluster.groups[clusterType];

		// Now delete all the links that are going between two different clusters
		linkList.getAllLinks().forEach(function (l) {

			// If the source and target are between groups, hide link
			if (g[l.a] != g[l.b]) {
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



