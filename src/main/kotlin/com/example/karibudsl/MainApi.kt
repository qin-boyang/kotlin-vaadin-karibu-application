package com.example.karibudsl.com.example.karibudsl

import com.example.karibudsl.Todo
import com.example.karibudsl.Todos
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.mainApiRoutes() {
    get("/api/todos") {
//        call.respondText("List of ToDos")

        val todos = transaction {
            Todos.selectAll().map {
                Todo(
                    id = it[Todos.id].value,
                    text = it[Todos.text],
                    done = it[Todos.done]
                )
            }
        }

        // Ktor handles the headers and JSON conversion automatically
        call.respond(todos)
    }
}