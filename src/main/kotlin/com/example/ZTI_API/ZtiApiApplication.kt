package com.example.ZTI_API

import com.example.ZTI_API.Models.Comment
import com.example.ZTI_API.Models.Meme
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.bson.Document
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router

@SpringBootApplication
class ZtiApiApplication
val tmpMemes =mutableListOf<Meme>()
val tmpComments =mutableListOf<Comment>()
fun user(user: String, pw: String, vararg roles: String) =
		User.withDefaultPasswordEncoder().username(user).password(pw).roles(*roles).build()
val users= mutableListOf(user("user", "pw", "USER"), user("wiciu", "123", "USER", "ADMIN"))

@EnableWebSecurity
class ZtiApiConfiguration : WebSecurityConfigurerAdapter() {

	override fun configure(http: HttpSecurity?) {
		http {
			httpBasic {}
			cors{disable()}
			csrf{disable()}
			authorizeRequests {
				authorize("/memes", hasRole("USER"))
				authorize(HttpMethod.DELETE, "/memes/**", hasAuthority("ROLE_ADMIN"))
				authorize(HttpMethod.DELETE, "/comments/**", hasAuthority("ROLE_ADMIN"))
			}
		}
	}
}

fun main(args: Array<String>) {
	runApplication<ZtiApiApplication>(*args) {
		addInitializers(beans {
			bean {
				InMemoryUserDetailsManager(users);
			}
			bean {
				router {
					DELETE("/memes/{id}") { request ->
						request.principal().map {
							val id = request.pathVariable("id").toInt();
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("memes")
							val idDoc=Document("id",id)
							collection.deleteOne(idDoc)
							mongoClient.close()
						}.map {
							ServerResponse.ok().body("Pomyśnie usunięto mema.")
						}.orElseGet {
							ServerResponse.badRequest().build()
						}
					}
					GET("/memes") { request ->
						request.principal().map {
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("memes")
							val results = collection.find().iterator()
							tmpMemes.clear()
							while (results.hasNext()) {
									val result=results.next()
									tmpMemes.add(Meme(result.getInteger("id"),result.getString("name"),result.getString("desc")))
							}
							mongoClient.close()
						}.map { ServerResponse.ok().body(listOf(tmpMemes)) }.orElseGet { ServerResponse.badRequest().build() }

					}
					POST("/memes") {

						request ->
						request.principal().map {
							val meme = request.body<Meme>();
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("memes")
							val memeDoc = Document("id",meme.id).append("name",meme.name).append("desc",meme.desc)
							collection.insertOne(memeDoc)
							mongoClient.close()
						}.map { ServerResponse.ok().body("Pomyślnie dodano mema.") }.orElseGet { ServerResponse.badRequest().build() }
					}
					POST("/comments") {

						request ->
						request.principal().map {
							val comment = request.body<Comment>();
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("comments")
							val commentDoc = Document("id",comment.id).append("postId",comment.postId).append("userId",comment.userId).append("content",comment.content)
							collection.insertOne(commentDoc)
							mongoClient.close()
						}.map { ServerResponse.ok().body("Pomyślnie dodano komentarz.") }.orElseGet { ServerResponse.badRequest().build() }
					}
					GET("/comments") { request ->
						request.principal().map {
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("comments")
							val results = collection.find().iterator()
							tmpComments.clear()
							while (results.hasNext()) {
								val result=results.next()
								tmpComments.add(Comment(result.getInteger("id"),result.getInteger("postId"),result.getInteger("userId"),result.getString("content")))
							}
							mongoClient.close()
						}.map { ServerResponse.ok().body(listOf(tmpComments)) }.orElseGet { ServerResponse.badRequest().build() }
					}
					GET("/comments/{id}") { request ->
						request.principal().map {
							val id = request.pathVariable("id").toInt();
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("comments")
							val idDoc=Document("postId",id)
							val results = collection.find(idDoc).iterator()
							tmpComments.clear()
							while (results.hasNext()) {
								val result=results.next()
								tmpComments.add(Comment(result.getInteger("id"),result.getInteger("postId"),result.getInteger("userId"),result.getString("content")))
							}
							mongoClient.close()
						}.map { ServerResponse.ok().body(tmpComments) }.orElseGet { ServerResponse.badRequest().build() }
					}
					DELETE("/comments/{id}") { request ->
						request.principal().map {
							val id = request.pathVariable("id").toInt();
							val uri = MongoClientURI(
									"mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
							val mongoClient = MongoClient(uri)
							val database = mongoClient.getDatabase("zti_project")
							val collection = database.getCollection("comments")
							val idDoc=Document("id",id)
							collection.deleteOne(idDoc)
							mongoClient.close()
						}.map {
							ServerResponse.ok().body("Pomyśnie usunięto komentarz.")
						}.orElseGet {
							ServerResponse.badRequest().build()
						}
					}


				}
			}
		})
}}

