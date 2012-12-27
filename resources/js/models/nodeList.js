define(["ajax/nodes", "radio", "controllers/session", "util/array", "util/cookie", "data/position", "models/nodeFactory"], 
	   function(nodes, radio, session, arrrr, cookie, position, nodeFactory) {

/* TRAILHEAD MODEL
 * ---------------------------------------------------
 *	
 *	this file contain the model for the nodeList
 *	
 *	
 *	---------------------------------------------------
 *	each node has the following information:
 *	
 *	id: the unique ID of the node
 *	links: the adjacents links
 *	domNode: the object displayed in the DOM
 *	x: position x of the node in the graph
 *	y: position y of the node in the graph
 *
 *	---------------------------------------------------
 *	And the link has the following informations:
 *	
 *	target: the node at the other end
 *	value: the link weight computed by our algorithm
 *		   curently we do not use it.
 *	domLink: a ref the object displayed in the DOM.
 *	---------------------------------------------------
 *
 *
 *	A node can be:
 *	
 * 	selected: we click on it and then we can do some
 *			  some action on it
 *
 *	scheduled: we have added it to our schedule.
 *
 *	Focused: The cursor was last over this node
 */


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var nodeList = {};


	//////////////////////////////////////////////
	//											//
	//               Variables					//
	//											//
	//////////////////////////////////////////////

	// Nodes and indices
	nodeList.nodes = new Array();
	nodeList.indexMap = new Object();


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////

	nodeList.events = function() {

		/**
		 * Broadcast
		 */

		// Broadcasts select or deselect based on the id
		var toggleScheduled = function(node) {
			if (nodeList.isScheduled(node)) radio("node:unschedule").broadcast(node);
			else radio("node:schedule").broadcast(node);
		}


		/**
		 * Subscribe
		 */

		// On node select, make sure the node is selected in the nodeList
		radio("node:select").subscribe(select);

		// On node scheduled, we add it to the list of scheduled nodeList
		// And change its color. 
		radio("node:schedule").subscribe(schedule);
		
		// On node unscheduled, we drop it from the scheduled list
		// and reset the UI.
		radio("node:unschedule").subscribe(unschedule);

		// Remove all scheduled nodes
		radio("node:unscheduleall").subscribe(nodeList.unscheduleAll);

		// On node focused, make sure the node is marked as focused in 
		// the nodeList
		radio("node:setfocus").subscribe(setFocus);

		// When some code calls toggleSelect, we check if the node is 
		// selected or not and call the proper event back
		radio("node:toggleScheduled").subscribe(toggleScheduled);
	};



	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	
	nodeList.init = function() {
		
		// The init function load the model of nodeList
		// It creates for each node an object with containing
		// all the related information
		
		// Load nodeList
		nodeList.nodes = nodes.map(nodeFactory.new);
		
		// Create indexMap
		nodeList.nodes.forEach(function (n) { 
			nodeList.indexMap[n.id] = n.index; 
		});


		
		// Load session
		// Load the node that are already scheduled
		//nodeList.scheduled = session.loadSelected();
		nodeList.scheduled = [];
		//nodeList.scheduled = session.loadScheduled()
									//.map(nodeList.getNodeFromIndex)
									//.filter(function(n) { return n != undefined; });
		// Load all the abstract:
		nodeList.scheduled.forEach(function(node) { node.getAbstract();});
		
		
		/*  TODO: This loading should be done in 
		 *	the future by looking session
		 * 	in the DB with Play
		 */
		nodeList.focused = nodeList.getNodeFromIndex(session.loadFocused());
		nodeList.focused = null;
		
		// TODO: save it in session and load it here.
		nodeList.selected = null;

		nodeList.stats = undefined;

	}




	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////


	// Return list of selected nodeList (nodeList.selected only contains the 
	// indices, so this function is convenient for when we need to know 
	// more


	// Returns true if the id is selected and false if it isn't
	nodeList.isScheduled = function(node) {
		//console.debug(node)
		return (nodeList.scheduled.indexOf(node) != -1);
	}


	// Broadcasts the selected nodeList and the focused nodeList. This should 
	// only be called in the initialization of the page, but I've put it 
	// apart from init() since it relies on the graph being generated
	nodeList.broadcastScheduled = function() {
		// Broadcast session
		nodeList.scheduled.forEach(function(node) { return radio("node:schedule").broadcast(node); });
		//radio("node:focused").broadcast(nodeList.focused);
	}


	// Remove all nodes from the scheduled list
	nodeList.unscheduleAll = function() {
		// Broadcast session
		nodeList.scheduled.forEach(function(node) { return radio("node:unschedule").broadcast(node); });
	}


	// Finally this function is not a hack anymore. Returns the data 
	// based on an id of a node. Look in graph.js for it's companion 
	// 'getNodeFromId'
	nodeList.getNodeFromIndex = function(index) {
		return nodeList.nodes[index];
	}
	

	// Go through all the nodes to find the nodes that have the ID.
	nodeList.getNodeFromID = function(id) {
		var index = nodeList.getIndex(id);
		return nodeList.nodes[index]
	}


	// Returns the index of a node
	nodeList.getIndex = function(id) {
		return nodeList.indexMap[id];
	}


	// Get random node
	nodeList.getRandom = function() {
		return Math.ceil(Math.random()*nodeList.nodes.length)
	}


	// Get list of nodes
	nodeList.getNodes = function() {
		return nodeList.nodes;
	}


	// Get a list of values used by the nodes
	nodeList.getStats = function() {

		if (nodeList.stats == undefined) {

			// Collect the earliest and latest date
			dates = nodeList.nodes.map(function(n) { return n.getDate(); }); 
			maxDate = dates.reduce(function(d1,d2) { return (d1 > d2) ? d1 : d2; });
			minDate = dates.reduce(function(d1,d2) { return (d1 < d2) ? d1 : d2; });

			// Collect all room types
			rooms = nodeList.nodes.map(function(n) { return n.room; }).unique();

			nodeList.stats = { rooms: rooms, maxDate: maxDate, minDate: minDate };
		}

		return nodeList.stats;
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Adds a new node to the list of selected nodeList, but only if it 
	// isn't already in the list
	var schedule = function(node) {
		// Check if id doesn't already exist
		if (!nodeList.isScheduled(node)) {
			// Add new item
			nodeList.scheduled.push(node);
			// Add a class name:
			//node.domNode.classed('scheduled', true);
			// Save changes
			//session.saveScheduled(nodeList.scheduled);
		}
	}

	// Removes the id from the list of selected nodeList
	var unschedule = function(node) {
		nodeList.scheduled = nodeList.scheduled.filter(function(n) { return (n != node); });
		//node.domNode.classed('scheduled', false);
		session.saveScheduled(nodeList.scheduled);
	}

	
	// Load the last focused node
	var setFocus = function(node) {
		nodeList.focused = node;
	}


	// Select a node (when it is clicked)
	var select = function(node) {
		// Unschedule the node scheduled before
		if (nodeList.selected) radio("node:deselect").broadcast(nodeList.selected);

		nodeList.selected = node;
	}



	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	nodeList.init();
	nodeList.events();
	return nodeList;
});
