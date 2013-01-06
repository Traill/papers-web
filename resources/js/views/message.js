// Messages is a small class that let you show a popup on the top of the windows with radio
define(["jquery", "lib/jquery-class", "radio", 'params'], function ($, Class, radio, config) {

	//////////////////////////////////////////////
	//											//
	//            	   Variables				//
	//											//
	//////////////////////////////////////////////



	
	//////////////////////////////////////////////
	//											//
	//            		Init 					//
	//											//
	//////////////////////////////////////////////

	var init = function(){

		// Show a windows:
		radio("message").subscribe(function(content){
			new Message(content);
		});

		// Put it the middle:
		radio("window:resize").subscribe(middle_adjust);

		return Message;

	}

	
	
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
		
		$('#message_wrap').css('right', (w-500)/2);
		
	}


	//////////////////////////////////////////////
	//											//
	//            List of messages				//
	//											//
	//////////////////////////////////////////////

	var registered_message = {
		'node:schedule': 'The talk as been <b>added</b> to your schedule list on the right',
		'node:unschedule': 'The talk as been <b>removed</b> from your schedule list',
		'sidebar:hide': 'You closed the sidebar. You can open it anytime by click on the arrow <img src="./img/img_sidebar.png" /> on your left side of the window.'
	}


	// First time you close the sidebar, show a message
	var nbclosed = 0;
	var is_closed = false;
	radio("sidebar:hide").subscribe(function () { 
		if(nbclosed == 0){
			nbclosed++;
			//show message
			radio('message').broadcast(registered_message['sidebar:hide']);
		}
		is_closed = true;
	});
	
	radio("sidebar:show").subscribe(function () { 
		is_closed = false;
	});

	radio("node:schedule").subscribe(function(){
		if(is_closed){
			radio('message').broadcast(registered_message['node:schedule']);
		}
	});
	radio("node:unschedule").subscribe(function(){
		if(is_closed){
			radio('message').broadcast(registered_message['node:unschedule']);
		}
	});

	
	return init;
});