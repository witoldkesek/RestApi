package com.example.ZTI_API

import com.example.ZTI_API.Authentications.AuthenticationRequest
import com.example.ZTI_API.Authentications.AuthenticationResponse
import com.example.ZTI_API.Filters.JwtRequestFilter
import com.example.ZTI_API.Models.*
import com.example.ZTI_API.Utils.JwtUtil
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.bind.annotation.*
import java.io.File
import java.sql.*
import java.util.*

@SpringBootApplication
class ZtiApiApplication
val tmpMemesWithId= mutableListOf<MemeWithId>()
@EnableWebSecurity
internal class WebSecurityConfig : WebSecurityConfigurerAdapter() {

	@Autowired
	private val myUserDetailsService: UserDetailsService? = null

	@Autowired
	private val jwtRequestFilter: JwtRequestFilter? = null
	@Autowired
	@Throws(Exception::class)
	fun configureGlobal(auth: AuthenticationManagerBuilder) {
		auth.userDetailsService(myUserDetailsService)
	}

	@Bean
	fun passwordEncoder(): PasswordEncoder {
		return NoOpPasswordEncoder.getInstance()
	}

	@Bean
	@Throws(java.lang.Exception::class)
	override fun authenticationManager(): AuthenticationManager? {
		return super.authenticationManager()
	}

	@Throws(Exception::class)
	override fun configure(httpSecurity: HttpSecurity) {
		httpSecurity.csrf().disable()
			.authorizeRequests().antMatchers("/authenticate", "/register").permitAll().anyRequest().authenticated().and()
			.exceptionHandling().and().sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
	}
}

@RestController
class HelloWorldController {
	val login = File("login.txt").readText()
	@Autowired
	private val authenticationManager: AuthenticationManager? = null

	@Autowired
	private val jwtTokenUtil: JwtUtil? = null

	@Autowired
	private val userDetailsService: MyUserDetailsService? = null
	//users
	@RequestMapping(value = ["/authenticate"], method = [RequestMethod.POST])
	@Throws(java.lang.Exception::class)
	fun createAuthenticationToken(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<*>? {
		try {
			authenticationManager!!.authenticate(
				UsernamePasswordAuthenticationToken(
					authenticationRequest.username,
					authenticationRequest.password
				)
			)
		} catch (e: BadCredentialsException) {
			throw java.lang.Exception("Incorrect username or password", e)
		}
		val userDetails = userDetailsService!!.loadUserByUsername(authenticationRequest.username)
		val jwt = jwtTokenUtil!!.generateToken(userDetails)
		return ResponseEntity.ok<Any>(AuthenticationResponse(jwt))
	}

	@RequestMapping(value = ["/register"], method = [RequestMethod.POST])
	fun postUser(@RequestBody user: UserSample): ResponseEntity<String?>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("users")
		val idDoc= Document("username",user.username)
		val results = collection.find(idDoc).iterator()
		if(!results.hasNext()) {
			val userDoc=Document("username",user.username).append("password",user.password).append("admin",false)
			collection.insertOne(userDoc)
			mongoClient.close()
			return ResponseEntity("User registered", HttpStatus.ACCEPTED)
		}else{
			mongoClient.close()
			return ResponseEntity("Username already exists",HttpStatus.ALREADY_REPORTED)
		}
		}

