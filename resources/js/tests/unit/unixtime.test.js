'use_strict';

define(['util/unixTime'],
  function(unixTime) {

  	//console.log(unixTime);
	describe("Unix time utilities", function() {
	  var a = new Date('2013-03-20T18:50:26');

	  // From date to unixTime
	  it("should return the unix time", function() {
	    expect( unixTime.fromDate(a) ).toBe(1363809026);
	  });

	  // From unixTime to date
	  it("should return the unix time", function() {
	    expect( unixTime.toDate('1363809026') ).toEqual(a);
	  });


	});


});