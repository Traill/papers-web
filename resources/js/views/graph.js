define(["radio", "util/screen", "models/zoom", 'params', 'lib/d3', "models/nodeList"], function(radio, screen, zoom, config, d3, nodes) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var graph = {}



	//////////////////////////////////////////////
	//											//
	//            Events handling				//
	//											//
	//////////////////////////////////////////////
	
	// Event initialization 
	graph.events = function () {
			
		// On node select, make sure the node is selected in the the graph
		radio("node:select").subscribe(select);

		// // On node deselect
		radio("node:deselect").subscribe(deselect);

		// On node mouseover
		radio("node:mouseover").subscribe(hover);

		// On node mouseout
		radio("node:mouseout").subscribe(hoverOut);

		// On node click, we want to try a new interface: focus on the node	
		//radio("node:click").subscribe(setFocus);

		// On node click, we want to try a new interface: focus on the node	
		radio("node:setfocus").subscribe(setFocus);

		// On search highlight result
		radio("search:add").subscribe(searchAdd);

		// Remove search highlight from past results
		radio("search:remove").subscribe(searchRemove);

		// remove a link
		radio("link:remove").subscribe(removeLink)

		// Add a link
		radio("link:add").subscribe(addLink)

	}


	

	//////////////////////////////////////////////	
	//											//
	// 		PRIVATE FUNCTION FOR EVENTS:		//
	// 											//
	//////////////////////////////////////////////
	
	
	// Schedule a particular node
	var scheduled = function(node) {

		var domNode	= node.domNode;

		// Update the new current node to scheduled
		node.classed("scheduled", true);
	}
		
		
	// unschedule a particular node
	var unschedule = function(node) {

		// Deselect it
		node.domNode.classed("scheduled", false);
	}


	// What happens when we hover over a node
	var hoverOut = function(node) {
		// Get node
		var domNode = node.domNode;

		// Make node red
		domNode.classed("hover", false);

		// Find all edges belinging to current node and update them
		for (var index in node.links) {
			var link = node.links[index];
			if(link.domLink) link.domLink.classed("hover", false);
		}
	}


	// Sets the node as the current node NON PERSISTANT
	var hover = function(node) {

		// Get node
		var domNode = node.domNode;

		// Make node red
		domNode.classed("hover", true);

	}


	
	// What happends when we select a node
	var select = function(node) {
		// Get node
		var domNode = node.domNode;

		// Be sure that we deselected the previous node:
		if(nodes.selected) radio("node:deselect").broadcast(nodes.selected);

		// Register this node as selected:
		nodes.selected = node;

		// Make node red
		domNode.classed("selected", true);
		domNode.transition().attr('r', config['radius_selected']);
	
	}
	
	// What happends when we deselect a node
	var deselect = function(node) {
		
		
		// Get node
		var domNode = node.domNode;

		// Make node red
		domNode.classed("selected", false);
		domNode.transition().attr('r', config['radius']);
		
		nodes.selected = null;
		
		
	}


	// Highlights a node as a search result
	var searchAdd = function(node) {

		// Get the domNode
		var domNode = node.domNode;

		// Add search class to affected node
		domNode.classed("search",true);
	}


	// Removes highlight from a node that is no longer a search result
	var searchRemove = function(node) {

		// Get the domNode
		var domNode = node.domNode;

		// Add search class to affected node
		domNode.classed("search",false);
	}
	
	
	// Question: Jonas where do you see setFocus?
	
	// Focus on a particular node
	var setFocus = function(node) {
		
		// Dimension
		var w = screen.width(),
			h = screen.height();
						
		// What is the scale?
		var factor = zoom.pos.s;
		// Compute the translation coeff
		var transx = factor * node.x - w/2, transy = factor * node.y - h/2;
			
			
		zoom.transitionTo(factor, [-transx, -transy] );
	
	}


	var addLink = function(target, source) {

	}


	var removeLink = function(target, source) {

	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	graph.events();
	return graph;

});
