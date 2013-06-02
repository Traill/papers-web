define(["ajax/edges", "radio", "util/array", "models/linkFactory", "models/nodeList"], 
	   function(jsonLinks, radio, arrrr, linkFactory, nodeList) {


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
		var links = filterDuplicates(jsonLinks);

		// Normalize weights
		links = normalize(links);

		// Initialize the links
		linkList.links = links.map(linkNodes);
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


	linkList.isHidden = function(link) {
		return (linkList.hidden[link.index] == link)
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////


	// Filter all links between two nodes that are already present
	var filterDuplicates = function(links) {

		var m = {};

		// Construct map
		links.forEach(function (l) {
			m[l.target + l.source] = l.value
		});

		// Filter all links that we
		var ls = links.filter(function(l) {
			if (m[l.source + l.target] != undefined) {
				m[l.target + l.source] = undefined
				return false;
			}
			else return true;
		});

		return ls;
	}


	// Normalize the values of the links
	var normalize = function(links) {

		var max = 0;

		links.forEach(function (l) {
			if (max < l.value) max = l.value;
		});

		return links.map(function(l) {
			l.value = l.value / max;
			return l;
		});
	}


	// Create a list of links, and link each link to the respective node
	var linkNodes = function(data, index) {
		var l = linkFactory.new(data, index)
		var n1 = nodeList.getNodeFromID(data.source)
		var n2 = nodeList.getNodeFromID(data.target)

		n1.addLink(l, n2)
		n2.addLink(l, n1)
		
		return l;
	}


	var isVisible = function(link) {
		return (linkList.hidden[link.index] == undefined)
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
