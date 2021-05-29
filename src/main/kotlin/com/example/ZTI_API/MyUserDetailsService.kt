package com.example.ZTI_API

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.bson.Document
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*


@Service
class MyUserDetailsService : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(s: String): UserDetails {
        val uri = MongoClientURI("mongodb://admin:123@zadb-shard-00-00.l9t9j.mongodb.net:27017,zadb-shard-00-01.l9t9j.mongodb.net:27017,zadb-shard-00-02.l9t9j.mongodb.net:27017/zti_project?ssl=true&replicaSet=atlas-eq1sic-shard-0&authSource=admin&retryWrites=true&w=majority")
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