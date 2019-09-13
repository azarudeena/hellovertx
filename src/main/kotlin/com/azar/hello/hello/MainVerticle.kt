package com.azar.hello.hello

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
//import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.parse


@Serializable
data class Person (val id: String, val name: String, val age: Int )

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val router = createRouter()

    vertx
      .createHttpServer()
      .requestHandler (router)
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause())
        }
      }

    }

  private fun createRouter() = Router.router(vertx).apply {
    get("/").handler(handlerRoot)
    get("/person").handler(handlerPerson)
    get("/person/:id").handler(handlerPersonid)
    route().handler(BodyHandler.create()).path("/person")
    post("/person").handler(handlePutPerson)
      // route().failureHandler(failurehandle).path("/error")

  }

  val handlerRoot = Handler<RoutingContext> { req ->
    req.response().end("Welcome!")
  }

  val handlerPerson = Handler<RoutingContext> { req ->
    req.response().end(Json.stringify(Person.serializer().list,personlist.sortedBy { it.id }))
  }

  val handlerPersonid = Handler<RoutingContext> { req ->
    val id = req.request().getParam("id")
    println("called person id    "+id)
    val personWithId = personlist.map{it}
      .distinct()
      .filter { it.id == null || it.id.equals(id, true)}
      .sortedBy { id }
    if (personWithId.isEmpty())
      req.fail(404)
    else
      req.response().end(Json.stringify(Person.serializer().list,personWithId))

  }

  val failurehandle = Handler<RoutingContext> { req ->

  }

  //@UseExperimental(ImplicitReflectionSerializer::class)
  val handlePutPerson = Handler<RoutingContext> { req ->
      try {
      val person = Json.parse<Person>(Person.serializer(),req.bodyAsString)

      val dupcheck = personlist.map {it}.distinct().filter { it.id == person.id}

      if (dupcheck.isNotEmpty())
        req.response().setStatusCode(409).end("Data already Present ")
      else
        personlist.add(person)
        req.response().end("201")


    }catch (e: Exception ){
      //e.printStackTrace()
      req.response().setStatusCode(400).end(e.message)
    }
  }


  val personlist = mutableListOf<Person>(
    Person("1", "Azar",29),
    Person("2", "Jerry",12),
    Person("3", "Tom",34),
    Person("4", "John",45)
  )


}
