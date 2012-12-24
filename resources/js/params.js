define(function () {

	var config = new Array();

	// // Radius for the node:
	// config['radius'] = 10;
	// config['radius_selected'] = 16;
	// 
	// // Width of a edge:
	// config['edgeSize'] = 1.5;
	// config['edgeSize_hover'] = 5;
	// 
	// 
	// // Zoom parameters:
	// config['zoomMin'] = 0.02;
	// config['zoomMax'] = 1;
	// config['nbIncrement'] = 40;
	// config['zoomInit'] = 0.5;

	// Radius for the node:
	config['radius'] = 4;
	config['radius_selected'] = 6;
	
	// Width of a edge:
	config['edgeSize'] = 0.5;
	config['edgeSize_hover'] = 2;
	
	
	// Zoom parameters:
	config['zoomMin'] = 0.1;
	config['zoomMax'] = 5;
	config['nbIncrement'] = 40;
	config['zoomInit'] = 1.5;

	
	// graph force layout size:
	config['graph_width'] = 1200;
	config['graph_height'] = 700;
	
	return config; 

});
