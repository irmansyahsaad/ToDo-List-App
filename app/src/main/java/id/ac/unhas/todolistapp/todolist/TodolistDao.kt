package id.ac.unhas.todolistapp.todolist

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodolistDao {
    @Query("Select * from todolist")
    fun getTodolists(): LiveData<List<Todolist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTodolist(todolist: Todolist)

    @Delete
    suspend fun deleteTodolist(todolist: Todolist)

    @Update
    suspend fun updateTodolist(todolist: Todolist)
}