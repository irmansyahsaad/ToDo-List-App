package com.id.ac.unhas.todolistapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.unhas.todolistapp.R
import id.ac.unhas.todolistapp.todolist.Todolist
import id.ac.unhas.todolistapp.util.Commons
import id.ac.unhas.todolistapp.util.FormDialog
import id.ac.unhas.todolistapp.util.TodoAdapter
import id.ac.unhas.todolistapp.util.TodoViewModel
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var todoViewModel: TodoViewModel
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager

        todoAdapter =
            TodoAdapter() { todo, _ ->
                val options = resources.getStringArray(R.array.option_edit_delete)
                Commons.showSelector(
                    this,
                    "Choose action",
                    options
                ) { _, i ->
                    when (i) {
                        0 -> showDetailsDialog(todo)
                        1 -> showEditDialog(todo)
                        2 -> showDeleteDialog(todo)
                    }
                }
            }

        recyclerview.adapter = todoAdapter

        todoViewModel = ViewModelProvider(this).get(TodoViewModel::class.java)

        //tombol tambah data (floating action button)
        fab.setOnClickListener {
            showInsertDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData(){
        todoViewModel.getTodos()?.observe(this, Observer {
            todoAdapter.setTodoList(it)
            setProgressbarVisibility(false)
        })
    }

    private fun refreshData(){
        setProgressbarVisibility(true)
        observeData()
    }

    //insert data
    private fun showInsertDialog(){
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        val dialogTitle = "Add data"
        val toastMessage = "Data has been added successfully"
        val failAlertMessage = "Please fill all the required fields"

        FormDialog(
            this,
            dialogTitle,
            view
        ) {
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()



            if (title == "" || date == "" || time == "") {
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate =
                    Commons.convertStringToDate(
                        "dd/MM/yy",
                        date
                    )
                val dueDate =
                    Commons.formatDate(
                        parsedDate,
                        "dd/MM/yy"
                    )

                val currentDate =
                    Commons.getCurrentDateTime()
                val dateCreated =
                    Commons.formatDate(
                        currentDate,
                        "dd/MM/yy HH:mm:ss"
                    )

                val todo = Todolist(
                    title = title,
                    note = note,
                    dateCreated = dateCreated,
                    dateUpdated = dateCreated,
                    dueDate = dueDate,
                    dueTime = time
                )

                todoViewModel.insertTodo(todo)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showEditDialog(todolist: Todolist) {
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        view.input_title.setText(todolist.title)
        view.input_note.setText(todolist.note)
        view.input_due_date.setText(todolist.dueDate)
        view.input_time.setText(todolist.dueTime)


        val dialogTitle = "Edit data"
        val toastMessage = "Data has been updated successfully"
        val failAlertMessage = "Please fill all the required fields"

        FormDialog(
            this,
            dialogTitle,
            view
        ) {
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()

            val dateCreated = todolist.dateCreated
            val prevDueTime = todolist.dueTime

            if (title == "" || date == "" || time == "") {
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate =
                    Commons.convertStringToDate(
                        "dd/MM/yy",
                        date
                    )
                val dueDate =
                    Commons.formatDate(
                        parsedDate,
                        "dd/MM/yy"
                    )

                val currentDate =
                    Commons.getCurrentDateTime()
                val dateUpdated =
                    Commons.formatDate(
                        currentDate,
                        "dd/MM/yy HH:mm:ss"
                    )

                todolist.title = title
                todolist.note = note
                todolist.dateCreated = dateCreated
                todolist.dateUpdated = dateUpdated
                todolist.dueDate = dueDate
                todolist.dueTime = time


                todoViewModel.updateTodo(todolist)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showDeleteDialog(todolist: Todolist) {
        todoViewModel.deleteTodo(todolist)
        Toast.makeText(this, "Data Telah Dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun showDetailsDialog(todolist: Todolist) {
        val title = "Title: ${todolist.title}"
        val dueDate = "Due date : ${todolist.dueDate}, ${todolist.dueTime}"
        val note = "Note: ${todolist.note}"
        val dateCreated = "Date created: ${todolist.dateCreated}"
        val dateUpdated = "Date updated: ${todolist.dateUpdated}"

        val strReminder = if(todolist.remindMe) "Enabled" else "Disabled"
        val remindMe = "Reminder: $strReminder"

        val strMessage = "$title\n$dueDate\n$note\n\n$dateCreated\n$dateUpdated\n$remindMe"

        AlertDialog.Builder(this).setMessage(strMessage).setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    private fun setProgressbarVisibility(state: Boolean) {
        if (state) progressbar.visibility = View.VISIBLE
        else progressbar.visibility = View.INVISIBLE
    }
}