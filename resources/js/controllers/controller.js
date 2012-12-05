define(["radio", "models/nodeList", "models/linkList", "models/search", "models/graph", "views/views"], function (radio, nodeList, linkList, search, graph, views) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var controller = {};


	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////

	// initialize graph
	graph.init(nodeList.getNodes(), linkList.getSimpleLinks());

	// Add selected and current node(s) from last session
	nodeList.broadcastScheduled();

	// Return the controller
	return controller;

});
