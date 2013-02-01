define(["lib/d3", "radio", "params", "models/zoom", "util/screen", "models/nodeList"], function(d3, radio, config, zoom, screen, nodeList) {
	
	
	// Declare interface
		var map = {};
		
		// Initializing the position:
		map.pos = {'s': 1,  'x': 0, 'y': 0};

		// Dom element where the nodes lie:
		map.dom = null;

		// The scale factor:
		map.ratio = 1.0;

		// Size of the map:
		map.Width = 312; // TODO compute it exactly
		map.Height = 150; // TODO compute it exactly

		///////////////////////////////////////////////

		// create object for the window
		map.window = {
			// Setup the size:
				'Height': 0,
				'Width': 0
			}
		
		//////////////////////////////////////////////
		//											//
		//            Constructor 					//
		//											//
		//////////////////////////////////////////////
		
		
		map.init = function() {

			
			// Windows dom:
			map.window.dom = d3.select('#map svg').append('svg:rect')
									// .attr('x', map.mapWidth - map.initPos.x )
									// .attr('y', map.mapWidth * map.ratioWindows - map.initPos.y )
									// .attr('width', initSize.w)
									// .attr('height', initSize.h)
									.attr('fill', 'none')
									.attr('style', 'stroke:yellow;stroke-width:2;');


			
			
			// Draw the nodes in the graph
			drawNodes( nodeList.getNodes() );

			// Init the windows size:
			setWinSize();

			// Draw the window: 
			changeWindow();
			
			// Add event listener: when we move the zoom change the rectangle
			radio("zoom:change").subscribe(changeWindow);
			
			// Each time the graph is changed, change the map:
			radio("graph:changed").subscribe(drawNodes);

			// Each time the windows size change, call:
			radio("window:resize").subscribe(function(){
				setWinSize();
				changeWindow();
			});
			
		}
		
		
		//////////////////////////////////////////////
		//											//
		//            Private Functions				//
		//											//
		//////////////////////////////////////////////
		
		
		// Position of the window according to the zoom and the starting position
		var changeWindow = function() {
			
			var s =  map.pos.s / zoom.pos.s,
				x = - zoom.pos.x / zoom.pos.s * map.pos.s + map.pos.x,
				y = - zoom.pos.y / zoom.pos.s * map.pos.s + map.pos.y;
			
			map.window.dom.attr('x', 0)
						.attr('y',  0)
						.attr('width', map.window.Width  )
						.attr('height', map.window.Height )
						.attr('transform',  dmat(s, x, y) )
						.attr('fill', 'none')
						.attr('style', 'stroke:yellow;stroke-width:'+ 2 / s +';');
			
		}

		// Set the size of the windows size:
		var setWinSize = function(){

			map.window.Height = screen.height() ;
			map.window.Width = screen.width() ;

		}
		
		
		// Display the matrix transformation from the scale, translation
		function dmat(s, x, y){
		  	  return "matrix("+ s + " 0 0 " + s + " " + x + " " + y + ")";
		}


		// Function to draw the graph nodes on the canvas:
		function drawNodes(nodes) {

			// find initial position of the windows in the map:
			var stat_nodes = nodeList.computeStat();

			// Setup the variable:
			var max_ratio_w =  (map.Width - 10 ) / Math.abs(stat_nodes.max[0] - stat_nodes.min[0]) ,
				max_ratio_h =  (map.Height - 10) / Math.abs(stat_nodes.max[1] - stat_nodes.min[1]) ;


			// Setup the scale:
			map.pos.s = Math.min(max_ratio_w, max_ratio_h);


			// Setup the translation x and y to have map in the middle:
			map.pos.x = map.Width/2 - stat_nodes.center[0] * map.pos.s + 3; 

			map.pos.y = map.Height/2 - stat_nodes.center[1] * map.pos.s + 3;

			// erase the g element containing the nodes
			if(map.dom) map.dom.remove();

			// Create a new element
			map.dom = d3.select('#map svg')
							.style('background', '#fff')
							.style('stroke-width', 1)
							.style('stroke', 'rgb(100,100,100)')
							.append("svg:g")
							.attr("width", map.Width+"px")
							.attr("height", map.Height+"px")
							.attr("transform", dmat(map.pos.s, map.pos.x, map.pos.y) );
			
			
			
			
			
			// copy each nodes in the map dom element
			// and keep a reference in map.nodes:
			nodes.forEach(function(el) {
				var node = map.dom.insert('svg:circle', null)
									   .attr('cx', el.x)
									   .attr('cy', el.y)
									   .attr('r', config['radius']*map.ratio*1.5);

				

			});
			
		
		}
		
		map.init();
		return map;
	
});