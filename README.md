Trailhead
=========

Introduction
------------

Trailhead aims to make it easier to browse scientific articles or similar 
collections of textual information. The code consists of a user interface 
written in *html* and *javascript* and a backend written in *scala*. The 
backend is split into two separate parts:

-  web: Takes care of requests from the frontend
-  paper: Takes care of parsing and analyzing text documents

Setup for testing
-----------------

For Trailhead to work you must have an instance of 
[mongoDB](http://mongodb.org) running in the background and 
[sbt](http://scala-sbt.org) installed on your system. Once you've installed 
these, clone the repository:
```sh
git clone https://github.com/Traill/papers-web.git
```
Next step is to parse a set of documents and add their information to the 
database. To do so, run
```sh
sbt console
```
Once sbt has compiled the project and opened the console you type the 
following:
```scala
import paper._
val a = Analyzer.initialize("ita2013").parse.link.save
```
In the last command the following things happen:
-  **initialize("ita2013")** specifies the location of the documents relative 
   the **resources** folder
-  **parse** takes each document and parses it
-  **link** goes through the documents and calculates the similarity between each 
   document
-  **save** saves the documents to the database

Now that the database is set up, all that is left to do is to write *exit* to 
exit the console, and type sbt run from the command line. Trailhead should now 
be running on [localhost](http://localhost:8080)

The Analyzer
------------
The Analyzer takes care of tasks to do with parsing texts and measuring their 
similarities. It consists of the Analyzer case class and a companion object, 
the Analyzer object, both are in src/paper/Analyzer.scala.  Depending on the 
type of data we are using, we extend analyzer with different traits (MyEdu, 
ITA2013, ISIT2012). These traits provide the function *parseDoc(d : Document) : 
Document* which takes care of getting information out of a file and inserting 
it in the *Document* case class (src/paper/Document.scala).

The analyzer provides a few top level functions for handling the data. Starting 
with the Analyzer companion object we have the following functions:

-  **initialize(path : String) : Analyzer** -- Given a path relative to the 
   *resources* folder, this will read in the file names of the folder as id's 
ready to be parsed.
-  **fromCache(c : String) : Analyzer** -- If data has been loaded from folder 
   "my_folder" and saved to Cache, it can be loaded directly by calling 
*Analyzer.fromCache("my_folder")*.

The Analyzer class then supports the following methods:

-  **parse : Analyzer** -- For every id in the analyzer, the corresponding file 
   is parsed and loaded into the Document case class. Note that you should call 
*Analyzer.initialize* before calling parse.
-  **link : Analyzer** -- For every document in the analyzer, the similarity to 
   all other documents is calculated. Note that you should call 
*Analyzer.parse* or *Analyzer.fromCache("folder_name")* before loading since we 
need the data parsed before it can be linked.
-  **save : Analyzer** -- Save all the documents to the database. This will 
   overwrite previously saved documents from the same folder name without 
notifying you.
-  **load : Analyzer** -- Load all the documents from the database. Usually 
   used in conjunction with *Analyzer.fromCache("folder_name")* (e.g.  
*Analyzer.fromCache("ita2013").load*).
-  **graph : Graph** -- Returns a graph structure where each document is a node 
   and the links have weights corresponding to the similarity between two 
documents. Note that this function doesn't have a return type of Analyzer.
-  **spectral(k : Int) : Analyzer** -- Partitions the graph of documents into 
   *k* partitions, that are then saved with the Document.
-  **louvain(treshold : Int = 20) : Analyzer** -- As above but using a modularity 
   optimizing graph clustering technique commonly known as the [louvain 
method](http://arxiv.org/pdf/0803.0476).
-  **get(id : String) : Option[Document]** -- Returns the document corresponding 
   to the id if such a document exists.

Loading files from another folder
---------------------------------

Often while development it will desirable to load files from a folder 
containing specific documents or fewer files and consequently display them in 
the web interface. To do so, create a folder in the *resources/* directory, say 
"resources/gnuf" (the '/' is a directory separator. Substitute by the 
appropriate symbol on non-unix systems). Then to add data to the database, open 
the sbt console (by running *sbt console* in the root directory) and execute 
the following commands:
```scala
import paper._
val a = Analyzer.initialize("gnuf").parse.link.save
```
Once this is done, open the file "*src/web/Server.scala*" and modify line *15* 
so "ita2013" is changed to "gnuf". Now you can run the server by calling "sbt 
run" from the command line and seeing the result in your browser
[here](http://localhost:8080).

Credits
-------
Trailhead has been developed by Yannik Messerli, Amine Mansour, Jonny Quarta 
and Jonas Arnfred under the guidance of Rudiger Urbanke. 
