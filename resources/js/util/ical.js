define(["jquery", "lib/jquery-class", 'params', 'util/dateFormat'], function($, Class, config, dateFormat) {

	if (typeof btoa === 'undefined') {
		var btoa = function(data) {
			// DO NOT ADD UTF8 ENCODING CODE HERE!!!!

			// UTF8 encoding encodes bytes over char code 128
			// and, essentially, turns an 8-bit binary streams
			// (that base64 can deal with) into 7-bit binary streams. 
			// (by default server does not know that and does not recode the data back to 8bit)
			// You destroy your data.

			// binary streams like jpeg image data etc, while stored in JavaScript strings,
			// (which are 16bit arrays) are in 8bit format already.
			// You do NOT need to char-encode that before base64 encoding.

			// if you, by act of fate
			// have string which has individual characters with code
			// above 255 (pure unicode chars), encode that BEFORE you base64 here.
			// you can use absolutely any approch there, as long as in the end,
			// base64 gets an 8bit (char codes 0 - 255) stream.
			// when you get it on the server after un-base64, you must 
			// UNencode it too, to get back to 16, 32bit or whatever original bin stream.

			// Note, Yes, JavaScript strings are, in most cases UCS-2 - 
			// 16-bit character arrays. This does not mean, however,
			// that you always have to UTF8 it before base64.
			// it means that if you have actual characters anywhere in
			// that string that have char code above 255, you need to
			// recode *entire* string from 16-bit (or 32bit) to 8-bit array.
			// You can do binary split to UTF16 (BE or LE)
			// you can do utf8, you can split the thing by hand and prepend BOM to it,
			// but whatever you do, make sure you mirror the opposite on
			// the server. If server does not expect to post-process un-base64
			// 8-bit binary stream, think very very hard about messing around with encoding.

			// so, long story short:
			// DO NOT ADD UTF8 ENCODING CODE HERE!!!!
			
			/**
			====================================================================
			base64 encoder
			MIT, GPL
		
			version: 1109.2015
			discuss at: http://phpjs.org/functions/base64_encode
			+   original by: Tyler Akins (http://rumkin.com)
			+   improved by: Bayron Guevara
			+   improved by: Thunder.m
			+   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
			+   bugfixed by: Pellentesque Malesuada
			+   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
			+   improved by: Rafal Kukawski (http://kukawski.pl)
			+   			 Daniel Dotsenko, Willow Systems Corp, willow-systems.com
			====================================================================
			*/
			
			var b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
			, b64a = b64.split('')
			, o1, o2, o3, h1, h2, h3, h4, bits, i = 0,
			ac = 0,
			enc = "",
			tmp_arr = [];
		 
			do { // pack three octets into four hexets
				o1 = data.charCodeAt(i++);
				o2 = data.charCodeAt(i++);
				o3 = data.charCodeAt(i++);
		 
				bits = o1 << 16 | o2 << 8 | o3;

				h1 = bits >> 18 & 0x3f;
				h2 = bits >> 12 & 0x3f;
				h3 = bits >> 6 & 0x3f;
				h4 = bits & 0x3f;
		 
				// use hexets to index into b64, and append result to encoded string
				tmp_arr[ac++] = b64a[h1] + b64a[h2] + b64a[h3] + b64a[h4];
			} while (i < data.length);

			enc = tmp_arr.join('');
			var r = data.length % 3;
			return (r ? enc.slice(0, r - 3) : enc) + '==='.slice(r || 3);

			// end of base64 encoder MIT, GPL
		}
	}
	
	////////////////////////////////////////////////
	//											  //
	//           	     CONSTANTS				  //
	//											  //
	////////////////////////////////////////////////

	var BEGIN = "BEGIN:VCALENDAR\nVERSION:2.0\nMETHOD:PUBLISH";
	var END = "\nEND:VCALENDAR";
	var preDateTZID = ";TZID=" + config['timezone'] + ":";

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
	docLine.push('TZID:' + config['timezone']);
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

			var form = $('<form action="ical/calendar.ics" method="post"></form>').appendTo('body');
			$('<input type="hidden" name="content" value="'+btoa(BEGIN+docContent+END)+'" />').appendTo(form);
			form.submit();
	 		//document.location.href = 'ical/' + btoa(BEGIN+docContent+END);
	 		
		}

	}); 

	return iCal;

});
