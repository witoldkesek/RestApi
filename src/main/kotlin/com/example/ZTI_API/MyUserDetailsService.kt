package com.example.ZTI_API

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.bson.Document
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.io.File
import java.util.*


@Service
class MyUserDetailsService : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(s: String): UserDetails {
        val login = File("login.txt").readText()
        val uri = MongoClientURI(login)
        val mongoClient = MongoClient(uri)
        val database = mongoClient.getDatabase("zti_project")
        val collection = database.getCollection("users")
        val idDoc= Document("username", s)
        val results = collection.find(idDoc).iterator()
        val u=results.next()
        return User(
            u.getString("username"), u.getString("password"),
            ArrayList()
        )
    }
}