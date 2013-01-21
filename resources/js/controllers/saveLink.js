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
	saveLink.data = {}
	saveLink.capture = false;
	saveLink.id = "";



	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	saveLink.events = function() {

		// Listen for adding and deleting a filter
		radio("filter:publish").subscribe(saveLocal);
		radio("filter:remove").subscribe(saveLocal);
		radio("filter:select").subscribe(saveLocal);
		radio("filter:deselect").subscribe(saveLocal);

		// Listen for schedule, unschedule and focus
		radio("node:schedule").subscribe(saveLocal);
		radio("node:unschedule").subscribe(saveLocal);
		radio("node:select").subscribe(saveLocal);

	}

	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	saveLink.init = function() {

		// Load cookie
		var g = JSON.parse(cookie("graph"));
		console.debug(g)

		// Check if cookie is set
		if (g == null) return;

		// If cookie is set, update id
		//saveLink.setId(g.id)

		// Then restore data
		restore(g);
	}




	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	saveLink.save = function(id) {
		saveLink.id = id;
		saveRemote(id);
	}

	// Enable saving the graph and return the id
	// saveLink.enable = function() {
	// 	var id = getNewID();
	// 	saveLink.setId(id);
	// 	save();
	// 	return id;
	// }

	// // Sets the id
	// saveLink.setId = function(id) {
	// 	saveLink.id = id;
	// 	saveLink.capture = true;
	// }

	// // Loads the graph
	// saveLink.load = function() {

	// 	var id = saveLink.id
	// 	if (id == "") throw new Error("No id set in saveLink.load()");

	// 	// Make ajax call to get data
	// 	$.getJSON("ajax/load/" + id, function (response) {

	// 		// Restore data
	// 		if (response) {
	// 			restore(response);
	// 		}
	// 	});
	// }
		


	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Update the data from the different models
	var update = function() {

		// Get filters
		saveLink.data.filters = search.data.map(function(f) {
			var filter = merge(f,{}); // make a deep copy
			filter.to = getUnixTimeFromDate(f.to);
			filter.from = getUnixTimeFromDate(f.from);
			return filter;
		});

		// Get active filters
		saveLink.data.currentFilters = search.currentIndices;

		// Get node related data and map for ids
		saveLink.data.scheduled = (nodeList.scheduled
			.map(function(n) { return n.id;	})
			.filter(function(id) { return id != undefined; }));

		// Get selected only if it is set
		if (nodeList.selected) saveLink.data.selected = nodeList.selected.id

		// Add id
		// saveLink.data.id = saveLink.id;
	}


	// Save data
	var saveLocal = function() {
		// If capture isn't on, enable saving
		//if (!saveLink.capture) saveLink.enable();

		// Update the data
		update();

		// Convert data to json
		var data = JSON.stringify(saveLink.data);

		// Save in cookie
		cookie("graph", data)
	}


	// Saving graph to server
	var saveRemote = function(id) {
		$.ajax({
			type: "POST",
			url: "ajax/saveGraph/" + saveLink.id,
			data: { data: JSON.stringify(saveLink.data) },
			success: saveSuccess,
			failure: saveFailure,
			dataType: "json"
		});
	}


	// What happens when we succesfully save the graph
	var saveSuccess = function(response) {
		console.debug(response);
	}


	// What happens when we fail at saving the graph
	var saveFailure = function(response) {
		console.debug(response);
	}


	// Get id from URL
	var getIDFromURL = function() {

		var url = document.URL.split("graph/").slice(-1)[0];
		var id = url.split("#")[0];
		return id;
	}


	// Restore saved data
	var restore = function(data) {

		// Schedule the right nodes
		nodeList.unscheduleAll();
		data.scheduled.forEach(function(id) {
			radio("node:schedule").broadcast(nodeList.getNodeFromID(id)); 
		});

		// Add the appropriate filters
		data.filters.forEach(function (f) {
			f.to = getDateFromUnixTime(f.to);
			f.from = getDateFromUnixTime(f.from);
			radio("filter:add").broadcast(f);
		});

		// Select the right filters
		search.deselectAll();
		data.currentFilters.forEach(function(index) {
			radio("filter:select").broadcast(index);
		});

		// Update data
		saveLink.data = data;
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


	var setDate = function(filter) {
		return filter;
	}


	// Create a new random id
	var getNewID = function() {

		// Let's hope we get no conflicts
		return Math.ceil(Math.random()*100000000000000).toString()
	}


	// Save a graph for the first time
	var firstSave = function() {

		// Capture future changes
		saveLink.capture = true;

		// Save name
		saveLink.id = getNewID();

		// Save
		save();
	}

	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	saveLink.events();
	return saveLink;
});
