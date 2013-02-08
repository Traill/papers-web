define(["jquery", "lib/jquery-class", "js!lib/jspdf.js!order", 'params', 'util/dateFormat', 'util/paragraphy'], function($, Class, jspdf, config, dateFormat, paragraphy) {

	var MAX_PAGE_POS = 275; // end of page is at 295 for a font of 10
	var yoffset = 90;

	var with_summary = true;

	var Pdf = Class.extend({
	  
	  ////////////////////////////////////////////////
	  //											//
	  //                Variables					//
	  //											//
	  ////////////////////////////////////////////////
	  doc: null,
	  day:  new Array(),
	  /////////////////////////////////////////////
	  //										 //
	  //               Constructor				 //
	  //										 //
	  /////////////////////////////////////////////
	  init: function(nodes, incl_sum){

	  	// Include summary?
	  	if(incl_sum != null) with_summary = incl_sum;


	    this.doc = new jsPDF();
	    
	    this.doc.pos = 0;

	    this.doc.nb_page = 1;
	    
	    /* Add metadata: */
	    this.doc.setProperties({
	    	title: 'Title',
	    	subject: 'This is the subject',		
	    	author: 'Rudiger Urbanke, Jonas Arnfred, Yannik Messerli',
	    	keywords: 'papers',
	    	creator: 'jsPDF'
	    });

	    //this.doc.setFont("helvetica"); // Ok let's do it in Helvetica ;-)
	    
	    // create front page:
	    this.makeFrontPage(this.doc);

	    if(!with_summary) this.addPage(this.doc);

	    /* Find all different day: */
	    nodes.forEach(function(node) {
	    	var d = node.getDate();
	    	var dayfound = this.dayExist(d)
	    	if( dayfound == null ){
	    		this.day.push( {'date': d, 'nodes': [node] } ) ;
	    	}else {
	    		this.day[dayfound].nodes.push(node);
	    	}
	    }, this);
	    
	    /* Add each page for the dates*/
	    this.day.forEach(function(day)  {
	    	this.addDay( day, this.doc );
	    }, this);
	  },
	  
	 /////////////////////////////////////////////
	 //											//
	 //                FUNCTION			   		//
	 //											//
	 /////////////////////////////////////////////
	 
	 
	  send: function() {
	  	// write the last page number:
	  	this.writePageNb(this.doc);
	  	// Output on a new page URL:
	  	this.doc.output('dataurl');
	  },
	  
	  makeFrontPage: function(doc) {
	  	// Write trailhead logo:
		doc.setFontSize(16);
	  	doc.setFontType("normal");
	  	doc.text(20, 30, "TRAIL" );
	  	doc.setFontType("bold");
	  	doc.text(36, 30, "HEAD" );

	  	// Put the name of the conference in big:
	  	doc.setFontSize(24);
	  	doc.setFontType("normal");
	  	var conf_name = config['conference_name'].toUpperCase().paragraphy(20);
	  	doc.text(yoffset, 100, conf_name ); 

	  	doc.setFontSize(14);
	  	var conf_place = config['conference_place'].paragraphy(50);
	  	doc.text(yoffset, 102 + conf_name.length*7,  conf_place);

		doc.setFontSize(11);
		doc.text(yoffset, 120 + conf_name.length*7 + conf_place.length*7, ("Your program for "+config['conference_name']+".").paragraphy(50));

	  	// write the day of generation:
	  	var now = new Date();
	  	doc.setFontSize(10);
	  	doc.text(yoffset-50, 97,  "GENERATED ON THE");
	  	doc.setFontType("bold");
	  	doc.text(yoffset-58, 100, now.format('dS "of" mmmm yyyy').toUpperCase());
	  },
	  beginDay: function(d, doc){
	

	  		
	  		doc.setFontSize(22);
	  		doc.pos += 20;
	  	    doc.setFontSize(16);
	  	    doc.setFontType("bold");
	  		doc.text(20, doc.pos, d.date.format('dddd, "the" dS "of" mmmm yyyy').toUpperCase() );
			
			doc.pos += 10;
	  },
	  addDay: function(d, doc) {
	  		
	  		if(with_summary) this.addPage(doc);

	  		this.beginDay(d, doc);
			
	  		d.nodes.forEach(function(node) {
	  			
	  			if( doc.pos > MAX_PAGE_POS-20 ) {
	  				this.addPage(doc);
	  				this.beginDay(d, doc);
	  			}

		  		doc.pos += 8;
		  		
		  		// Write the time:
		  		this.writeTime(node, doc);
		  		
		  		//Write the room
		  		this.writeRoom(node, doc);
		  		
		  		// Write the title:
				this.writeTitle(node, doc)
				
				// Write the authors:
				this.writeAuthors(node, doc);
				
				if(with_summary){
			  		doc.pos += 5;
			  		
			  		var summary = node.getCachedAbstract().substr(11).replace(/(\r\n|\n|\r)/gm,"").paragraphy(60);
			  		summary.forEach(function(string) {

			  			if( doc.pos > MAX_PAGE_POS-1 ) {
			  				this.addPage(doc);
			  				this.beginDay(d, doc);
			  			}
						doc.setFontType("normal");
			  			doc.setFontSize(10);
			  			doc.pos = doc.pos + 5;
			  			doc.text(yoffset, doc.pos,  string);
			  		}, this);
			  	}
	  		
	  		}, this);
	  		
	  	
	  },
	  writeTitle: function(node, doc){

	  	doc.setFontSize(13);
		doc.setFontType("bold");
		node.title.paragraphy(40).forEach(function(title) {
			doc.pos += 6;
			doc.text(yoffset, doc.pos,  title);
		});
	  },
	  writeRoom: function(node, doc){
	  		doc.pos += 5;
	  		doc.setFontSize(10);
	  		doc.setFontType("italic");
	  		doc.text(yoffset - 40, doc.pos,  node.room );
	  		doc.pos -= 11; // reset
	  },
	  writeTime: function(node, doc){
	  		var d = node.getDate();
	  		doc.pos += 6;
	  		doc.setFontSize(13);
	  		doc.setFontType("bold");
	  		doc.text(yoffset-40, doc.pos,  d.format("HH:MM TT"));
	  		//doc.pos -= 6;
	  },
	  writeAuthors: function(node, doc){
	  		var authors = [""];
			var i = 0;
			doc.setFontType("italic");
			doc.setFontSize(10);
			node.authors.forEach(function(author) {
				if( (authors[i]+author).length < 60 ) authors[i] =  authors[i]  == ""? authors[i]+author: authors[i]+", "+author;
				else {
					i++;
					authors[i] = author;
				}
			});
			//doc.pos += 2;
			authors.forEach(function(authorline) {
				doc.pos += 5;
				doc.text(yoffset, doc.pos,  authorline);
			});
	  },
	  addPage: function(doc){
	  		
	  		// Increment the page number
	  		doc.nb_page += 1;
	  		// Reset position to default:
	  		doc.pos = 0;
	  		doc.addPage();
	  		// Write header:
	  		this.writeHeader(doc);
	  		// write the number of page:
	  		this.writePageNb(doc);

	  },
	  writePageNb: function(doc){
	  		doc.setFontSize(11);
	  		doc.setFontType("normal");
	  		doc.text(100, 290, "- "+doc.nb_page+" -");
	  },
	  writeHeader: function(doc){
	  		// add the day of generation of this pdf:
	  		var now = new Date();
	  		doc.setFontSize(8);
	  		doc.setFontType("normal");
	  		doc.text(20, 12, "GENERATED BY TRAIL");
	  		doc.setFontType("bold");
	  		doc.text(51, 12, "HEAD");
	  		doc.setFontType("normal");
	  		doc.text(60, 12, "ON THE "+now.format('dS "of" mmmm yyyy').toUpperCase())
	  },
	  dayExist: function(newday) {
	  	var e = false;
	  	var index = null;
	  	this.day.forEach(function(day, i){
	  		if( day.date.getDay() == newday.getDay()){
	  			e = true;
	  			index = i;
	  		}
	  	});
	  	return index;
	  }
	  
	  });

	return Pdf;
})
