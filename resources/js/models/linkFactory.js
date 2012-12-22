define(["models/nodeList"], function(nodeList) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////

	var linkFactory = {};



	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	
	// There are no events in link for a very good reason: If an event is
	// called it will trigger functions for every single link created. This is
	// most likely not what we want, so think about what you are doing before
	// adding an event here. Instead put an event in the nodes model and handle
	// it there



	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	
	linkFactory.new = function(data, index) {
		console.debug(data)

		var l =	{
					// Dom Properties
					domLink:	null,
					
					// Link properties
					value:		data.value,
					a:			nodeList.getNodeFromID(data.source).index,
					b:			nodeList.getNodeFromID(data.target).index,
					index:		index,

					// Functions
					simple:		simpleFun
				}

		return l;
	}


	//////////////////////////////////////////////
	//											//
	//                Functions					//
	//											//
	//////////////////////////////////////////////
	

	// A function to get a simple link to use with force
	//
	// I don't know what's up here. If I return the link, it returns a link
	// containing the nodes. If I don't, it contains a link containing the
	// indices
	var simpleFun = function() {
		var l = { target: this.a, source: this.b, value: this.value };
		//console.debug(l)

		return l;
	}


	// Return the linkFactory
	return linkFactory;
})
