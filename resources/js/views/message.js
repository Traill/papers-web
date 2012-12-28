// Messages is a small class that let you show a popup on the top of the windows with radio
define(["jquery", "lib/jquery-class", "radio", 'params'], function ($, Class, radio, config) {
	
	//////////////////////////////////////////////
	//											//
	//            		Init 					//
	//											//
	//////////////////////////////////////////////
		
	// Show a windows:
	radio("message").subscribe(function(content){
		new Message(content);
	});

	// Put it the middle:
	radio("window:resize").subscribe(middle_adjust);

	
	//////////////////////////////////////////////
	//											//
	//            Class declaration				//
	//											//
	//////////////////////////////////////////////
	
	
	var Message = Class.extend({
		init: function(content, duration){

			// duration is optional
			if(!duration) duration = 5000;

			// inject into dom
			var mess = $('<div class="message messageTop"><p>'+content+'</p></div>').appendTo('#message_wrap');
			// show in
			mess.fadeIn();

			// attach effect
			mess.delay(duration).fadeOut(500, function(){
				mess.remove();
			});
		}
	});

	//////////////////////////////////////////////
	//											//
	//            private function				//
	//											//
	//////////////////////////////////////////////

	var middle_adjust = function(w, h){
		console.log('ok');
		$('#message_wrap').css('right', (w-500)/2);
		
	}

	// Stupid bug with radio (?)
	var init = function(){

		// Put it the middle:
		radio("window:resize").subscribe(middle_adjust);

		return Message;

	}
	
	return init;
});