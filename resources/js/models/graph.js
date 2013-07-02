/* 
 *	This module is in charge to take the nodes and to draw
 *	the graph. It is also concerned by taking care of what
 *	happen when node are selected, and when. (DOM interaction)
 *	
 * TODO: Change every event to pass id and not the complete node object!
 */

define(["lib/d3", "util/screen", "radio", "util/levenshtein", "models/zoom", "params", "views/loader", "models/nodeList", "controllers/position"], 
	   function(d3, screen, radio, levenshtein, zoom, config, loader, nodeList, position) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var graph = {}
	graph.id = "default";


	//////////////////////////////////////////////
	//											//
	//               Variables					//
	//											//
	//////////////////////////////////////////////

		// Dimensions
	var w = screen.width(),
		h = screen.height();

	graph.zoom = zoom;

	// Var to test if we have loaded some position from the server
	var positionLoaded = false;
	// Var to know if we are rendering or not:
	var isRendering = false;

	//////////////////////////////////////////////
	//											//
	//                 Events                   //
	//											//
	//////////////////////////////////////////////
	
	graph.events = function(nodes) {

		/*
		 * We register all the event for the DOM
		 * We could not have done earlier since
		 * we initilize the DOM in the graph.js 
		 */
		 
		 
		// Broadcast when a node is clicked
		nodes.forEach(function(node){
			node.domNode.on("click", function(d,i) { 
				var e = d3.event; 
				radio("node:click").broadcast(node, e);
				radio("node:select").broadcast(node, e);

				// Ok this isn't a good way to structure
				// but it's a simple hack to make it worK:
				radio("sidebar:tab").broadcast(1);
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
	//               Graph Init 				//
	//											//
	//////////////////////////////////////////////

	// Instate a new graph
	graph.set = function(nodes, links, iter) {

		// Set forcelayout
		graph.setForceLayout(nodes, links)
		
		// Initialize events
		graph.events(nodes);

		// Find the position:
		position.load(graph.id);

		// Render the updated graph if we haven't already done that 
		graph.render(nodes, links, iter);
	}


	graph.init = function(nodes, links) {

		// Our canvas.
		graph.canvas = d3.select("#graph").append("svg")
			.attr("width", "100%")
			.attr("height", "100%")
			// Enable zoom feature:
			.call(	graph.zoom )
			// Register event:
			// .on('click', function(){
			// 	radio('canvas:click').broadcast();
			// })
			// Add paning g:
			.append('svg:g') 
			.attr("pointer-events", "all")
			.attr('id', 'viewport');
		
		//enable scrolling on the canvas:
		graph.zoom.init(graph.canvas, nodeList.computeStat() );


		// Draw the nodes and edges
		graph.drawNodes(nodes);
		graph.drawEdges(nodes, links);

		// Create a new force Layout
		//graph.set(nodes, links);
		//
		
		// Listen to some events:
		
		// Listen when we have loaded the position:
		radio("position:loaded").subscribe(positionLoadedFct);

	}

	


	//////////////////////////////////////////////
	//											//
	//           Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Calculates the strokewidth
	graph.strokeWidth = function(d, weight) { 
		if (weight == undefined) weight = 0.1;
		return Math.sqrt(d.value) * weight; 
	}

	
	// Draq the edges at the bottom:
	graph.drawEdges = function(nodes, links) {
		links.forEach(function (l) {
			radio("link:add").broadcast(l);
		});
	}


	// Draw the node on the top of the document:
	graph.drawNodes = function(nodes) {
		nodes.forEach(function(el){
			el.domNode = graph.canvas.insert('svg:circle', null)
									 .attr('cx', el.x)
									 .attr('cy', el.y)
									 .attr('r', config['radius']);
		});
	}


	// Move all nodes
	graph.moveNodes = function(nodes) {
		nodes.forEach(function(el){
			el.domNode.attr('cx', el.x)
					  .attr('cy', el.y)
		});
	}


	// Move all nodes smoothly so that we have a nice animation:
	graph.moveNodesSmoothly = function(nodes, callback) {
		nodes.forEach(function(el){
			el.domNode.transition().duration(500).attr('cx', el.x);
			el.domNode.transition().duration(500).attr('cy', el.y);
		});
		setTimeout(callback, 550);
	}


	// Move all edges
	graph.moveLinks = function(links) {
		links.forEach(function(link) {
			radio("link:show").broadcast(link);
			link.domLink
				.attr('x1', nodeList.getNodeFromIndex(link.a).x)
				.attr('y1', nodeList.getNodeFromIndex(link.a).y)
				.attr('x2', nodeList.getNodeFromIndex(link.b).x)
				.attr('y2', nodeList.getNodeFromIndex(link.b).y)
		});
	}


	// Render graph
	graph.render = function(nodes, links, iter) {
		
		// Define some conditions to stop:
		var treshold = 1.5; //3.1 is ideal
		//var nbTotIter = 1000; // Wait less than 10s to avoid unreachead minimum 
		var nbTotIter = (iter) ? iter : 300; // Wait less than 10s to avoid unreachead minimum 
		
		// Set the var:
		isRendering = true;

		// Hide all edges
		radio("link:hideAll").broadcast();
		
		// Avoid user interaction:
		radio("selectBox:hide").broadcast();
		radio("loader:show").broadcast();

		// Animate the graph
		graph.animate(nodes, links, treshold, nbTotIter)
	}


	// Animate the graph
	graph.animate = function(nodes, links, treshold, iterations) {

		// Take one step
		graph.force.start();
		graph.force.tick();
		graph.force.stop();

		if(iterations % 5 == 0)
			graph.moveNodes(nodes);
		
		// Recourse
		if (nbChanges(nodes) > treshold && iterations > 0 && !positionLoaded ){ 

			setTimeout(function() { graph.animate(nodes, links, treshold, iterations-1); }, 1);
		}
		
		// Show graph
		else {

			// Stats
			//console.debug("iterations left: 	" + iterations)
			
			var end = function(){
				// Move links and display them	
				graph.moveLinks(links);
				
				// Enable user interaction:
				radio("loader:hide").broadcast();
				
				// Broadcast the event that the graph has changed:
				radio("graph:changed").broadcast(nodes, graph.id, positionLoaded);

				// Reset the positionLoaded var
				positionLoaded = false;
				// Reset the isRendering var
				isRendering = false;
			}

			// Move smoothly if we just loaded them:
			if( positionLoaded )  {
				graph.moveNodesSmoothly(nodes, end);

			}else{
				end();
			}

			
		}
	}

	
	// If we want to re-render the graph,
	// let's randomize the position of each node:
	graph.randomizePosition = function(nodes) {
		
		nodes.forEach(function(el){
			el.x = config['graph_width']*Math.random();
			el.y = config['graph_height']*Math.random();
		});
		
	}


	// initialize a d3 force-layout
	graph.setForceLayout = function(nodes, links) {

		// Force layout to recompute position
		graph.force = d3.layout.force()
						//.charge(-1000)
						//.linkDistance(40)
						//.friction(0.8)
						//.theta(0.8)
						.charge(-400)
						.linkDistance(20)
						.friction(0.8)
						.theta(0.8)
						.nodes(nodes)
						.links(links.map(function(l) { return l.simple(); }))
						.size([config['graph_width'], config['graph_height']])
						.linkStrength( function(d, i) { return d.value; });



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
	var nbChanges = function(nodes) {
		var tot = 0;

		nodes.forEach(function(node, i) {

			tot += Math.abs(node.px-node.x) + Math.abs(node.py-node.y);
			
		});
		
		// normalization:
		tot = tot / (2*nodes.length);
		
		return tot;
	}
	

	var getPositions = function(nodes) {
		
		positions = {};
		
		nodes.forEach(function(el){
			positions[el.id] = {x: el.x, y: el.y};
		});
				
		return positions;
	}

	
	// Display the position has been loaded
	var positionLoadedFct = function(){
		if(isRendering) positionLoaded = true;
	}

	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	return graph;
})

