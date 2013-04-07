define(["jquery", "util/dateFormat", "radio"], function ($, _, radio) {

	//////////////////////////////////////////////
	//											//
	//               Variable					//
	//											//
	//////////////////////////////////////////////


	var isTimerRunning = 0;

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var infobox = {};


	function init(){

		$("#info").mouseenter(
			function(){
				var e = $("#info");

				// Clear queue first
				if(e.data('delay')) clearTimeout(e.data('delay'));
				$("#info").stop(true,true).fadeIn(0);
			});

		$("#info").mouseleave(fadeOut);
	}


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////


	infobox.events = function () {

		/**
		 * Broadcast
		 */


		/**
		 * Subscribe
		 */

		// On mouseover, fade in the infobox
		radio("node:mouseover").subscribe(fadeIn);

		// On mouseover give it a moment and then fade away
		radio("node:mouseover").subscribe(fadeOut);

		// On Click, add the abstract etc
		//radio("node:select").subscribe(setAbstract);
		radio("sidebar:hover").subscribe(setAbstract);


		// New event just for the infobox....
		radio("arrow:over").subscribe(fadeIn);
		radio("arrow:out").subscribe(fadeOut);
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////


	/**
	 * Code for fading out the infobox
	 */
	var fadeOut = function(node) {
		var e = $("#info");

		// Clear queue first
		if(e.data('delay')) clearTimeout(e.data('delay'));

		// Add a to queue:
		e.data('delay', setTimeout(function() { e.stop(true, true).fadeOut(); }, 8000));
	}

	/**
	 * Code for fading in the infobox
	 */
	var fadeIn = function(node) {

		// Get description
		$("#info").text(node.authors + ": " + node.title);
		$("#info").stop(true,true).fadeIn("fast");

		isTimerRunning = true;
	}


	/**
	 * Sets the abstract in the infobox
	 */
	function setAbstract(node) {

		// Default abstract (loader in case we are loading from the web 
		// server)
		var abstract = "<img class=\"loading\" src=\"/img/ajax-loader_dark.gif\" style=\"margin:3px 0\"/><span class=\"loading-text\">Loading Abstract...</span>";

		// get time, date, room etc
		var date		= node.getDate();
		var time		= date.format("HH:MM") + " on " + date.format("dddd mmm d, yyyy");
		var room		= "&nbsp;Room: " + node.room + "";
		var title		= node.title;
		var authors		= "By " + node.authors;

		var html		= "<p class=\"ii\" id=\"infoTitle\">" + title + "</p>";
		html		   += "<p class=\"ii\" id=\"infoAuthors\">" + authors + "</p>";
		html		   += "<p class=\"ii\" id=\"infoAbstract\">" + abstract + "</p>";
		html		   += "<p class=\"ii\" id=\"infoRoom\">" + room + "</p>";
		html		   += "<p class=\"ii\" id=\"infoTime\">" + time + ", </p>";
		html		   += "<br class=\"clear\"/>";

		// Append html
		$("#info").stop(true,true).fadeIn().html(html);

		// If the abstract isn't cached, fetch it
		// It's in the end in case we get it really fast
		node.getAbstract(function(data) {
			$("#infoAbstract").html(data); 
		});

	}

	// Set the events
	infobox.events();
	init();
	// Export the controller
	return infobox;
});
