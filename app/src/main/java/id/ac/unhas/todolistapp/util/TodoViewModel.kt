package id.ac.unhas.todolistapp.util

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import id.ac.unhas.todolistapp.todolist.Todolist
import id.ac.unhas.todolistapp.todolist.TodolistRepository

class TodoViewModel(application: Application): AndroidViewModel(application) {
    private var todoRepository = TodolistRepository(application)
    private var todos: LiveData<List<Todolist>>? = todoRepository.getTodolists()


    fun getTodos(): LiveData<List<Todolist>>? {
        return todos
    }

    fun insertTodo(todolist: Todolist) {
        todoRepository.insert(todolist)
    }

    fun deleteTodo(todolist: Todolist) {
        todoRepository.delete(todolist)
    }

    fun updateTodo(todolist: Todolist) {
        todoRepository.update(todolist)
    }
}