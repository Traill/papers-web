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
	config['radius'] = 6;
	config['radius_selected'] = 15;
	config['radius_scheduled'] = 10;
	
	// Width of a edge:
	config['edgeSize'] = 1.5;
	config['edgeSize_hover'] = 5;
	
	
	// Zoom parameters:
	config['zoomMin'] = 0.1;
	config['zoomMax'] = 5;
	config['nbIncrement'] = 40;
	config['zoomInit'] = 1;


	// graph force layout size:
	config['graph_width'] = 2400;
	config['graph_height'] = 1400;
	

	config['talk_duration'] = 20;



	//config['conference_data'] = //"http://yannik-messerli.com/isit2013/"; //"http://ipgwww.epfl.ch/~arnfred/papers/";
	config['conference_data'] = "http://yannik-messerli.com/ita2014/paper_";
	config['conference_abbr'] = "ita2014";
	config['cluster_spreads'] = [26, 20, 15, 11, 8, 6, 5, 4, 3, 2];
    //config['cluster_spreads'] = [71, 60, 50, 41, 33, 26, 20, 15, 11, 8, 6, 5]
	config['conference_name'] = "IEEE International Symposium on Information Theory";
	config['conference_place'] = "Istanbul, Turkey";
	config['timezone'] = "Europe/Istanbul";


	return config; 

});
