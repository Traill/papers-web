define(["radio", "util/screen", "models/zoom", 'params', 'lib/d3'], function(radio, screen, zoom, config, d3) {


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

		// On node schedule, make sure the node is sceduled in the the graph
		radio("node:schedule").subscribe(schedule);

		// // On node unschedule
		radio("node:unschedule").subscribe(unschedule);

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
		radio("link:remove").subscribe(removeLink);

		// Add a link
		radio("link:add").subscribe(addLink);


	}


	

	//////////////////////////////////////////////	
	//											//
	// 		PRIVATE FUNCTION FOR EVENTS:		//
	// 											//
	//////////////////////////////////////////////
	
	
	// Schedule a particular node
	var schedule = function(node) {
		// Update the new current node to scheduled
		node.domNode.classed("scheduled", true);
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

		// Make node red
		node.domNode.classed("selected", true);
		node.domNode.transition().attr('r', config['radius_selected']);
	}
	
	// What happends when we deselect a node
	var deselect = function(node) {
		// Make node red
		node.domNode.classed("selected", false);
		node.domNode.transition().attr('r', config['radius']);
	}


	// Highlights a node as a search result
	var searchAdd = function(node) {
		// Add search class to affected node
		node.domNode.classed("search",true);
	}


	// Removes highlight from a node that is no longer a search result
	var searchRemove = function(node) {
		// Add search class to affected node
		node.domNode.classed("search",false);
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
