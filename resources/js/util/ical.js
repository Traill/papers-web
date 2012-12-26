define(["jquery", "lib/jquery-class", 'params', 'util/dateFormat'], function($, Class, config, dateFormat) {

	////////////////////////////////////////////////
	//											  //
	//           	     CONSTANTS				  //
	//											  //
	////////////////////////////////////////////////

	var BEGIN = "BEGIN:VCALENDAR\nVERSION:2.0\nMETHOD:PUBLISH";
	var END = "\nEND:VCALENDAR";
	var preDateTZID = ";TZID=Europe/Zurich:";

	// TODO genreated UID:
	var UID = function(){
		return "hafas1866543331";
	}

	////////////////////////////////////////////////
	//											  //
	//                Variables					  //
	//											  //
	////////////////////////////////////////////////

	// content of the document, line by line:
	var docLine = new Array();

	// Add the time zone definition of our timestamp:
	docLine.push('BEGIN:VTIMEZONE');
	docLine.push('TZID:Europe/Zurich');
	docLine.push('BEGIN:STANDARD');
	docLine.push('TZOFFSETFROM:+0200');
	docLine.push('TZOFFSETTO:+0100');
	docLine.push('TZNAME:CET');
	docLine.push('DTSTART:19701025T030000');
	docLine.push('RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10');
	docLine.push('END:STANDARD');
	docLine.push('END:VTIMEZONE');

	var iCal = Class.extend({
		init: function(){
			// nothing here

		},
		addEvent: function(dstart, dend, summary, location, description ){
			// Add event definition in the content of the document:
			docLine.push('BEGIN:VEVENT');
			//docLine.push('UID:'+UID() );
			docLine.push('DTSTART'+preDateTZID+dstart.format('yyyymmdd"T"HHMMss'));
			docLine.push('DTEND'+preDateTZID+dend.format('yyyymmdd"T"HHMMss'));
			docLine.push('SUMMARY:'+summary);
			docLine.push('LOCATION:'+location);
			docLine.push('DESCRIPTION:'+description);
			docLine.push('END:VEVENT');
		},
		send: function(){
			
			// build string:
			var docContent = "";
			docLine.forEach(function(el){ docContent += "\n"+el });

	 		document.location.href = 'data:application/ical;base64,' + btoa(BEGIN+docContent+END);
	 		
		}

	}); 

	return iCal;

});