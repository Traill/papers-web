define(["jquery", "util/cookie", "util/array", "radio"], function($, cookie, arrr, radio) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var session = {}

	// Initialize cookie options
	session.options = { expires: 365, path: '/' };


	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	

	// Save scheduled ids
	session.saveScheduled = function(scheduled) {
		var string = scheduled.join(",");
		$.post("setGraphData/schedulePapers/" + selectedLink + "/" + string, function (data) { });
		
	}


	// Save focused
	session.saveFocused = function(focused) {
		// If there is no focused, delete focused cookie
		if (cur == undefined) $.post("setGraphData/focusPaper/" + selectedLink + "/", function (data) { });
		else $.post("setGraphData/focusPaper/" + selectedLink + "/" + cur, function (data) { });
	}


	// Load scheduled papers
	session.loadScheduled = function() {
		// get strings
		var txt;
		$.post("getGraphData/schedulePapers/" + selectedLink, function (data) { 
				txt = data;
		});
		
		var ids = txt ? txt.split(",") : [];

		// Parse scheduled and filter the NaN
		var scheduled = ids.map(function(e) { return parseInt(e); })
						  .filter(function(e) { return !isNaN(e); }); 

		return scheduled;
	}

	// Load focused paper
	session.loadFocused = function() {
		var output;
		$.post("getGraphData/focusPaper/" + selectedLink, function (data) { 
				output = parseInt(data);
		});
		
		return output;
	}

	
	return session;
})
