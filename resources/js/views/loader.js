// To be use along radio to avoid passing its reference from object ot object


define(["jquery", "radio", 'params'], function ($, radio, config) {
	
	// Declare interface
	var loader = {};
	
	
	//////////////////////////////////////////////
	//											//
	//            Constructor 					//
	//											//
	//////////////////////////////////////////////
	
	
	loader.init = function() {
	
		// Link with the dom element
		loader.dom = $('#loader');
		
		// Add event listener:
		
		// Show the loader:
		radio("loader:show").subscribe(show);

		// Hide the loader:
		radio("loader:hide").subscribe(hide);
	}
	
	
	//////////////////////////////////////////////
	//											//
	//            Private Functions				//
	//											//
	//////////////////////////////////////////////
	
	var show = function() {
		loader.dom.fadeIn(null, function() { radio("loader:isShowed").broadcast(); });
	}
	
	
	var hide = function() {
		loader.dom.fadeOut();
	}
	
	
	loader.init();
	return loader;
});