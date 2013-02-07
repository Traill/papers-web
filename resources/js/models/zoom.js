// To be use along radio to avoid passing its reference from object ot object


define(["lib/d3", "radio", 'params', 'util/screen'], function (d3, radio, config, screen) {
	
	
	// Create a zoom behavior:
	var zoom = d3.behavior.zoom().scaleExtent([config['zoomMin'], config['zoomMax']]);
	
	// Track the position:
	zoom.pos = {};
	zoom.pos.x = 0;
	zoom.pos.y = 0;
	zoom.pos.s = 1;
	zoom.canvas = null;
	


	
	
	
	zoom.init = function(_canvas, stats){
		
		zoom.canvas = _canvas;
		
		
		
		
		
		// Publish event:
		zoom.on("zoom", function() {
			
			var e = d3.event;
			var transform = e.translate;
			var scale = e.scale;
			zoom.moveTo(scale, transform);
			
			 
		});
		
		
		//Initializing position:
		initPos(stats);
		
		// Prototype style!
		return zoom;
	}
	
	
	//////////////////////////////////////////////
	//											//
	//            Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Move the canvas to the new position:
	zoom.moveTo = function(scale, translation){
		
		setValueManually(scale, translation);
		goTo();

	} 
	
	// Manually move the canvas to the new position:
	zoom.transitionTo = function(scale, translation){
		
		var transTo = {};
		transTo.x = translation[0];
		transTo.y = translation[1];
		transTo.s = scale;
		
		zoom.canvas.transition().tween('transform', function() {
			
			var a = zoom.pos;
			var b = transTo;
			
			return function(t) {
				    
				// Interpolation
				var posx = a.x + (b.x-a.x) * t;
				var posy = a.y + (b.y-a.y) * t;
				var s = a.s + (b.s-a.s) * t;
				
				
				setValueManually(s, [posx, posy]);
				
				// Change canvas:
				goTo();
				return null; //"translate(" + posx + ", "+posy+") scale(" + s + ")";
			}
		});
		
		//transitionTo();
	
	}
	
	//////////////////////////////////////////////
	//											//
	//            private Functions				//
	//											//
	//////////////////////////////////////////////
	var setValueManually = function(scale, transform) {
		
		zoom.pos.x = transform[0];
		zoom.pos.y = transform[1];
		zoom.pos.s = clipScale(scale);
		
       // Update the zoom with new position:
       zoom.translate(transform);
       zoom.scale(scale);
	}
	
	var clipScale = function(s){
		var s = s > config['zoomMax'] ? config['zoomMax'] : s;
		s = s < config['zoomMin'] ? config['zoomMin'] : s;
		
		
		return s;
	}
	
	var goTo = function() {
		
		radio("zoom:change").broadcast(zoom);
		var transMatrix = ComputeMatrix();
		zoom.canvas.attr("transform", "matrix(" + transMatrix.join(' ') + ")");
		
	}

	function ComputeMatrix(){

		var transMatrix = [1,0,0,1,0,0];
		for (var i=0; i<transMatrix.length; i++)
		  {
		    transMatrix[i] *= zoom.pos.s;
		  }

		  // transMatrix[4] += (1-zoom.pos.s)*1000/2;
		  // transMatrix[5] += (1-zoom.pos.s)*500/2;

		  transMatrix[4] += zoom.pos.x;
	  	  transMatrix[5] += zoom.pos.y;

	  	  return transMatrix;
	}

	var initPos = function( stats ){
		// Size of the window:
		var w = screen.width(),
			h = screen.height();

		// Setup the variable:
		var max_ratio_w =  (w - 20 ) / Math.abs(stats.max[0] - stats.min[0]) ,
			max_ratio_h =  (h - 60) / Math.abs(stats.max[1] - stats.min[1]) ;


		// Setup the scale:
		zoom.pos.s = Math.min(max_ratio_w, max_ratio_h);


		// Setup the translation x and y to have zoom in the middle:
		zoom.pos.x = w/2 - stats.center[0] * zoom.pos.s + 10; 

		zoom.pos.y = h/2 - stats.center[1] * zoom.pos.s + 30;

		// Then update the view
		zoom.translate([zoom.pos.x, zoom.pos.y]);
		zoom.scale(zoom.pos.s);
		
		goTo();
	}
	

	
	return zoom;
});
