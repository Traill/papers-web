define(["jquery", "radio", "params","util/array"], function ($, radio, config, arrrr) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var position = {};



	//////////////////////////////////////////////
	//											//
	//               Properties					//
	//											//
	//////////////////////////////////////////////

	// Will be something like:
	// {"ididididid": {'x':12, 'y': 13}, ... }
	var pos_data = {} ;

	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	events = function() {

		// Listen for adding and deleting a filter
		radio("graph:changed").subscribe(position.save);

	}

	//////////////////////////////////////////////
	//											//
	//              Initialize					//
	//											//
	//////////////////////////////////////////////
	position.init = function() {
			// Load the correct position:
			// Do we load it, or wait the graph.set?
			
			//this.load("default");
			return this;
	}

	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	


		
	// Get 
	position.get = function(id){
		return pos_data[id];
	}
	


	// Save data
	position.save = function(nodes, id, hasbeenloaded) {
		// No need to do if we have loaded them form the server:
		if(!hasbeenloaded){
			if(!id) id = "default";
			//if (config['save_position']) return
			data = {};
			//console.log("Saving graph "+id);
			// Get the position of the node.
			nodes.forEach(function(el){
				data[el.id] = {'x': parseInt(el.x), 'y': parseInt(el.y)}
			});

			console.log("Saving graph "+id);
			
			// Save with ajax
			$.ajax({
				type: "POST",
				url: "ajax/savePos/"+id,
				data: { data: JSON.stringify(data) },
				success: function (response) { /* nothing */ },
				dataType: "json"
			});
		}
	}

	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Loads the position
	position.load = function(id) {

		console.log("Loading graph "+id);

		// Make ajax call to get data
		$.getJSON("ajax/loadPos/"+id, function (response) {
			if (response && response != "" && !$.isEmptyObject(response) ) {
				pos_data = response;
				// Just for test
				//setTimeout(function(){
					radio("position:loaded").broadcast(position);
				//}, 80);
				
				
			}
		});

		return pos_data
	}

	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	events();
	return position.init();
})
