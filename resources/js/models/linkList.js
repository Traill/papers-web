define(["ajax/edges", "radio", "util/array", "models/linkFactory", "models/nodeList"], 
	   function(links, radio, arrrr, linkFactory, nodeList) {


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

		// Filter duplicate links

		// Initialize the links
		linkList.links = links.map(linkList.linkNodes);
	}


	// Create a list of links, and link each link to the respective node
	linkList.linkNodes = function(data, index) {
		var l = linkFactory.new(data, index)
		var n1 = nodeList.getNodeFromID(data.source)
		var n2 = nodeList.getNodeFromID(data.target)

		n1.addLink(l, n2)
		n2.addLink(l, n1)
		
		return l;
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
