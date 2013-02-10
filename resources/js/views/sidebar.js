define(["jquery", "radio", "util/truncate", "util/pdf", "models/nodeList", "util/ical", 'params', "util/array", 'js!lib/jquery/jquery-ui-1.9.1.custom.min.js!order',"js!lib/jquery/multiselect!order", 'js!lib/jquery/jquery.transit.min.js!order', "util/unixTime", "params"], 
function($, radio, truncate, Pdf, nodeList, iCal, param, arrrr, tabbbb, tabbb, tabb, unixTime, params) {

	//////////////////////////////////////////////
	//											//
	//               Interface					//
	//											//
	//////////////////////////////////////////////
	var sidebar = {};

	var isOpen = true;
	//////////////////////////////////////////////
	//											//
	//                Events					//
	//											//
	//////////////////////////////////////////////
	

	sidebar.events = function () {

		/**
		 * Broadcast
		 */



		/**
		 * Subscribe
		 */

		// Current node
		radio("node:select").subscribe(setFocus);

		// Unselect it when click somewhere else
		radio("node:unselect").subscribe(unsetFocus);

		// Select node
		radio("node:schedule").subscribe(schedule);

		// unscheduled node
		radio("node:unschedule").subscribe(unschedule);

		// On unscheduled all nodes, we should close "are you sure?"
		radio("sidebar:removeAll").subscribe(removeAll);
		
		// on click in the sidebar, select the node
		radio("sidebar:click").subscribe(function (id, e) { 
			radio("node:select").broadcast(id, e);
		});
		
		// on hover in the sidebar, broadcast node:current
		radio("sidebar:hover").subscribe(function (id, e) { 
			radio("node:mouseover").broadcast(id, e);
		});
		
		// on mouseout in the sidebar, broadcast node:current
		radio("sidebar:hoverout").subscribe(function (id, e) { 
			radio("node:mouseout").broadcast(id, e);
		});

		// when we click remove in the sidebar, unscheduled the item
		radio("sidebar:remove").subscribe(function (id, e) { 
			radio("node:unschedule").broadcast(id, e);
		});
		
		// Hide the sidebar
		radio("sidebar:hide").subscribe(hide);
		
		// Show the sidebar
		radio("sidebar:show").subscribe(show);
		
		
		// When the windows is resized, change UI
		radio("window:resize").subscribe(resize);
		
		// When the dom is ready, update the UI:
		radio("domready").subscribe(resize);

		// Current node
		radio("node:select").subscribe(putInfo);

		// Unselect it when click somewhere else
		radio("node:unselect").subscribe(removeInfo);

		// Add new event to open a particular tab:
		radio("sidebar:tab").subscribe(openTab);


	}


	//////////////////////////////////////////////
	//											//
	//                  Init					//
	//											//
	//////////////////////////////////////////////

	
	/**
	 * Initializes the sidebar plus events
	 * TODO: Break this function up to smaller bits and pull the events 
	 * out
	 */
	sidebar.init = function() {

		// Make remove button work
		$(".removeall").click( function() { removeVerify(); });

		// Make generate schedule button work
		$(".downpdf").click("click", function () { abstractVerify(0); });
		
		// Make generate schedule button work
		$(".downicn").click("click", function () { abstractVerify(1); });

		// Call events
		sidebar.events();
		
		// Set the UI height:
		resize();
		
		// Init the tabs:
		$('#tabs').tabs();
		
		// Make sidebar open and close working:
		$("#closeSidebar").click( function() { toggleSidebar(); });
	}



	//////////////////////////////////////////////
	//											//
	//             Private Functions			//
	//											//
	//////////////////////////////////////////////
	

	/**
	 * Updates the list with the current element
	 */
	var setFocus = function(node) {

		// Removes current from last list item
		$(".listItem.current").removeClass("current");

		// Adds current to the current list item
		$("#" + node.id).addClass("current");
	}


	/**
	 * Updates the list when unselecting a node
	 */
	var unsetFocus = function (node) {

		// Removes current from last list item
		$(".listItem.current").removeClass("current");
	}


	// Renders the schedule with dates and all
	render = function() {
		var scheduled = nodeList.scheduled;
		var sorted = scheduled.sort(function(s1,s2) { return s1.time - s2.time });
		var days = sorted.map(function(s) { return unixTime.toDate(s.time/1000); });

		// Now loop through sorted and build the html items
		var listItems = sorted.map(function(s) { return buildListItem(s) });

		var div = $("<ul>");
		listItems.forEach(function(item, i) {
			// If we start a new day, then
			if (i == 0 || days[i].format("yymmdd") != days[i-1].format("yymmdd")) {
				// Insert date
				var d = days[i].format("fullDate");
				div.append("<span class=\"listDate\">" + d + "</span>");

				// Create new list
				div.append($("<ul>").addClass("sidebarDay").append(item));
			}
			// If not, just insert item to last list
			div.find("ul").last().append(item);
		});

		$("#schedule").html(div);


	}


	var buildListItem = function(node) {
		// Clone listItemTemplate
		var item = $("#listItemTemplate").clone();
		item.css("display","block");
		item.attr("id", node.id);

		// Add information
		item.find(".listItemText").html(truncate(node.title, 62));
		item.find(".listItemPdfLink").attr("href",node.pdf);
		item.find("input").attr("value", node.id);

		// Add mouseover event
		item.mouseover(function (e) { 
			radio("sidebar:hover").broadcast(node,e); 
		});
		
		// Add click event
		item.click(function (e) { 
			radio("sidebar:click").broadcast(node,e); 
		});
		
		// Add mouseout event
		item.mouseout(function (e) { 
			radio("sidebar:hoverout").broadcast(node,e); 
		});

		// Add remove event
		item.find(".listItemRemove").click(function (e) { 
			radio("sidebar:remove").broadcast(node,e); 
		});

		return item;
	}




	/**
	 * What happens when a node is scheduled
	 */
	var schedule = function(node) {

		// Render the new list
		render();

		// Set it as the current element
		setFocus(node);

		// Move sidebar when close to show it has been added
		if(!isOpen){
			moveSidebar(-280, 100, 0, function(){ moveSidebar(-310, 50, 0) });
		}

		radio('sidebar:tab').broadcast(0);
	}


	/**
	 * What happens when a node is scheduled
	 */
	var unschedule = function(node) {

		// Render the new list
		render();

		if(!isOpen){
			moveSidebar(-280, 100, 0, function(){ moveSidebar(-310, 50, 0) });
		}
	}

	/**
	 * Prompts the user if the want to remove all nodes from list
	 */
	var removeVerify = function() {

		// Hide downloadType if open
		if(confirm("Are you sure? ")) radio("sidebar:removeAll").broadcast();
	}


	/**
	 * Promps the user if they want to include an abstract with the pdf
	 */
	var abstractVerify = function(type) {
		
		var $dialog = $('<div></div>')
		.html('Do you want to include abstract of the papers?')
		.dialog({
			      resizable: false,
			      modal: true,
			      buttons: {
			        "Yes with abstract": function() {
			          $( this ).dialog( "close" );
			          if(type == 0){
							withAbstract(1);
						}else{
							downloadIcal(1);
						}
			        },
			        "No, without": function() {
			          $( this ).dialog( "close" );
			          if(type == 0){
							withAbstract(0);
						}else{
							downloadIcal(0);
						}
			        }
			      }
			    });

	
		$dialog.dialog('open');


		// var abst = (confirm("Do you want to include abstract of the papers? ")) ? 1: 0 ;
		
	}


	/**
	 * What happens when we click on getSmall
	 */
	var withAbstract = function(abst) {

		// Update hidden field
		$("input[name=abstract]").attr("value",abst);

		// Get all the nodes in the schedule:
		var scheduled = nodeList.scheduled;

		// generate the pdf
		var t = new Pdf(scheduled, abst == 1 );
		t.send();
	}


	/**
	 * Create a document on ical format:
	 */

	var downloadIcal = function(abst){

		// create the ical document:
		var icalDoc = new iCal();
		

		// Add each in node in the ical document:
		nodeList.scheduled.forEach(function(node) {

				
			// create the description of the event:
			var description = "";
			
			// create author string:
			node.authors.forEach(function(author, i) { description += (i > 0) ? ", "+author: author; });

			// if we want abastract in the ical event:
			if(abst == 1) description += "\n"+node.getCachedAbstract().substr(11).replace(/(\r\n|\n|\r)/gm,"");

			// create ending date:
			var start = node.getDate();
			var end = new Date( start.getTime()+param['talk_duration']*60*1000 );
			icalDoc.addEvent(start, end, node.title, node.room, description );
					
		});

		icalDoc.send();

	}

	/**
	 * Removes all nodes from list
	 */
	var removeAll = function() {

		radio("node:unscheduleall").broadcast();
	}

	
	/**
	 * Open or close the sidebar
	 */
	var toggleSidebar = function() {
		if(isOpen){
			radio("sidebar:hide").broadcast();
			isOpen = false;
		}else{
			radio("sidebar:show").broadcast();
			isOpen = true;
		}
	}
	
	/**
	 * Move the side bar
	 */
	
	var moveSidebar = function(dist, duration, rot, callback) {
		if(dist == null) dist = -310;
		if(duration == null) duration = 300;
		if(rot == null) rot = 0;

		$('#tabs').animate({"left": ""+dist+"px"}, duration, callback);
		$('#closeSidebar').animate({"left": ""+(dist+310)+"px"}, duration);
		$('#closeSidebar div').transition({ rotate: ""+rot+'deg' });
	}

	/**
	 * Hide the side bar
	 */

	 var hide = function(){
	 	moveSidebar();
	 }
	
	/**
	 * Show the side bar
	 */
	 
	 var show = function() {

		moveSidebar(0, 300, 180);
	 }
	
	
	/**
	 * Update the UI of the tabs accordingly of the windows height:
	 */
	var resize = function(w, h){
	
		$('#tabs').css('height', h);
		$('#tabs .scrollable').css('height', h-248);
		
		// Later will implement a scroll only on the summary:
		//$("#tab1-summary").css('height', h-440);
		// A bit complicate, 'cause need to compute the size the header take (title, authors...)
		
	}

	/**
	 * Show information of the current node in the info tab:
	 */
	var putInfo = function(node){
		

		var template = function(id, title, time, abstract, room, authors){
			
			var goog = 'http://www.google.com/search?ie=UTF-8&oe=UTF-8&sourceid=navclient&btnI=1&q=';
			var domEl = $('#template_info').clone();
			domEl.css("display","block");
			domEl.attr("id", id);

			var authorString = "";
			authors.forEach( function(author, i) {

				var authURL = goog + author.replace(" ","+");
				if(i == 0) authorString += 'By <a href="' + authURL + '" target="_blank">';
				//else if (authors.length == 2) authorString += ' and <ahref="' + authURL + '" target="_blank">';
				else if (i == authors.length - 1) authorString += ' and <a href="' + authURL + '" target="_blank">';
				else authorString += ', <a href="' + authURL + '" target="_blank">';

				authorString += author;
				authorString += '</a>';
			});

			// Get conference
			var baseurl = params['conference_data'] + params['conference_abbr'] + '/';

			// Add thumb:
			domEl.find('.thumb').append($('<img src="' + baseurl + id +'.jpg" width="50" />').error(function() {
  				domEl.find('.thumb').remove();
  				domEl.find('.authors').css('width', '100%');
			}));
			
			
			

			// check if pdf exist:
			// $.ajax({
			// 	url:baseurl + id + '.pdf',
			// 	type:'HEAD', 
			// 	error: function(){
			// 		// Remove link
			// 		domEl.find('.pdf_dwn').remove();
			// 	},
			// 	success: function(){
			// 		// Add pdf url:
			// 		domEl.find('.pdf_dwn a').attr('href', url);
			// 	}
			// });
			domEl.find('.pdf_dwn').remove();



			domEl.find('.title').html(title);
			domEl.find('.time b').html(  time.format("HH:MM  mmm d, yyyy") );

			var rooml = room.substr(0, 15);
			if(room.length > 15 ) rooml += "...";

			domEl.find('.location b').html( rooml  );
			domEl.find('.authors').html(authorString);
			domEl.find('.abstract').html(abstract);

			return domEl;


		}


		// Default abstract (loader in case we are loading from the web 
		// server)
		var abstract = "<img class=\"loading\" src=\"/img/ajax-loader_dark.gif\" style=\"margin:3px 0\"/><span class=\"loading-text\">Loading Abstract...</span>";

		$('#tabs-1').find('.content_tab').html( template( node.id, node.title, node.getDate(), abstract, node.room, node.authors ));

		// If the abstract isn't cached, fetch it
		// It's in the end in case we get it really fast
		node.getAbstract(function(data) {
			$(".abstract").html(data); 
		});
	 }

	 /**
	 * Show information of the current node in the info tab:
	 */
	 var removeInfo = function(node){
	 		$('#tabs-1').html('<div class="form-w" style="margin-top:40px; text-align:center;">no node selected</div>');
	 }

	 var openTab = function(tabIndex){
		$( "#tabs" ).tabs( "option", "active",  tabIndex);
	 }

	// Export the controller
	sidebar.init();
	return sidebar;
})
