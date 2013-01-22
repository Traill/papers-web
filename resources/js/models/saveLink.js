define(["models/nodeList", "models/search", "radio", "util/array", "util/cookie", "util/merge"], 
  function (nodeList, search, radio, arrrr, cookie, merge) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var saveLink = {};

	
	//////////////////////////////////////////////
	//											//
	//               Properties					//
	//											//
	//////////////////////////////////////////////
	saveLink.data = { scheduled:[], filters:[] }
	saveLink.capture = true;
	saveLink.id = "";



	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	saveLink.events = function() {

		// Listen for adding and deleting a filter
		radio("save:filters").subscribe(saveFilters);

		// Listen for schedule, unschedule and focus
		radio("save:nodes").subscribe(saveNodes)

	}

	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	saveLink.init = function() {

		// Load cookie
		var g = JSON.parse(cookie("graph"));
		if (typeof g == "string") g = JSON.parse(g);
		console.debug(g)

		// Check if cookie is set
		if (g == null) return;

		// Get id from url
		var id = getIDFromURL();
		if (id != "" || id != undefined) saveLink.id = id;

		// Then restore data
		restore(g);
	}




	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	saveLink.save = function(id) {
		if (id == "" || id == undefined) throw Error("Id not specified on save");
		saveLink.id = id;
		saveRemote(id);
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////


	// Saves the filters
	var saveFilters = function(filters) {

		// Take all removed filters out
		var fs = filters.filter(function(f) { return !f.removed });

		// Get filters
		saveLink.data.filters = fs.map(function(f) {
			// save filter
			var filter = merge(f, {});
			filter.to = getUnixTimeFromDate(filter.to);
			filter.from = getUnixTimeFromDate(filter.from);
			return filter;
		});

		// then save
		saveGraph();
	}


	// Saves the nodes
	var saveNodes = function(nodes) {

		// Get node related data and map for ids
		saveLink.data.scheduled = (nodes.scheduled
			.map(function(n) { return n.id;	})
			.filter(function(id) { return id != undefined; }));

		// Get selected only if it is set
		if (nodes.selected) saveLink.data.selected = nodes.selected.id

		// Then save locally
		saveGraph();
	}


	// Saves the graph both locally and remotely
	var saveGraph = function() {

		// Only save if capture is on
		if (saveLink.capture) {
			// If we have an id then save remotely
			if (saveLink.id != "" || saveLink.id == undefined) saveRemote(saveLink.id);

			// In all cases save locally
			saveLocal();
		}
	}


	// Save data
	var saveLocal = function() {

		// Convert data to json
		var data = JSON.stringify(saveLink.data);

		// Save in cookie
		cookie("graph", data)
	}



	// Saving graph to server
	var saveRemote = function(id) {
		// Make sure to punish those who don't specify an id
		if (id == "" || id == undefined) throw Error("No id specified in saveRemote");

		// Save on server
		$.ajax({
			type: "POST",
			url: "/ajax/saveGraph/" + saveLink.id,
			data: { data: JSON.stringify(saveLink.data) },
			success: saveSuccess,
			failure: saveFailure,
			dataType: "json"
		});
	}


	// What happens when we succesfully save the graph
	var saveSuccess = function(response) {
		if (response.success) ;// TODO: we need an icon the changes when graph is saved
		else radio("message").broadcast("Technical problem while saving graph");
	}


	// What happens when we fail at saving the graph
	var saveFailure = function(response) {
		radio("message").broadcast("Connection problem while saving graph");
	}


	// Get id from URL
	var getIDFromURL = function() {

		var url = document.URL.split("graph/").slice(-1)[0];
		var id = url.split("#")[0];
		return (id.indexOf("http://") == -1) ? id : "";
	}


	// Restore saved data
	var restore = function(data) {

		// Disable capture while we restore state
		saveLink.capture = false;

		// Schedule the right nodes
		nodeList.unscheduleAll();
		data.scheduled.forEach(function(id) {
			radio("node:schedule").broadcast(nodeList.getNodeFromID(id)); 
		});

		// Prepare list of selected filters
		var selected = [];

		// Add the appropriate filters
		data.filters.forEach(function (f,i) {
			if (f.selected) selected.push(i);
			var filter = merge(f,{}); // make a deep copy
			filter.to = getDateFromUnixTime(f.to);
			filter.from = getDateFromUnixTime(f.from);
			radio("filter:add").broadcast(filter);
		});

		// Select the right filters
		search.deselectAll();
		selected.forEach(function(i) {
			radio("filter:select").broadcast(i);
		});

		// Update data
		saveLink.data = data;

		// Switch back noSave flag so changes are saved
		saveLink.capture = true;

	}

	// Get the date showing the same hour and minutes as the unixtime would if
	// measured on greenwhich time
	var getDateFromUnixTime = function(unixTime) { 
		return new Date((parseInt(unixTime) + (new Date()).getTimezoneOffset()*60)*1000); 
	}

	// Get the unixtime measured from the hours and minutes in the given date
	// (and not measured from the hours and minutes of the greenwhich time
	// relatively offset from the given date)
	var getUnixTimeFromDate = function(date) { 
		// ut = d/1000 - tzo*60
		// d = (ut + tzo*60) * 1000
		return Math.round(date.getTime()/1000) - (new Date()).getTimezoneOffset()*60
	}



	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	saveLink.events();
	return saveLink;
});
