package com.example.karibudsl

import com.example.karibudsl.com.example.karibudsl.mainApiRoutes
import com.github.mvysny.vaadinboot.VaadinBoot
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.PWA
import com.vaadin.flow.theme.lumo.Lumo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*

@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
class AppShell : AppShellConfigurator

/**
 * Run this function to launch your app in Embedded Jetty.
 */
fun main() {
    DatabaseConfig.init() // Initialize the database

    // 1. Start Ktor API asynchronously on port 8081
    embeddedServer(Netty, port = 8081) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            mainApiRoutes()
        }
    }.start(wait = false)

    // 2. Start Vaadin UI asynchronously on port 8080
    VaadinBoot().apply {
        setPort(8080) // Configures the port
    }.run()           // Starts the embedded server
}