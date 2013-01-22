define(["jquery", "models/saveLink", "radio"], function ($, saveLink, radio) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var saveView = {};



	//////////////////////////////////////////////
	//											//
	//               Properties					//
	//											//
	//////////////////////////////////////////////

	saveView.checking = "";
	saveView.valid = false;
	saveView.nameText = "Your name here";


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////

	saveView.events = function() {

		// Fill in information when we open the window
		$('#save_graph').click(initFields);

		// Check if the graph name is free
		$("#savegraph_val").keyup(checkName);

		// Check if the graph name is free
		$("#savegraph_val").click(clearNameField);

		// Save the graph
		$("#savegraph_button").click(saveGraph);

		// Copy the link
		$("#savegraph_link").click(selectLink);

		// Make link field unmodifiable
		$("#savegraph_link").keydown(protectLink);
	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////


	// Save the graph
	var saveGraph = function() {

		// Get value
		var name = $("#savegraph_val").val();

		console.debug("saving name " + name);

		saveLink.save(name);

		// Change url

		// Change to field with url
		$("#savegraph_link").val($("#savegraph_url").html() + name);
		$("#savegraph_form").fadeOut("fast",function() {
			$("#savegraph_done").fadeIn("fast");
		});

	}

	// Copy the link in the link field when we click
	var selectLink = function() {

		// Select the text in the field
		$("#savegraph_link").select();
	}


	// Protect link field from being modified
	var protectLink = function(e) {
		
		// TODO find way of making text field unmodifable
		//e.preventDefault();
	}


	// Initiates the values in the fields of the popup
 	var initFields = function() {

		$("#savegraph_url").html(window.location.host + "/graph/")
		$(".nameresult").hide();
		if (saveLink.id != "") $('#savegraph_val').attr('value', saveLink.id);
		else {
			$('#savegraph_val').addClass("inactive");
			$('#savegraph_val').attr('value', saveView.nameText);
		}
	}


	// Function for checking if a username exists
	var checkName = function(e) {

		// Get value
		var name = $("#savegraph_val").val();

		// Check if we have any input at all
		if (name == "") {
			$(".nameresult").hide();
			$("#savegraph_button").hide();
			return false
		}

		// If the keypress is enter and we have a valid name and we aren't
		// currently checking, then save the graph under that name
		else if (e.keyCode == 13) { // keep them nested please
			if (saveView.valid) saveGraph();
		}

		else {
			saveView.valid = false;
			$(".nameresult").stop(true,true).hide();
			$("#checkname").fadeIn("fast");
			saveView.checking = name;
			$.getJSON("/ajax/checkGraphId/" + name, checkNameResult);
			return true;
		}
	}


	// This function is triggered when we get a response from the checkGraphId event
	var checkNameResult = function(data) {

		// check if this is the last result we expected
		if (data.name != saveView.checking) return

		// hide the checking animation
		$("#checkname").stop(true, true).hide();

		// check if the name is taken
		if (data.taken) {
			saveView.valid = false;
			$("#savegraph_button").hide();
			$("#name_is_taken").fadeIn("fast");
		}
		// If it isn't then check the valid option and make a save button appear
		else {
			saveView.valid = true;
			$("#name_is_free").fadeIn("fast");
			$("#savegraph_button").fadeIn("fast");
		}
	}


	// In case no id is set, we clear the text field on click
	var clearNameField = function() {

		if (saveLink.id == "" && $("#savegraph_val").val() == saveView.nameText) {
			$('#savegraph_val').removeClass("inactive");
			$("#savegraph_val").val("");
		}
	}




	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	radio("domready").subscribe(saveView.events);
	return saveView;
});
