define(["jquery", "radio", "params", "models/zoom", "util/screen", 'js!lib/jquery/jquery-ui-1.9.1.custom.min.js!order'], function($, radio, config, zoom, screen ) {

	
	/////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var zoomer = {};

	zoomer.pos
	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	
	zoomer.events = function () {
	
		/**
		 * Broadcast
		 */
	
	
	
		/**
		 * Subscribe
		 */
	
		radio("zoom:change").subscribe(changeZoomer);
	}
	
	
	//////////////////////////////////////////////
	//											//
	//                  Init					//
	//											//
	//////////////////////////////////////////////
	
	zoomer.init = function() {
		
		// Set parameter
		zoomer.wrapper = $('#zoomer');
		zoomer.height = $('#scale').css('width').replace(/[^-\d\.]/g, '');
		zoomer.isDragging = false;
		zoomer.increment =  (config['zoomMax'] - config['zoomMin']) / config['nbIncrement'];
		
		// Place initially:
		changeZoomer();
		
		// Set up the UI:
		$( "#indicator" ).draggable({ axis: "x", containment: $("#scale"), drag: updateZoom, start: function() { zoomer.isDragging = true; }, stop: function() { zoomer.isDragging = false; }});
		
		
		// Plus:
		$( "#zoomer .plus" ).on('click', function() {
			zoom.moveTo(zoom.pos.s + zoomer.increment, [zoom.pos.x - zoom.pos.x*(zoom.pos.s+zoomer.increment), zoom.pos.y - zoom.pos.y*(zoom.pos.s+zoomer.increment) ]);
		});
		
		// Minus:
		$( "#zoomer .moins" ).on('click', function() {
			zoom.moveTo(zoom.pos.s - zoomer.increment, [zoom.pos.x, zoom.pos.y]);
		});
		
		
		// Set event:
		zoomer.events();
	
	}
	
	var changeZoomer = function() {
		$('#zoom_val').attr('value', Math.round((zoom.pos.s-config['zoomMin'])/(0.975*(config['zoomMax']-config['zoomMin']))*100)+"%" );

		if(!zoomer.isDragging)
			$('#indicator').css('left', getPosition(zoom.pos.s) );
	
	}
	
	// Update the zoom to follow the indicator:s
	var updateZoom = function(event, ui) {
		var s = $('#indicator').css('left').replace(/[^-\d\.]/g, '')/zoomer.height*config['zoomMax']+config['zoomMin'];
		zoom.moveTo(s, 
			[
				zoom.pos.x + ( zoom.pos.s-s ) * screen.width() / 2, 
			 	zoom.pos.y + ( zoom.pos.s-s) * screen.height() / 2 
			]);
	}
	
	var getPosition = function(t){

		return zoomer.height * (t - config['zoomMin'])/config['zoomMax'];
		
	}
	
	
	
	
	// Export the controller
	zoomer.init();
	return zoomer;

});