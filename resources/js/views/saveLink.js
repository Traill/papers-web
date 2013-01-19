define(["jquery", "controllers/saveLink", "radio"], function ($, saveLink, radio) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var saveView = {};


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////

	saveView.events = function() {

		radio("domready").subscribe(saveClick)
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

 	var saveClick = function() {
		// toggle variable for the popup
		var is_open = false;

		// Register the in the DOM
		$('#save_graph').click(function(e){

			$("#savegraph_url").html(window.location.host + "/graph/")
			$('#savegraph_val').attr('value', saveLink.id);
		});

		// $('#copy').click(function(e){
			// 	e.preventDefault();

			// 	window.clipboardData.setData('text', $('#savegraph_val').attr('value') );  
			// });
	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	saveView.events();
	return saveView;
});
