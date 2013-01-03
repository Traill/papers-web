define(["data/position", "util/merge", "params"], function(position, merge, config) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////

	var nodeFactory = {};



	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	
	// There are no events in node for a very good reason: If an event is
	// called it will trigger functions for every single node created. This is
	// most likely not what we want, so think about what you are doing before
	// adding an event here. Instead put an event in the nodes model and handle
	// it there



	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	
	nodeFactory.new = function(data, index) {
		
		var n =	{
					// Properties
					domNode:	null,
					links:		new Object(),
					//pos:		initPosition(index) ,
					
					//to be complient with force layout:
					x:			initPosition(data.id).x,
					y:			initPosition(data.id).y,
					weight:		1,
					index:		index,

					// Methods
					// isScheduled:	isScheduledFun,
					getAbstract:	function(callback) {
												return getAbstractFun(callback, this); },
					getCachedAbstract:	function(callback) {
														return getCachedAbstract(this) },
					getDate:		getDateFun,
					addLink:		addLinkFun,
				}

		return merge(n,data);
	}


	//////////////////////////////////////////////
	//											//
	//                Functions					//
	//											//
	//////////////////////////////////////////////
	

	// A function to add a link to a node
	var addLinkFun = function(link, target) {
		
		this.links[link.index] = { link: link, targetNode: target }
	}


	// Fetch an abstract per ajax
	var getAbstractFun = function(callback, self) {
		// If we have an abstract already, call the callback
		if(self.abstract) { if (callback != undefined) return callback(self.abstract); }

		// If not, then fetch abstract from server
		else {
			$.get("ajax/abstract/" + self.id, {}, function (data) { 
				if (data.success == true) {
					self.abstract = data.abstract;
				} else {
					self.abstract = "Not found";
				}

				if (callback != undefined) return callback(self.abstract);
			});
		}
	}
	
	// Return the cached abstract or null if it is not defined
	var getCachedAbstract = function(self) {
		return self.abstract ? self.abstract: null;
	}

	// Get date from node
	var getDateFun = function() {
		var date		= new Date(parseInt(this.time) + (new Date()).getTimezoneOffset()*60000)
		return date;
	}

	
	
	// Find initial position of the node, else create it.
	var initPosition = function(id) {
		var pos = {};
		if(position[id] == null){
			
			pos.x = config['graph_width']*Math.random();
			pos.y = config['graph_height']*Math.random();
			
		}else {
			pos = position[id];
		}
		return pos;
	
	}


	// Return the nodeFactory
	return nodeFactory;
})
