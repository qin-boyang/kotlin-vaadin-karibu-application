package com.example.karibudsl.com.example.karibudsl

import io.ktor.server.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class TodoRequest(val text: String, val done: Boolean = false)

fun Route.mainApiRoutes() {
    route("/api/todos") {
        get {
            val todos = transaction {
                Todos.selectAll().map { it.toTodo() }
            }

            call.respond(todos)
        }

        get("/{id}") {
            val id = call.todoIdOrNull() ?: return@get
            val todo = transaction {
                Todos.selectAll()
                    .where { Todos.id eq id }
                    .singleOrNull()
                    ?.toTodo()
            } ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(todo)
        }

        post {
            val request = call.receive<TodoRequest>().normalizedOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Todo text must not be blank")

            val todo = transaction {
                val id = Todos.insertAndGetId {
                    it[text] = request.text
                    it[done] = request.done
                }

                Todo(id.value, request.text, request.done)
            }

            call.respond(HttpStatusCode.Created, todo)
        }

        put("/{id}") {
            val id = call.todoIdOrNull() ?: return@put
            val request = call.receive<TodoRequest>().normalizedOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Todo text must not be blank")

            val updated = transaction {
                Todos.update({ Todos.id eq id }) {
                    it[text] = request.text
                    it[done] = request.done
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(Todo(id, request.text, request.done))
            }
        }

        delete("/{id}") {
            val id = call.todoIdOrNull() ?: return@delete
            val deleted = transaction {
                Todos.deleteWhere { Todos.id eq id }
            }

            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ResultRow.toTodo(): Todo = Todo(
    id = this[Todos.id].value,
    text = this[Todos.text],
    done = this[Todos.done],
)

private fun TodoRequest.normalizedOrNull(): TodoRequest? {
    val normalizedText = text.trim()
    return if (normalizedText.isBlank()) null else copy(text = normalizedText)
}

private suspend fun ApplicationCall.todoIdOrNull(): Long? {
    val id = parameters["id"]?.toLongOrNull()
    if (id == null) {
        respond(HttpStatusCode.BadRequest, "Todo id must be a number")
    }
    return id
}
