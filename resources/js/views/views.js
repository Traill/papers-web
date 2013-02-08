define(["radio", "util/screen", "models/nodeList", "views/graph", "views/infobox", "views/sidebar", "views/selectBox", "views/menu", "views/search", "views/links", "views/zoomer", "views/map", "views/message", "views/saveLink"], 
	function(radio, screen, nodeList, graph, infobox, sidebar, selectbox, menu, search, links, zoomer, map, MessageInit, saveLink) {


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
		
		// Stupid verification it's the first time:
		var first = true;
		radio("loader:hide").subscribe(function(){
			if(first) radio('message').broadcast('Welcome to TrailHead!<br/>You can learn how it works on the <a href="./page/about.html">about page</a>');	
			first = false;
		})
		
		// Broadcast the event that the DOM is ready:
		$(window).ready(function(e) {
		    
		    // Give info about windows
		    var w = screen.width(),
		   		h = screen.height();
		   
			radio("domready").broadcast(w, h, e);

			resized_pub();

			
			

			// Add a listener for each popup in the menu...
			$('.popup_w').each(function(i, el){
				$(el).attr('is_open', 0);
				$(el).children('.btn').click(function(e){
					e.preventDefault();
					if($(el).attr('is_open') == 0){
						$('.popup_w.clicked').removeClass('clicked').attr('is_open', 0);
						$(el).addClass('clicked');
						$(el).attr('is_open', 1);
					}else{
						$(el).removeClass('clicked');
						$(el).attr('is_open', 0);
					}

				});
			});
			
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
