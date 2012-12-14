define(["jquery", "lib/jquery-class", "js!lib/jspdf.min.js!order", 'params', 'util/dateFormat', 'util/paragraphy'], function($, Class, jspdf, config, dateFormat, paragraphy) {

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
	  init: function(nodes){
	    this.doc = new jsPDF();
	    
	    this.doc.pos = 0;
	    
	    /* Add metadata: */
	    this.doc.setProperties({
	    	title: 'Title',
	    	subject: 'This is the subject',		
	    	author: 'Rudiger Urbanke, Jonas Arnfred',
	    	keywords: 'papers',
	    	creator: 'jsPDF'
	    });

	    //this.doc.setFont("helvetica"); // Ok let's do it in Helvetica ;-)
	    
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
	  	// Output on a new page URL:
	  	this.doc.output('dataurl');
	  },
	  
	  makeFrontPage: function() {
	  
	  
	  },
	  
	  addDay: function(d, doc) {
	  		
	  		this.addPage(doc);
	  		
	  		doc.setFontSize(22);
	  		doc.pos += 20;
	  	    doc.setFontSize(16);
	  	    doc.setFontType("bold");
	  		doc.text(20, doc.pos, d.date.format('dddd, "the" dS "of" mmmm yyyy').toUpperCase() );
			
			//reset
			doc.setFontSize(12);
			doc.setFontType("normal");
			
			doc.pos += 10;
			
	  		d.nodes.forEach(function(node) {
	  			
	  			this.writeSummary(doc, node);
	  		
	  		}, this);
	  		
	  	
	  },
	  writeSummary: function(doc, node) {
	  		
	  		var yoffset = 90;
	  		doc.pos += 8;
	  		
	  		// Write the time:
	  		var d = node.getDate();
	  		doc.pos += 6;
	  		doc.setFontSize(13);
	  		doc.setFontType("bold");
	  		doc.text(yoffset-32, doc.pos,  d.format("HH:MM TT"));
	  		//doc.pos -= 6;
	  		
	  		//Write the room
	  		doc.pos += 5;
	  		doc.setFontSize(10);
	  		doc.setFontType("italic");
	  		doc.text(yoffset-node.room.length*2.2, doc.pos,  node.room );
	  		doc.pos -= 11; // reset
	  		
	  		// Write the title:
			doc.setFontSize(13);
			doc.setFontType("bold");
			node.title.paragraphy(40).forEach(function(title) {
				doc.pos += 6;
				doc.text(yoffset, doc.pos,  title);
			});
			
			// Write the authors:
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
			
	  		doc.setFontType("normal");
	  		doc.pos += 5;
	  		doc.setFontSize(10);
	  		var summary = node.getCachedAbstract().substr(11).replace(/(\r\n|\n|\r)/gm,"").paragraphy(60);
	  		summary.forEach(function(string) {
	  			doc.pos = doc.pos + 5;
	  			doc.text(yoffset, doc.pos,  string);
	  		}, this)
	  		
	  		
	  		
	  },
	  addPage: function(doc){
	  		// Write logo on top
	  		// page on bottom
	  		// etc...
	  		
	  		// Reset position to default:
	  		doc.pos = 0;
	  		doc.addPage();
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