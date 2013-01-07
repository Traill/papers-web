define(["radio", "util/screen", "models/nodeList", "views/graph", "views/infobox", "views/sidebar", "views/selectBox", "views/menu", "views/search", "views/links", "views/zoomer", "views/map", "views/message"], 
	function(radio, screen, nodeList, graph, infobox, sidebar, selectbox, menu, search, links, zoomer, map, MessageInit) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var views = {};



	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	views.init = function() {
			
		// Broadcast the event that the DOM is ready:
		$(window).ready(function(e) {
		    
		    // Give info about windows
		    var w = screen.width(),
		   		h = screen.height();
		   
			radio("domready").broadcast(w, h, e);

			resized_pub();

			radio('message').broadcast('Welcome on trailHead! <br/> You can learn how it is working on the <a href="./page/about.html">about page</a>');

		});

		// Broadcast the event that the windows has been resized:
		$(window).resize(function(e) {
			resized_pub();
		});

		// Stupid radio bug: we need to initalize:
		MessageInit();
	}

	var resized_pub = function(){
		var w = screen.width(),
			h = screen.height();

	  	radio("window:resize").broadcast(w, h);
	}
	
	//Init
	views.init();
	
	// Return views
	return views;
});
