define(["ajax/edges", "radio", "util/array", "models/linkFactory"], 
	   function(links, radio, arrrr, linkFactory) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var linkList = {};


	//////////////////////////////////////////////
	//											//
	//               Variables					//
	//											//
	//////////////////////////////////////////////

	linkList.links = new Array();
	linkList.hidden = new Object();

	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////


	linkList.events = function() {

		// Add a link to dom (makes it visible)
		radio("link:show").subscribe(showLink)

		// Removes a link from dom (makes it invisible)
		radio("link:hide").subscribe(hideLink)

	}


	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	
	linkList.init = function() {

		// Initialize the links
		linkList.links = links.map(linkFactory.new);

		// Link the nodes
		linkList.linkNodes();

	}


	linkList.linkNodes = function() {

		// Add links to the nodes
		linkList.links.forEach( function(link) {
			link.sourceNode.addLink(link);
			//link.targetNode.addLink(link);
		});
	}


	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////


	// Returns a list of all links with nodes attached
	linkList.getAllLinks = function() {
		return linkList.links;
	}


	// Returns a list of visible links with nodes attached
	linkList.getLinks = function() {
		return linkList.links.filter(isVisible)
	}




	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	var isVisible = function(link) {
		return (linkList.hidden[link.index] == undefined)
	}

	var isHidden = function(link) {
		return (linkList.hidden[link.index] != undefined)
	}

	var showLink = function(link) { linkList.hidden[link.index] = undefined; }

	var hideLink = function(link) { linkList.hidden[link.index] = link; }


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	linkList.init();
	linkList.events();
	return linkList;
});
