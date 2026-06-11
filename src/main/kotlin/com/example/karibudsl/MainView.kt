package com.example.karibudsl.com.example.karibudsl

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.setPrimary
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

// Define the Exposed table
object Todos : LongIdTable("Todos") {
    val text = varchar("text", 255)
    val done = bool("done").default(false)
}

// Update the Todo data class to match the table structure
@Serializable
data class Todo(val id: Long, var text: String, var done: Boolean = false)

@Route("")
class MainView : KComposite() {
    private lateinit var newTaskField: TextField
    private lateinit var addTaskButton: Button
    private lateinit var todoListLayout: VerticalLayout

    init {
        ui {
            verticalLayout(classNames = "centered-content") {
                h1("Vaadin To-Do App")

                horizontalLayout {
                    width = "100%"
                    newTaskField = textField("New Task") {
                        placeholder = "Enter a new task"
                        addValueChangeListener { addTaskButton.isEnabled = it.value.isNotBlank() }
                        setWidthFull()
                    }
                    addTaskButton = button("Add Task") {
                        setPrimary()
                        addClickShortcut(Key.ENTER)
                        isEnabled = false
                        element.style.set("margin-top", "var(--lumo-space-m)")
                    }
                }

                todoListLayout = verticalLayout {
                    width = "100%"
                }
            }
        }

        addTaskButton.onClick {
            val taskText = newTaskField.value.trim()
            if (taskText.isNotBlank()) {
                addTodo(taskText)
                newTaskField.value = ""
                addTaskButton.isEnabled = false
            }
        }

        refreshTodoList()
    }

    private fun addTodo(text: String) {
        // Insert into the database
        transaction {
            Todos.insert {
                it[Todos.text] = text
                it[Todos.done] = false
            }
        }
        refreshTodoList()
    }

    private fun deleteTodo(todo: Todo) {
        // Delete from the database
        transaction {
            Todos.deleteWhere { Todos.id eq todo.id }
        }
        refreshTodoList()
    }

    private fun refreshTodoList() {
        todoListLayout.removeAll()

        // Fetch all records from the database
        val todos = transaction {
            Todos.selectAll().map {
                Todo(
                    id = it[Todos.id].value,
                    text = it[Todos.text],
                    done = it[Todos.done]
                )
            }
        }

        // Render them to the UI
        todos.forEach { todo ->
            todoListLayout.add(createTodoItemComponent(todo))
        }
    }

    private fun createTodoItemComponent(todo: Todo): HorizontalLayout {
        val taskSpan = Span(todo.text).apply {
            setWidthFull()
            element.style.set("flex-grow", "1")
        }

        val taskEditField = TextField().apply {
            value = todo.text
            setWidthFull()
            element.style.set("flex-grow", "1")
            isVisible = false
        }

        // Fix: Use standard instantiation since we aren't inside a DSL builder block here
        val editButton = Button().apply {
            icon = VaadinIcon.EDIT.create()
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        }

        val saveButton = Button("Save").apply {
            icon = VaadinIcon.CHECK.create()
            addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
            isVisible = false
        }

        editButton.onClick {
            taskSpan.isVisible = false
            taskEditField.isVisible = true
            taskEditField.focus()
            editButton.isVisible = false
            saveButton.isVisible = true
        }

        saveButton.onClick {
            val newText = taskEditField.value

            // Update the text in the database
            transaction {
                Todos.update({ Todos.id eq todo.id }) {
                    it[Todos.text] = newText
                }
            }

            taskSpan.text = newText
            taskSpan.isVisible = true
            taskEditField.isVisible = false
            saveButton.isVisible = false
            editButton.isVisible = true
        }

        val deleteButton = Button().apply {
            icon = VaadinIcon.TRASH.create()
            addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
            onClick {
                deleteTodo(todo)
            }
        }

        return HorizontalLayout().apply {
            width = "100%"
            alignItems = Alignment.CENTER

            val checkbox = Checkbox(todo.done) { event ->
                val isDone = event.value

                // Update the checked state in the database
                transaction {
                    Todos.update({ Todos.id eq todo.id }) {
                        it[Todos.done] = isDone
                    }
                }
            }

            add(checkbox, taskSpan, taskEditField, editButton, saveButton, deleteButton)
        }
    }
}