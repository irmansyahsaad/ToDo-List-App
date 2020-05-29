package id.ac.unhas.todolistapp.todolist

import android.app.Application
import androidx.lifecycle.LiveData
import id.ac.unhas.todolistapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TodolistRepository(application: Application) {

    private val todolistDao: TodolistDao?
    private var todolists: LiveData<List<Todolist>>? = null

    init {
        val db = AppDatabase.getInstance(application.applicationContext)
        todolistDao = db?.todolistDao()
        todolists = todolistDao?.getTodolists()
    }

    fun getTodolists(): LiveData<List<Todolist>>? {
        return todolists
    }

    fun insert(todolist: Todolist) = runBlocking {
        this.launch(Dispatchers.IO) {
            todolistDao?.insertTodolist(todolist)
        }
    }

    fun delete(todolist: Todolist) {
        runBlocking {
            this.launch(Dispatchers.IO) {
                todolistDao?.deleteTodolist(todolist)
            }
        }
    }

    fun update(todolist: Todolist) = runBlocking {
        this.launch(Dispatchers.IO) {
            todolistDao?.updateTodolist(todolist)
        }
    }

}