	//memes
	@RequestMapping(value = ["/memes"], method = [RequestMethod.GET])
	fun memesGET(): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("memes")
		val results = collection.find().iterator()
		tmpMemesWithId.clear()
		while (results.hasNext()) {
						val result=results.next()
						tmpMemesWithId.add(MemeWithId(result.getObjectId("_id").toHexString(),result.getString("category"),result.getString("name"),result.getString("desc"),result.getInteger("likeCount"),result.getString("imgAddress")))
		}
		mongoClient.close()
		return ResponseEntity.ok<Any>(tmpMemesWithId)
	}

	@RequestMapping(value = ["/memes/{id}"], method = [RequestMethod.GET])
	fun memesGetById(@PathVariable("id") id: String): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("memes")
		val idDoc=Document("_id",ObjectId(id))
		val results = collection.find(idDoc).iterator()
		tmpMemesWithId.clear()
		while (results.hasNext()) {
			val result=results.next()
			tmpMemesWithId.add(MemeWithId(result.getObjectId("_id").toHexString(),result.getString("category"),result.getString("name"),result.getString("desc"),result.getInteger("likeCount"),result.getString("imgAddress")))
		}
		mongoClient.close()
		return ResponseEntity.ok<Any>(tmpMemesWithId)
	}

	@RequestMapping(value = ["/memes/category/{category}"], method = [RequestMethod.GET])
	fun memesGetByCategory(@PathVariable("category") category: String): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("memes")
		val idDoc=Document("category",category)
		val results = collection.find(idDoc).iterator()
		tmpMemesWithId.clear()
		while (results.hasNext()) {
			val result=results.next()
			tmpMemesWithId.add(MemeWithId(result.getObjectId("_id").toHexString(),result.getString("category"),result.getString("name"),result.getString("desc"),result.getInteger("likeCount"),result.getString("imgAddress")))
		}
		mongoClient.close()
		return ResponseEntity.ok<Any>(tmpMemesWithId)
	}

	@RequestMapping(value = ["/memes"], method = [RequestMethod.POST])
	fun memePOST(@RequestBody meme: Meme): ResponseEntity<String?>? {

		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("memes")
		val memeDoc = Document("category",meme.category).append("name",meme.name).append("desc",meme.desc).append("likeCount",meme.likeCount).append("imgAddress",meme.imgAddress)
		collection.insertOne(memeDoc)
		mongoClient.close()
		return ResponseEntity("Meme posted", HttpStatus.OK)
	}

	@RequestMapping(value = ["/memes/{id}"], method = [RequestMethod.DELETE])
	fun memesDeleteById(@PathVariable("id") id: String): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("memes")
		val idDoc=Document("_id",ObjectId(id))
		collection.deleteOne(idDoc)
		mongoClient.close()
		return ResponseEntity.ok<Any>("Pomyslnie usunieto mem.")
	}

	@RequestMapping(value = ["/comments"], method = [RequestMethod.POST])
	fun commentPOST(@RequestBody comment: Comment): ResponseEntity<String?>? {

		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("comments")
		val commentDoc = Document("postId", ObjectId(comment.postId)).append("username",comment.username).append("content",comment.content).append("likeCount",0)
		collection.insertOne(commentDoc)
		mongoClient.close()
		return ResponseEntity("Comment posted", HttpStatus.OK)
	}

	@RequestMapping(value = ["/comments/{postId}"], method = [RequestMethod.GET])
	fun commentsGetByPostId(@PathVariable("postId") postId: String): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("comments")
		val idDoc=Document("postId",ObjectId(postId))
		val results = collection.find(idDoc).iterator()
		val tmpCommentsWithId =mutableListOf<CommentWithId>()
		while (results.hasNext()) {
			val result=results.next()
			tmpCommentsWithId.add(CommentWithId(result.getObjectId("_id").toHexString(),result.getObjectId("postId").toHexString(),result.getString("username"),result.getString("content"),result.getInteger("likeCount")))
		}
		mongoClient.close()
		return ResponseEntity.ok<Any>(tmpCommentsWithId)
	}

	@RequestMapping(value = ["/comments/{id}"], method = [RequestMethod.DELETE])
	fun commentsDeleteById(@PathVariable("id") id: String): ResponseEntity<*>? {
		val uri = MongoClientURI(login)
		val mongoClient = MongoClient(uri)
		val database = mongoClient.getDatabase("zti_project")
		val collection = database.getCollection("comments")
		val idDoc=Document("_id",ObjectId(id))
		collection.deleteOne(idDoc)
		mongoClient.close()
		return ResponseEntity.ok<Any>("Pomyslnie usunieto komentarz.")
	}

}
fun main(args: Array<String>) {
	runApplication<ZtiApiApplication>(*args) {
}}

