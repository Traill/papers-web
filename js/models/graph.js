/* 
 *	This module is in charge to take the nodes and to draw
 *	the graph. It is also concerned by taking care of what
 *	happen when node are selected, and when. (DOM interaction)
 *	
 * TODO: Change every event to pass id and not the complete node object!
 */

define(["lib/d3", "util/screen", "radio", "util/levenshtein", "models/zoom", "models/nodeList", "params", "views/loader"], function(d3, screen, radio, levenshtein, zoom, nodeList, config, loader) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var graph = {}



	//////////////////////////////////////////////
	//											//
	//               Variables					//
	//											//
	//////////////////////////////////////////////

		// Dimensions
	var w = screen.width(),
		h = screen.height();

	graph.zoom = zoom;
	
	var nodes = new Array();

	//////////////////////////////////////////////
	//											//
	//               Graph Init 				//
	//											//
	//////////////////////////////////////////////

	graph.init = function () {
		
		// Our canvas.
		graph.canvas = d3.select("#graph").append("svg")
			.attr("width", "100%")
			.attr("height", "100%")
			// Enable zoom feature:
			.call(	graph.zoom )
			// Add paning g:
			.append('svg:g') 
			.attr("pointer-events", "all")
			.attr('id', 'viewport');
		
		
		//enable scrolling on the canvas:
		graph.zoom.init(graph.canvas);
		
		
		
		// Fill the node:
		nodes = nodeList.getNodes();
		
		// Force layout to recompute position
		graph.force = d3.layout.force()
						.charge(-90)
						.linkDistance(70)
						.friction(0.5)
						.theta(0.4)
						.nodes(nodes)
						.links(nodeList.links.slice(1))
						.size([config['graph_width'], config['graph_height']])
						.linkStrength( function(d, i) { return Math.log(d.value)/10; });
		
		
		graph.force.on("tick", function() {
		  graph.moveNodes();
		});
		
		// Initialize the DOM UI: 

		graph.drawNodes();
		graph.render();	
		
							
		/*
		 * We register all the event for the DOM
		 * We could not have done earlier since
		 * we initilize the DOM in the graph2.js 
		 */
		 
		 
		// Broadcast when a node is clicked
		nodes.forEach(function(node){
			node.domNode.on("click", function(d,i) { 
				var e = d3.event; 
				radio("node:click").broadcast(node, e);
				radio("node:select").broadcast(node, e);
			});
		});


		// Broadcast when the mouse enters a node
		nodes.forEach(function(node){
			node.domNode.on("mouseover", function(d, i) { 
				var e = d3.event;
				radio("node:mouseover").broadcast(node, e);
				//radio("node:current").broadcast(node.id, e);
			});
		});

		
		// Broadcast when the mouse exits a node
		nodes.forEach(function(node){
				node.domNode.on("mouseout", function(d, i) { 
				var e = d3.event;
				radio("node:mouseout").broadcast(node, e) 
			});
		});
		
		
		
			

	}

	
	//////////////////////////////////////////////
	//											//
	//           Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	graph.strokeWidth = function(d, weight) { 
		if (weight == undefined) weight = 0.1;
		return Math.sqrt(d.value/100) * weight; 
	}
	
	// Draq the edges at the bottom:
	graph.drawEdges = function() {

		nodes.forEach(function(el, j){
				el.links.forEach(function(link, i){
				if(link.domLink == null ){
						
					link.domLink = graph.canvas.insert('svg:line', ':first-child')
									  .attr('x1', el.x)
									  .attr('y1', el.y)
									  .attr('x2', link.target.x)
									  .attr('y2', link.target.y)
									  .attr('source', el.id)
									  .style("stroke-width", graph.strokeWidth(link, config["edgeSize"]))
									  .classed('link', true);

				}
			});	
		});
	
	}
	// Draw the node on the top of the document:
	graph.drawNodes = function() {
		nodes.forEach(function(el){
			el.domNode = graph.canvas.insert('svg:circle', null)
										.attr('cx', el.x)
										.attr('cy', el.y)
										.attr('r', config['radius']);
		});
	}
	
	// remove all children of the graph:
	graph.clearCanvas = function() {

		nodes.forEach(function(el){
			
			el.domNode.remove();
			el.domNode = null;
			
			el.links.forEach(function(link, i){
				if(link.domLink != null ){
					link.domLink.remove();
					link.domLink = null;
				}
			});
		});
	}
	
	graph.clearEdges = function() {
	
		nodes.forEach(function(el){
			el.links.forEach(function(link, i){

				if(link.domLink != null ){
					
					link.domLink.remove();
					link.domLink = null;
				}
			});
		});
	}
	
	graph.moveNodes = function() {
		
		nodes.forEach(function(el){
					el.domNode.attr('cx', el.x)
					  		  .attr('cy', el.y)
				});
	
	}
	
	graph.render = function(treshold, nbTotIter) {
		
		// Define some conditions to stop:
		if(treshold == null) treshold = 3.4; //3.1 is ideal
		if(nbTotIter == null) nbTotIter = 1000; // Wait less than 10s to avoid unreachead minimum 
		
		// Remove selected box:
		if(nodeList.selected != null && nodeList.selected != undefined)
			radio("node:deselect").broadcast(nodeList.selected);
		
		// Avoid user interaction:
		radio("loader:show").broadcast();
		// Count number of iterarion
		var nbIter = 0;
		
		// Delay the execution to have the curtains over
		setTimeout(function() {
				
				
				
				// Remove everything that can freeze the browser
				// Remove the edges for faster UI:
				graph.clearEdges();
				
				
				
				function recursive() {
					
					nbIter++;
					//console.log(nbIter);
					// One tick:
					graph.force.start();
					graph.force.tick()
					graph.force.stop();
					
					if(nbChanges() > treshold && nbIter < nbTotIter){ 
						setTimeout(recursive, 1);
					}else {
						// Stats:
						console.log("Nb of iteration done: "+nbIter);
						//console.log(JSON.stringify(getPositions())  );
						savePositions();
						
						setTimeout(function() {
							
							
							//graph.moveNodes();
							graph.drawEdges();
						
							// Avoid user interaction:
							radio("loader:hide").broadcast();
						}, 1);
						
					}
					
				}
				
				recursive();
				
				
				
			
			}, 100);
			
			
		
	
	}
	
	// If we want to re-render the graph,
	// let's randomize the position of each node:
	graph.randomizePosition = function(){
		
		nodes.forEach(function(el){
			el.x = config['graph_width']*Math.random();
			el.y = config['graph_height']*Math.random();
		});
		
	}

	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////


	


	// Highlights search results
	var searchHighlight = function(node) {
		// Do stuff
	}
	
	
	// Compute how much the node have changed of
	// position within one tick:
	var nbChanges = function() {
		var tot = 0;

		nodes.forEach(function(node, i) {

			tot += Math.abs(node.px-node.x) + Math.abs(node.py-node.y);
			
		});
		
		// normalization:
		tot = tot / (2*nodes.length);
		
		return tot;
		
	}
	
	var getPositions = function() {
		
		positions = {};
		
		nodes.forEach(function(el){
					positions[el.id] = {x: el.x, y: el.y};
				});
				
		return positions;
	}
	
	var savePositions = function(){
	
		var JSONPosition = getPositions();
		
		// Then send it; Nothing now. 
	}
	
	

	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	graph.init();
	return graph;
})

