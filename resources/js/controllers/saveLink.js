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
	saveLink.capture = false;
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


	// Saves the filters
	var saveFilters = function(filters, currentIndices) {

		// Get filters
		saveLink.data.filters = filters.map(function(f) {
			if (!f) return undefined;
			var filter = merge(f,{}); // make a deep copy
			filter.to = getUnixTimeFromDate(f.to);
			filter.from = getUnixTimeFromDate(f.from);
			return filter;
		});
		console.debug(saveLink.data.filters)

		// Get active filters
		saveLink.data.currentFilters = search.currentIndices;

		// then save locally
		saveLocal();
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
		saveLocal();
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
		console.debug(saveLink.data);
		console.debug(JSON.stringify(saveLink.data));
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
			var filter = merge(f,{}); // make a deep copy
			filter.to = getDateFromUnixTime(f.to);
			filter.from = getDateFromUnixTime(f.from);
			radio("filter:add").broadcast(filter);
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
