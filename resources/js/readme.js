// #Hello Jonas
// Here's a few changes I made for our application.

// - A way to simply generate some doc
// - A test environment

// For both of them, you will need to install [nodejs](http://nodejs.org).

// ## Doc generation:

// So the first one is implemented through a simple and dirty utiliy called [docco](http://jashkenas.github.com/docco/). It's dead simple, just install it through:


// >	sudo npm install -g docco


// Then use it to generate documentation for any js file as follow:


// >	docco src/*.js


// You can also run it on markdown file (like on this file)

// However we will need to change slightly our comments to fit in the framework (and avoid these huge block of /////////////// ;-) )

// ## Test environment

// I spent quite a long time to make it run with <code>curljs</code>. I found a solution a bit weird but here's how it works: so you will need to install [testacular](http://karma-runner.github.com/0.8/index.html), which is gorgeous:

// >	npm install -g karma

// Then I created a little config file that tells <code>testacular</code> where file are, and I also created a <code>maintest.js</code> file. This file just calls every file in the <code>unit</code> folder, ending with <b>.test.js</b>. To load testacular, you need to run this command:

// >	testacular start testacular.conf.js

// Then open any browser at the following address:

// > http://localhost:9876/

// And then each time you change a file, the tests will be re-run and you'll get a nice report in your terminal that every tests success or not:

// > Chrome 25.0 (Mac): Executed 2 of 2 SUCCESS (0.345 secs / 0.006 secs)

// So you can continue to build your application while be sure nothing is broken. To write the tests file, it works as follow:


'use_strict';

// Be aware that I'm using requirejs here since the syntax for curljs and requirejs is the same, it shouldn't cause any problem
// I did it so, because testacular where not able to use curljs...

// We are testing here the Unix time utility:

define(['util/unixTime'],
  function(unixTime) {


  	// You can use the console log as always, to get nice report in the broswer or in the command line
  	// You can also use 'dump' but never used it yet...
  	console.log(unixTime);
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
