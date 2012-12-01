define(["lib/d3", "radio", "params", "models/zoom", "util/screen", "models/nodeList"], function(d3, radio, config, zoom, screen, nodeList) {
	
	
	// Declare interface
		var map = {};
		
		// Initializing the position:
		map.pos = zoom.pos;
	
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
			
			map.ratio = screen.height()/ screen.width();
			map.windowWidth = 100;
			
			map.center = computeCenter();
			
			map.window = map.dom.append('svg:rect')
									.attr('x', 100 - zoom.pos.x*(80/map.center[0] ) )
									.attr('y', 15 -  zoom.pos.y*(35/map.center[1] ) )
									.attr('width', map.windowWidth * map.pos.s)
									.attr('height', map.windowWidth * map.ratio * map.pos.s)
									.attr('fill', 'none')
									.attr('style', 'stroke:yellow;stroke-width:2;');
			
			// Add event listener: when we move the zoom change the rectangle
			radio("zoom:change").subscribe(changeWindow);
	
			
		}
		
		
		//////////////////////////////////////////////
		//											//
		//            Private Functions				//
		//											//
		//////////////////////////////////////////////
		
		
		var changeWindow = function() {
		
			//console.log('ok');
			
			map.window.attr('x', 100 - zoom.pos.x*(80/map.center[0]/ zoom.pos.s ) )
						.attr('y', 15 - zoom.pos.y*(35/map.center[1]/ zoom.pos.s ) )
						.attr('width', map.windowWidth / zoom.pos.s)
						.attr('height', map.windowWidth * map.ratio / zoom.pos.s)
						.attr('fill', 'none')
						.attr('style', 'stroke:yellow;stroke-width:2;');
			
		}
		
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
		
		
		map.init();
		return map;
	
});