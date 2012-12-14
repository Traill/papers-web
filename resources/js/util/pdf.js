define(["jquery", "lib/jquery-class", "js!lib/jspdf.min.js!order", 'params', 'util/dateFormat', 'util/paragraphy'], function($, Class, jspdf, config, dateFormat, paragraphy) {

	var Pdf = Class.extend({
	  
	  ////////////////////////////////////////////////
	  //											//
	  //                Variables					//
	  //											//
	  ////////////////////////////////////////////////
	  doc: null,
	  pos: 0,
	  day:  new Array(),
	  /////////////////////////////////////////////
	  //										 //
	  //               Constructor				 //
	  //										 //
	  /////////////////////////////////////////////
	  init: function(nodes){
	    this.doc = new jsPDF();
	    
	    
	    /* Add metadata: */
	    this.doc.setProperties({
	    	title: 'Title',
	    	subject: 'This is the subject',		
	    	author: 'Rudiger Urbanke, Jonas Arnfred',
	    	keywords: 'papers',
	    	creator: 'jsPDF'
	    });
	    console.log(this.doc.getFontList());
	    //this.doc.setFont("helvetica"); // Ok let's do it in Helvetica ;-)
	    
	    /* Find all different day: */
	    nodes.forEach(function(node) {
	    	
	    	var d = node.getDate();
	    	
	    	if( ! this.dayExist(d) ){
	    		this.day.push( {'date': d, 'nodes': [node] } ) ;
	    	}else {
	    		this.day[d.getDay()].nodes.push(node);
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
	  		this.pos += 20;
	  		doc.text(20, this.pos, d.date.format('dddd, "the" dS "of" mmmm yyyy'));
	  		doc.setFontSize(16);
	  	
	  },
	  writeSummary: function(doc, node) {
	  		
	  		
	  },
	  addPage: function(doc){
	  		// Write logo on top
	  		// page on bottom
	  		// etc...
	  		
	  		// Reset position to default:
	  		this.pos = 0;
	  		doc.addPage();
	  },
	  
	  dayExist: function(newday) {
	  	var e = false;
	  	this.day.forEach(function(day){
	  		e = e || (day.date.getDay() == newday.getDay());
	  	});
	  	return e;
	  }
	  
	  });

	return Pdf;
})