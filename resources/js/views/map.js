define(["lib/d3", "radio", "params", "models/zoom", "util/screen", "models/nodeList"], function(d3, radio, config, zoom, screen, nodeList) {
	
	
	// Declare interface
		var map = {};
		
		// Initializing the position:
		//map.pos = zoom.pos;
		// Initialize the nodes references array
		map.nodes = new Array();
		
		//////////////////////////////////////////////
		//											//
		//            Constructor 					//
		//											//
		//////////////////////////////////////////////
		
		
		map.init = function() {
		
			// Link with the dom element
			map.dom = d3.select('#map svg')
							.style('background', '#fff')
							.style('stroke-width', 1)
							.style('stroke', 'rgb(100,100,100)');
			
			// Ratio of the size of the windows:
			map.ratioWindows = screen.height()/ screen.width();

			// Size of the map:
			map.mapWidth = 312; // TODO compute it exactly
			
			// Compute the ratio between the real g canvas dimension and the map:
			map.ratio = map.mapWidth/(screen.width() / config['zoomInit'])/2;

			// find initial position
			map.initPos = initPosition();
			
			// find initial dimension:
			initSize = {"w": map.mapWidth / config['zoomInit'],  "h": map.mapWidth / config['zoomInit'] * map.ratioWindows};
			
			
			map.window = map.dom.append('svg:rect')
									.attr('x', map.initPos.x + zoom.pos.x * map.ratio )
									.attr('y', map.initPos.y + zoom.pos.y * map.ratio)
									.attr('width', initSize.w)
									.attr('height', initSize.h)
									.attr('fill', 'none')
									.attr('style', 'stroke:yellow;stroke-width:2;');
			
			// Add event listener: when we move the zoom change the rectangle
			radio("zoom:change").subscribe(changeWindow);
			
			// Draw the nodes in the graph
			drawNodes( nodeList.getNodes() );
			
			// Each time the graph is changed, change the map:
			radio("graph:changed").subscribe(drawNodes);
			
		}
		
		
		//////////////////////////////////////////////
		//											//
		//            Private Functions				//
		//											//
		//////////////////////////////////////////////
		
		
		// Position of the window according to the zoom and the starting position
		var changeWindow = function() {
		
			// find position of the windows in the map:
			Pos =  {"x": zoom.pos.x * map.ratio , "y": zoom.pos.y * map.ratio };
			
			// find dimension:
			SizeW ={"w": map.mapWidth / zoom.pos.s  ,  "h": map.mapWidth / zoom.pos.s  * map.ratioWindows };
			
			
			map.window.attr('x', Pos.x + map.initPos.x*zoom.pos.s  )
						.attr('y', Pos.y + map.initPos.y*zoom.pos.s)
						.attr('width', SizeW.w)
						.attr('height', SizeW.h)
						.attr('fill', 'none')
						.attr('style', 'stroke:yellow;stroke-width:2;');
			
		}
		
		// Function to recompute the inital position of the windows and nodes
		// based of the mean position of the graph
		
		var initPosition = function() {
		
			// find initial position of the windows in the map:
			// first compute the graph center to put it in the middle of our windows:
			centerG = computeCenter();
			return {"x": map.mapWidth/2 - centerG[0] * map.ratio + zoom.pos.x * map.ratio / zoom.pos.s, "y": map.mapWidth*map.ratioWindows/2 - centerG[1] * map.ratio + zoom.pos.y * map.ratio / zoom.pos.s};
		}
		
		// Function to compute the center of the graph to display the windows
		var computeCenter = function() {
			var centerx = 0,
				centery = 0,
				tot = 0;
			
			nodeList.nodes.forEach(function(node){
			
				tot++;
				centerx += node.x;
				centery += node.y;
				
			});
			centerx = centerx/tot;
			centery = centery/tot;
			return [centerx, centery];
		}
		
		
		// Function to draw the graph nodes on the canvas:
		function drawNodes(nodes) {
			// erase every elements already in the map:
			while(map.nodes.length > 0) {
				var node = map.nodes.pop();
				node.remove();
				
			}
			
			// find initial position of the windows in the map:
			map.initPos = initPosition();
			changeWindow();
			
			// copy each nodes in the map dom element
			// and keep a reference in map.nodes:
			nodes.forEach(function(el) {
				var node = map.dom.insert('svg:circle', null)
									   .attr('cx', el.x*map.ratio + map.initPos.x)
									   .attr('cy', el.y*map.ratio + map.initPos.y)
									   .attr('r', config['radius']*map.ratio*1.5);

				map.nodes.push(node);

			});
			
		
		}
		
		map.init();
		return map;
	
});