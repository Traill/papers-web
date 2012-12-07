define(["lib/d3", "radio", "util/array", "models/nodeList", "models/graph", "params"], function(d3, radio, arrrr, nodeList, graph, config) {
	
	
	// GLOBAL VARIABLES: 
	
	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var links = {}


	//////////////////////////////////////////////
	//											//
	//               Links Init 				//
	//											//
	//////////////////////////////////////////////

    // Function that subscribe node event the 
	links.init = function () {
		
		
		/**
		 * Subscribe
		 */

		// On link selected, add little clickable	
		radio("node:select").subscribe(select);
		
		
		// On link unselected, remove every thing
		radio("node:deselect").subscribe(deselect);
		
		
		// On link selected, add little clickable	
		radio("node:mouseover").subscribe(hover);
		
		
		// On link selected, add little clickable	
		radio("node:mouseout").subscribe(hoverOut);

		// Hide All links
		radio("link:hideAll").subscribe(hideAll)

		// Hide link
		radio("link:hide").subscribe(hide)

		// Show link
		radio("link:show").subscribe(show)

		// Add link
		radio("link:add").subscribe(add)
	}
	


	//////////////////////////////////////////////
	//											//
	//            Private Functions				//
	//											//
	//////////////////////////////////////////////
	

	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////

	var select = function(node) {
	
		// Find all edges belinging to current node and update them
		for( var index in node.links ){
			link = node.links[index];

			if(!link.domLink) throw new Error("Link with index: " + link.index + " has no DOM object");
			
			var e = d3.event;
			
			link.domLink.classed('clikable', true);
			link.domLink.style("stroke-width", graph.strokeWidth(link, config["edgeSize_hover"]));
			showClickable(node, link);

			
			
		};
		
			
	}
	
	// Show a little clikable dom object to go from one node to second. 
	var showClickable = function(source, link) {
		
		var target = (link.targetNode.id == source.id) ? link.sourceNode : link.targetNode;
		 
		 // Compute the direction offset:
		 // Get the vector:
		 var rx  = parseFloat(target.x) - parseFloat(source.x), ry  = parseFloat(target.y) - parseFloat(source.y);
		 		 
		 // Normalize it:
		 rxn = rx / Math.sqrt(rx*rx+ry*ry);
		 ryn = ry / Math.sqrt(rx*rx+ry*ry);
		 
		 // Find the angle:
		 var a = 180*Math.atan2(ryn, rxn)/Math.PI+90;
		 
		 dst = Math.sqrt(rx*rx+ry*ry);
		 randomfact =  10+ Math.sqrt(dst) + 10*Math.random();
		 
		 var posx  = parseFloat(source.x) + randomfact*rxn-2, posy  = parseFloat(source.y) + randomfact*ryn-2;
		 
		 // Correction of the mean of the point:
		 posy = posy+2;
		 
		 posx = posx+2;
		 
		 link.clickable = graph.canvas.insert('svg:polygon')
		 						.attr('points', '-33.001,24.991 0,18.687 33,24.991 -0.309,-24.991') //57.042,22.06 0,-5.159 -57.042,22.06 -57.042,5.159 0,-22.06 57.042,5.159
		 						//.attr('height', 4)
		 						//.attr('width', 4)
		 						.attr('fill', '#990C00')
		 						.classed('handle', true)
		 						.attr('transform', "translate("+posx+", "+posy+") scale("+0.1+") rotate("+a+")" );
		 						//.attr('y', );
		
		
		link.clickable.on('mouseover', function() {
		link.clickable.transition().attr('transform', "translate("+posx+", "+posy+") scale("+0.12+") rotate("+a+")" );}); 
		link.clickable.on('mouseout', function() {  link.clickable.transition().attr('transform', "translate("+posx+", "+posy+") scale("+0.1+") rotate("+a+")" );});
		
		link.clickable.on('click', function () { 
				
				var e = d3.event;
				radio('node:deselect').broadcast(source, e);
				
				radio('node:select').broadcast(link.targetNode, e);
				radio('node:setfocus').broadcast(link.targetNode, e);
				
		} );
	}


	// remove all the clickable item of the old node.
	var deselect = function(node) {
		 
		 
		 for( var index in node.links ){
			if(!link.domLink) throw new Error("Link with index: " + link.index + " has no DOM object");
			link = node.links[index];
			var e = d3.event;
			link.clickable.remove();
			link.domLink.style("stroke-width", graph.strokeWidth(link, config["edgeSize"]));
			link.domLink.classed('clikable', false);
			
		};
	}
		
	var hover = function(node) {
		
		for (var index in node.links) {
			var link = node.links[index];
			if(!link.domLink) throw new Error("Link with index: " + link.index + " has no DOM object");
			
			var e = d3.event;
			link.domLink.classed('hover', true);
			link.domLink.style("stroke-width", graph.strokeWidth(link, config["edgeSize_hover"]));
			
		}
	}
	
	var hoverOut = function(node) {
		
		if( nodeList.selected == null || nodeList.selected.index != node.index){
			
			for (var index in node.links) {
				var link = node.links[index];
				if(link.domLink) {
					var e = d3.event;
					// Check if note selected
					link.domLink.classed('hover', false);
					link.domLink.style("stroke-width", graph.strokeWidth(link, config["edgeSize"]));
				}
			}
		}
	}


	// Hide all links
	var hideAll = function() {
		graph.canvas.selectAll("line").style("display","none")
	}

	// Hide just one link
	var hide = function(link) {
		link.domLink.style("display","none");
	}

	// Show just one link
	var show = function(link) {
		link.domLink.style("display", "inline");
	}


	var add = function(link) {

		// Throw an error if domlink already exists
		if (link.domLink != null) throw new Error("Link with index: " + link.index + " has already been added to DOM once");

		link.domLink = graph.canvas
							.insert('svg:line', ':first-child')
							.attr('x1', link.sourceNode.x)
							.attr('y1', link.sourceNode.y)
							.attr('x2', link.targetNode.x)
							.attr('y2', link.targetNode.y)
							.attr('source', link.sourceNode.id)
							.style("stroke-width", graph.strokeWidth(link, config["edgeSize"]))
							.classed('link', true);
							
	}





	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	links.init();
	return links;
});
