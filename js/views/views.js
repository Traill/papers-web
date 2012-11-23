define(["radio", "util/screen", "models/nodeList", "views/graph", "views/infobox", "views/sidebar", "views/selectBox", "views/menu", "views/search", "views/links", "views/zoomer"], 
	function(radio, screen, nodeList, graph, infobox, sidebar, selectbox, menu, search, links, zoomer) {


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
			});
			
			
			// Broadcast the event that the windows has been resized:
			$(window).resize(function(e) {
				var w = screen.width(),
					h = screen.height();
				
		  		radio("window:resize").broadcast(w, h, e);
			});
			
			
			
	
	}
	
	//Init
	views.init();
	
	// Return views
	return views;
});
