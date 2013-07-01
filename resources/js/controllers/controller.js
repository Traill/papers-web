define(["radio", "models/nodeList", "models/linkList", "models/search", "models/graph", "views/views", "models/saveLink", "models/cluster"], 
		function (radio, nodeList, linkList, search, graph, views, saveLink, cluster) {


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
	graph.init(nodeList.getNodes(), linkList.getAllLinks());


	// Load saved data
	saveLink.init();

	// If we have a clustering in the saved data then load that, if not render graph
	if (saveLink.hasCluster()) cluster.makeClusters();
	else graph.set(nodeList.getNodes(), linkList.getLinks(), 500)


	// Return the controller
	return controller;

});
