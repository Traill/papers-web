/* 
 *	This module is in charge of displaying the clustering
 */

define(["radio", "models/cluster", 'js!lib/jquery/jquery-ui-1.9.1.custom.min.js!order'], function(radio, clusterModel) {


	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////

	var cluster = {}



	//////////////////////////////////////////////
	//											//
	//               Graph Init 				//
	//											//
	//////////////////////////////////////////////

	cluster.init = function () {

		// Activate events
		cluster.events();

		// Set slider
		$( "#slider" ).slider({
			range: "min",
			value: 0,
			min: 0,
			max: 5,
			step: 1,
			slide: function( event, ui ) {
				radio("slider:move").broadcast(ui.value)
			},
			change: function(event, ui) { 
				clusterModel.setSpread(ui.value);
			}
		});
	}



	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////

	cluster.events = function () {

		/**
		 * Broadcast
		 */


		/**
		 * Subscribe
		 */
		$("input[name='cluster']").click(function() {
			clusterModel.makeClusters("louvain")
		})
		
		radio("slider:move").subscribe(setSpread);

	}



	//////////////////////////////////////////////
	//											//
	//           Private Functions				//
	//											//
	//////////////////////////////////////////////
	

	var setSpread = function(n) {
		$("#slider").slider({ value: n });
		if (n != 0) {
			$("#spread").html(n)
		}
		else $("#spread").html("None")
	}


	//////////////////////////////////////////////
	//											//
	//            Return Interface				//
	//											//
	//////////////////////////////////////////////

	cluster.init();
	return cluster;
})
