define(["jquery", "models/nodeList", "models/search", "radio", "util/array"], function ($, nodeList, search, radio, arrrr) {

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

		// Listen for schedule, unschedule and focus
		radio("node:schedule").subscribe(save);
		radio("node:unschedule").subscribe(save);
		radio("node:select").subscribe(save);
		//radio("node:setfocus").subscribe(save);
	}



	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	saveLink.setId = function(id) {
		saveLink.id = id;
		saveLink.capture = true;
	}

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
			f.to = Math.round(f.to.getTime() / 1000)
			f.from = Math.round(f.from.getTime() / 1000)
			return f;
		});

		// Get node related data and map for ids
		saveLink.data.scheduled = nodeList.scheduled.map(function(n) {
			return n.id;
		}).filter(function(id) { return id != undefined; });

		// Get selected only if it is set
		if (nodeList.selected) saveLink.data.selected = nodeList.selected.id

		// Set success to true
		saveLink.data.success = true;
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
			url: "ajax/save/" + saveLink.id,
			data: { data: JSON.stringify(saveLink.data) },
			success: function (response) { /* nothing */ },
			dataType: "json"
		});
	}


	// Restore saved data
	var restore = function(data) {

		console.debug(data)
		// Schedule the right nodes
		nodeList.unscheduleAll();
		data.scheduled.forEach(function(id) {
			radio("node:schedule").broadcast(nodeList.getNodeFromID(id)); });
		if (data.selected != undefined) {
			radio("node:select").broadcast(nodeList.getNodeFromID(data.selected));
		}

		// Add the appropriate filters
		data.filters.forEach(function (f) {
			search.add(f)
		});

		// Update data
		saveLink.data = data;
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
})
