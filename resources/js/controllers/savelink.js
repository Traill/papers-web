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
		radio("filter:publish").subscribe(save);
		radio("filter:remove").subscribe(save);
		radio("filter:select").subscribe(save);
		radio("filter:deselect").subscribe(save);

		// Listen for schedule, unschedule and focus
		radio("node:schedule").subscribe(save);
		radio("node:unschedule").subscribe(save);
		radio("node:select").subscribe(save);

	}

	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	saveLink.init = function() {

		// Load cookie
		var g = JSON.parse(JSON.parse(cookie("graph")))

		// Check if cookie is set
		if (g == null || g.id == undefined) return;

		// If cookie is set, update id
		saveLink.setId(g.id)

		// Then restore data
		restore(g);

		

		
	}

	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Enable saving the graph and return the id
	saveLink.enable = function() {
		var id = getNewID();
		saveLink.setId(id);
		save();
		return id;
	}

	// Sets the id
	saveLink.setId = function(id) {
		saveLink.id = id;
		saveLink.capture = true;
	}

	// Loads the graph
	saveLink.load = function() {

		// Get id from url
		//var url = document.URL.split("graph/").slice(-1)[0];
		//var id = url.split("#")[0];
		var id = saveLink.id
		if (id == "") throw new Error("No id set in saveLink.load()");

		// Make ajax call to get data
		$.getJSON("ajax/load/" + id, function (response) {

			// Restore data
			if (response) {
				restore(response);
			}
		});
	}
		


	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Update the data from the different models
	var update = function() {

		// Get filters
		saveLink.data.filters = search.data.map(function(f) {
			var filter = merge(f,{});
			filter.to = getUnixTimeFromDate(f.to);
			filter.from = getUnixTimeFromDate(f.from);
			return filter;
		});

		// Get active filters
		saveLink.data.currentFilters = search.currentIndices;

		// Get node related data and map for ids
		saveLink.data.scheduled = nodeList.scheduled.map(function(n) {
			return n.id;
		}).filter(function(id) { return id != undefined; });

		// Get selected only if it is set
		if (nodeList.selected) saveLink.data.selected = nodeList.selected.id

		// Set success to true
		saveLink.data.success = true;

		// Add id
		saveLink.data.id = saveLink.id;
	}


	// Save data
	var save = function() {
		// If capture isn't on, return
		if (!saveLink.capture) return

		// Update the data
		update();

		// Save with ajax
		$.ajax({
			type: "POST",
			url: "ajax/saveGraph/" + saveLink.id,
			data: { data: JSON.stringify(saveLink.data) },
			success: function (response) { /* nothing */ },
			dataType: "json"
		});
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
