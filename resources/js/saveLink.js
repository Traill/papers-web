define(["jquery", "models/nodeList", "models/search"], function ($, nodeList, search) {

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
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	var save = function() {
		// If capture isn't on, return
		if (!capture) return

		// Get filters
		data.filters = search.data;

		// Get node related data
		data.scheduled = nodeList.scheduled;
		data.selected = nodeList.selected

	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	saveLink.events();
	return saveLink;
})
