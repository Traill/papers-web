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

	cluster.groups = new Array();
	
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
				cluster.groups[n] = data;
				cluster.render(n);
			})
		}

		// If not, render straight away
		else cluster.render(n)
	}


	// Render a clustering
	cluster.render = function(n) {

		// Get groups
		g = cluster.groups[n];

		// Now delete all the links that are going between two different clusters
		linkList.getAllLinks().forEach(function (l) {

			// If the source and target are between groups, hide link
			if (g[l.sourceNode.id] != g[l.targetNode.id]) {
				radio("link:hide").broadcast(l);
			}
			
			// Else, show it
			else radio("link:show").broadcast(l);
		});

		// Render graph
		graph.set(nodeList.getNodes(), linkList.getLinks())
	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	cluster.init();
	return cluster;
})



