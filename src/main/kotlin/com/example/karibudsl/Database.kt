package com.example.karibudsl

import com.example.karibudsl.com.example.karibudsl.Todos
import org.h2.tools.Server // 引入 H2 服务器类
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun init() {
        // 2. 连接到内存数据库
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password =  "")

        // 3. 创建表结构
        transaction {
            SchemaUtils.create(Todos)
        }
    }
}