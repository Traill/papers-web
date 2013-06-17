define(["filter", "radio", "models/nodeList"], function (filter, radio, nodeList) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var search = {};


	//////////////////////////////////////////////
	//											//
	//               Properties					//
	//											//
	//////////////////////////////////////////////

	// List of all filters currently added
	search.filters = [];

	// List of all data instances (in the same order as the filters
	search.data = [];

	// The currently active filter
	search.current = filter.new();

	// The currently active results
	search.results = [];


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////

	search.events = function() {

		// Listen for adding a new filter
		radio("filter:add").subscribe(search.add);

		// Listen for selecting only one filter
		radio("filter:selectOnly").subscribe(selectOnly);

		// Selecting a filter
		radio("filter:select").subscribe(select);

		// Deselecting a filter
		radio("filter:deselect").subscribe(deselect);

		// Removing a filter
		radio("filter:remove").subscribe(remove);

		// Toggling between selecting and deselecting a filter
		radio("filter:selectToggle").subscribe(selectToggle);

		// Remove a filter (Is this really necessary?)
		// radio("filter:remove").subscribe(remove);

		// Do an action on a filter (is this really necessary?)
		// radio("filter:action").subscribe(action);
		
		// Schedule all current nodes in results
		radio("filter:scheduleAll").subscribe(scheduleAll);
	}



	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Returns the different rooms used in the nodes
	search.getRooms = function() { return nodeList.getStats().rooms; }

	// Returns the first searchable date
	search.getMinDate = function() { return nodeList.getStats().minDate; }

	// Returns the last searchable date
	search.getMaxDate = function() { return nodeList.getStats().maxDate; }

	// Adds a new filter
	search.add = function(data) {

		// Create new filter and fill in the appropriate details
		var f = filter.none();

		// for each keyword/context combination, add to filter
		var new_f;
		data.context.forEach(function(c) { 
			new_f = filter.new().keyword(data.keywords, c);
			f = f.or(new_f);
		});

		// Add location and time
		f = f.interval(data.from, data.to);
		f = f.location(data.location);

		// Add filter to list of filters and data to list of data
		var index = search.filters.push(f) - 1;
		search.data[index] = data;
		search.data[index].removed = false;

		// Update current filter
		search.current = createCurrent();

		// Draw the update
		radio("filter:publish").broadcast(data, index);
		radio("filter:selectOnly").broadcast(index);
	}

	// Deselects all selected filters
	search.deselectAll = function() {

		// Deselect all currently selected filters
		search.data.forEach(function(d,i) {
			radio("filter:deselect").broadcast(i);
		});
	}

	

	//////////////////////////////////////////////
	//											//
	//            Private Functions				//
	//											//
	//////////////////////////////////////////////
	


	// Selects a filter and deselects all currently selected filters
	var selectOnly = function(index) {
		
		// Deselect all currently selected filters
		search.deselectAll();

		// Select the one filter we want
		radio("filter:select").broadcast(index);
	}


	// Toggles between select and deselect
	var selectToggle = function(index) {
		if (search.data[index].selected) radio("filter:deselect").broadcast(index);
		else radio("filter:select").broadcast(index);
	}


	// Selects another filter (doesn't deselect the old filters)
	var select = function(index) {
		
		// mark filter as selected
		search.data[index].selected = true;

		// Create current filter
		search.current = createCurrent();

		// Update graph
		updateResults();

	}


	// Deselects another filter (doesn't deselect the old filters)
	var deselect = function(index) {
		
		// mark filter as deselected
		search.data[index].selected = false;

		// Create current filter
		search.current = createCurrent();

		// Update graph
		updateResults();
	}


	// Removes a filter from the list
	// I'm not too happy with this code. A filter should ideally be assigned a unique id
	var remove = function(index) {

		// mark filter as removed
		search.data[index].removed = true;
	}


	// Creates a filter which is a combination of the selected filters
	var createCurrent = function() {

		var c = filter.none();
		search.data.forEach(function(d,i) {
			if (d.selected) c = c.or(search.filters[i]);
		});

		return c;
	}


	// Updates the graph with the search results
	var updateResults = function() {

		// Get the old nodes
		var oldNodes = search.results;

		// Get the nodes we are searching for
		search.results = search.current.nodes(nodeList.nodes);

		// For each of the old nodes, demark it
		oldNodes.forEach(function(node) { radio("search:remove").broadcast(node); });

		// For each of the new nodes, mark it
		search.results.forEach(function(node) { radio("search:add").broadcast(node); });

		// Make sure we save the data
		radio("save:filters").broadcast(search.data);
	}

	// Add all the node in the schedule
	var scheduleAll = function(){

		// For each of the new nodes, add it in the schedule
		search.results.forEach(function(node) { radio("node:schedule").broadcast(node); });

	}



	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	search.events();
	return search;
})
