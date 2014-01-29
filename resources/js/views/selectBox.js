define(["jquery", "models/nodeList", "radio", "models/zoom", "params"], function ($, nodeList, radio, zoom, params) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var selectBox = {};


	//////////////////////////////////////////////
	//											//
	//           Private variables				//
	//											//
	//////////////////////////////////////////////
	
	
	// Reference to the node the selectbox is associative with
	var selectedNode = null;
	var nodeIndex = -1;
	
	// Is the select box visible?
	var isShown = false;


	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////


	selectBox.events = function () {

		/**
		 * Broadcast
		 */
		// Broadcast when the mouse enters the selectBox
		$("#clickbox").hover(
			function() { radio("selectBox:mouseover").broadcast(this); },
			function() { radio("selectBox:mouseout").broadcast(this); }
		)

		// Broadcast when a the select field is clicked
		$("#select").click(function () { radio("node:toggleScheduled").broadcast(nodeList.selected); });


		/**
		 * Subscribe
		 */

		// When a node is selected, add the abstract etc
		radio("node:select").subscribe(showselectBox);
		//radio("node:setfocus").subscribe(showselectBox);
		
		// Hide it when unselect a node
		radio("node:deselect").subscribe(hideselectBox);
		
		// On mouseOver, cancel animation, then when the mouse leaves, 
		// restart the animation
		radio("selectBox:mouseover").subscribe(cancelAnimation);
		radio("selectBox:mouseout").subscribe(restartAnimation);

		// Event for hiding the selectbox
		radio("selectBox:hide").subscribe(hideselectBox);

		// On schedule or unschedule, change image
		radio("node:schedule").subscribe(unschedule);
		radio("node:unschedule").subscribe(schedule);
		
		// when we move the canvas, modify the position of the box:
		radio("zoom:change").subscribe(moveselectBox);
	}




	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Shows the small box that selectBoxs the graph when you click on a 
	// node
	var showselectBox = function(node, e) {
		
		if(nodeIndex != node.index) {
			// Register the node position:
			nodeIndex = node.index;
			selectedNode = node;
			
			// the select box is shown
			isShown = true;
			
			pos = svg2domPosition(selectedNode.x, selectedNode.y, zoom.pos);
			
			// set select image
			if (nodeList.isScheduled(selectedNode)) { unschedule(selectedNode.index); }
			else schedule(selectedNode.index);

			// Set download link
			$("#download a").attr("href", params['conference_data'] + selectedNode.id + ".pdf").attr("target", "_blank");

			// Change position of and fade in
			$("#clickwrap")
				.stop(true,true)
				.css("left",pos[0] + "px")
				.css("top", pos[1] + "px")
				.fadeIn();//.delay(4000).fadeOut(); 
		}else{
			isShown = false;
			$("#clickwrap").fadeOut();
			nodeIndex = -1;
			selectedNode = null;
		}
	}

	
	// when moving the canvas, we want that the clickwrap follow the node:
	
	var moveselectBox = function(zoom, e) {
		if(isShown){ 
			pos = svg2domPosition(selectedNode.x, selectedNode.y, zoom.pos);
			$("#clickwrap").css("left",pos[0] + "px")
						   .css("top", pos[1] + "px");
		}
		
	
	}
	
	var hideselectBox = function(oldSelectedNode, e) {

		if(oldSelectedNode == undefined || (selectedNode != null && selectedNode.index == oldSelectedNode.index)){

			// Register the node position:
			selectedNode = null;
			
			// the select box is shown
			isShown = false;
	
			// Change position of and fade in
			$("#clickwrap")
				.stop(true, true)
				.fadeOut();
		}
	}

	// Sets the image on the selectBox as selected
	var schedule = function() {
		$("#select img").attr("src","/img/icons/calendar.png").css("padding-top",0);	
		$("#select a").attr("title","Add to Schedule");
	}


	// Sets the image on the selectBox as deselected
	var unschedule = function() {
		$("#select img").attr("src","/img/icons/remove.png").css("padding-top","2px");	
		$("#select a").attr("title","Remove from Schedule");
	}


	// Cancels the animation in case the mouse is over the selectBox
	var cancelAnimation = function() {
		//$("#clickwrap").stop(true,true);
	}


	// Restarts the animation for when the mouse leaves the selectBox
	var restartAnimation = function() {
		//$("#clickwrap").stop(true,true).delay(500).fadeOut();
	}

	
	// COmpute the position of the selected box relatively to the browser cordinate system
	// from the position of the node relatively to the SVG canvas.
	var svg2domPosition = function(posvgx, posvgy, trans) {
		var posx = posvgx*trans.s+trans.x, 
			posy = posvgy*trans.s+trans.y;
		
		
		return [posx, posy];
	}
	// Initialize and set events
	selectBox.events();
	return selectBox;
});
