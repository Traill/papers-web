define(["jquery", "lib/jquery-class", "js!lib/jspdf.min.js!order", 'params'], function($, Class, jspdf, config) {

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
	    	author: 'Rudiger Urbanke',
	    	keywords: 'papers',
	    	creator: 'jsPDF'
	    });
	    
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
	    	this.addDay( day );
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
	  
	  addDay: function(d) {
	  		
	  		// Reset position to default:
	  		this.pos = 0;
	  		this.doc.addPage();
	  		
	  		this.doc.setFontSize(22);
	  		this.pos += 20	
	  		this.doc.text(20, this.pos, d.date.toDateString());
	  		this.doc.setFontSize(16);
	  	
	  },
	  
	  PageTemplate: function(){
	  	
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