package paper

import net.liftweb.json._
import java.io._
import scala.io.Source
import scala.reflect.runtime.universe._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import scala.util.Properties
import com.mongodb.casbah.commons.conversions.scala._


object Cache {

  RegisterJodaTimeConversionHelpers()

  println(Properties.envOrNone("MONGOHQ_URL"));
  //val MongoSetting(mongoDB) = Properties.envOrNone("MONGOHQ_URL")
  val MongoSetting(mongoDB) = Some(Properties.envOrElse("MONGOHQ_URL", "mongodb://heroku:9f4db15648e7d65475777fb389aed51a@alex.mongohq.com:10092/app11027994"))

  // Constants
  val basedir = "cache" + File.separator
  val suffix = "cache"

  def putItem[A <: AnyRef : Manifest](collection : String, id : String, data : A) : Unit = {

    // Serialize data
    val dbo = grater[A].asDBObject(data) + ("_id" -> id)
   
    // Get the right collection
    val mongoColl = mongoDB(collection)

    // Then save
    mongoColl.save(dbo)
  }

  def getItem[A <: AnyRef : Manifest](collection : String, id : String) : Option[A] = {
    // Get the right collection
    val mongoColl = mongoDB(collection)

    // Then load
    mongoColl.findOne(Map("_id" -> id)) match {
      case Some(data) => Some(grater[A].asObject(data))
      case None       => None
    }
  }

  def getList[A <: AnyRef : Manifest](collection : String, ids : List[String]) : List[A] = {
    // Get the right collection
    val mongoColl = mongoDB(collection)

    // Load all ids
    val q : DBObject = ("_id" $in ids.toList)
    (for (data <- mongoColl.find(q)) yield grater[A].asObject(data)).toList
  }

  def getQuery[A <: AnyRef : Manifest](collection : String, query : Map[String, String], limit : Option[Int] = None) : List[A] = {

    // Get the right collection
    val mongoColl = mongoDB(collection)

    limit match {
      case Some(n) => (for (data <- mongoColl.find(query).limit(n)) yield grater[A].asObject(data)).toList
      case None    => (for (data <- mongoColl.find(query)) yield grater[A].asObject(data)).toList
    }
  }
}

object MongoSetting {

  val localDbName = "trailhead"

  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val db = MongoConnection(host, port.toInt)(dbName)
        db.authenticate(u,p)
        Some(db)
      case None =>
        Some(MongoConnection("localhost", 27017)(localDbName))
    }
  }
}
