package id.ac.unhas.todolistapp

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.unhas.todolistapp.adapter.TodoAdapter
import id.ac.unhas.todolistapp.todolist.Todolist
import id.ac.unhas.todolistapp.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_todo.view.*

class MainActivity : AppCompatActivity() {

    companion object{
        var isSortByDateCreated = true
    }

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var alarmReceiver: AlarmReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager

        todoAdapter = TodoAdapter(this){ todo, _ ->
            val options = resources.getStringArray(R.array.option_edit_delete)
            Commons.showSelector(this, "Select action", options) { _, i ->
                when (i) {
                    0 -> showDetailsDialog(todo)
                    1 -> showEditDialog(todo)
                    2 -> showDeleteDialog(todo)
                }
            }
        }

        recyclerview.adapter = todoAdapter

        todoViewModel = ViewModelProvider(this).get(TodoViewModel::class.java)

        swipe_refresh_layout.setOnRefreshListener {
            refreshData()
        }

        fab.setOnClickListener {
            showInsertDialog()
        }

        alarmReceiver = AlarmReceiver()
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
        swipe_refresh_layout.isRefreshing = false
    }

    private fun showInsertDialog(){
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)
        var reminderTimeCategory = 0

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        view.input_remind_time.setOnClickListener {
            val options = resources.getStringArray(R.array.options_reminder)
            Commons.showSelector(this, "Select reminder time", options) { _, i ->
                view.input_remind_time.setText(options[i])
                reminderTimeCategory = i
            }
        }

        view.input_remind_me.setOnClickListener {
            view.input_remind_time.visibility = if (view.input_remind_me.isChecked) View.VISIBLE else View.GONE
        }

        if (view.input_remind_me.isChecked)
            view.input_remind_time.visibility = View.VISIBLE

        val dialogTitle = "Add data"
        val toastMessage = "Data has been added successfully"
        val failAlertMessage = "Please fill all the required fields"

        FormDialog(this, dialogTitle, view){
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()

            val remindMe = view.input_remind_me.isChecked

            if (title == "" || date == "" || time == "") {
                android.app.AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateCreated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                val todo = Todolist(
                    title = title,
                    note = note,
                    dateCreated = dateCreated,
                    dateUpdated = dateCreated,
                    dueDate = dueDate,
                    dueTime = time,
                    remindMe = remindMe,
                    remindTime = reminderTimeCategory
                )

                todoViewModel.insertTodo(todo)

                if (remindMe)
                    setReminder(reminderTimeCategory, title, dueDate, time)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showEditDialog(todo: Todolist) {
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)
        var reminderTimeCategory = todo.remindTime

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        view.input_remind_time.setOnClickListener {
            val options = resources.getStringArray(R.array.options_reminder)
            Commons.showSelector(this, "Select reminder time", options) { _, i ->
                view.input_remind_time.setText(options[i])
                reminderTimeCategory = i
            }
        }

        view.input_remind_me.setOnClickListener {
            view.input_remind_time.visibility = if (view.input_remind_me.isChecked) View.VISIBLE else View.GONE
        }

        if (view.input_remind_me.isChecked)
            view.input_remind_time.visibility = View.VISIBLE

        view.input_title.setText(todo.title)
        view.input_note.setText(todo.note)
        view.input_due_date.setText(todo.dueDate)
        view.input_time.setText(todo.dueTime)
        view.input_remind_me.isChecked = todo.remindMe

        val reminderTime = Commons.getReminderTimeFromCategory(reminderTimeCategory)
        view.input_remind_time.setText(reminderTime)

        val dialogTitle = "Edit data"
        val toastMessage = "Data has been updated successfully"
        val failAlertMessage = "Please fill all the required fields"

        FormDialog(this, dialogTitle, view){
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()

            val dateCreated = todo.dateCreated
            val remindMe = view.input_remind_me.isChecked
            val prevDueTime = todo.dueTime

            if (title == "" || date == "" || time == "") {
                android.app.AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateUpdated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                todo.title = title
                todo.note = note
                todo.dateCreated = dateCreated
                todo.dateUpdated = dateUpdated
                todo.dueDate = dueDate
                todo.dueTime = time
                todo.remindMe = remindMe
                todo.remindTime = reminderTimeCategory

                todoViewModel.updateTodo(todo)

                if (remindMe && prevDueTime!=time)
                    setReminder(reminderTimeCategory, title, dueDate, time)
                else if (!remindMe)
                    alarmReceiver.cancelAlarm(this)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showDeleteDialog(todo: Todolist) {
        val dialogTitle = "Delete"
        val dialogMessage = "Are you sure want to delete this data?"
        val toastMessage = "Data has been deleted successfully"
        CustomConfirmDialog(this, dialogTitle, dialogMessage) {
            todoViewModel.deleteTodo(todo)
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showDetailsDialog(todo: Todolist) {
        val title = "Title: ${todo.title}"
        val dueDate = "Due date : ${todo.dueDate}, ${todo.dueTime}"
        val note = "Note: ${todo.note}"
        val dateCreated = "Date created: ${todo.dateCreated}"
        val dateUpdated = "Date updated: ${todo.dateUpdated}"

        val strReminder = if(todo.remindMe) "Enabled" else "Disabled"
        val remindMe = "Reminder: $strReminder"

        val strMessage = "$title\n$dueDate\n$note\n\n$dateCreated\n$dateUpdated\n$remindMe"

        android.app.AlertDialog.Builder(this).setMessage(strMessage).setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    private fun setReminder(timeCategory: Int, title: String, dueDate: String, dueTime: String){
        when (timeCategory) {
            0 -> alarmReceiver.setReminderAlarm(this, timeCategory, dueDate, dueTime,"$title is due in 1 hour")
            1 -> alarmReceiver.setReminderAlarm(this, timeCategory, dueDate, dueTime,"$title is due in 6 hours")
            2 -> alarmReceiver.setReminderAlarm(this, timeCategory, dueDate, dueTime,"$title is due in 12 hours")
            3 -> alarmReceiver.setReminderAlarm(this, timeCategory, dueDate, dueTime,"$title is due tomorrow")
            4 -> alarmReceiver.setReminderAlarm(this, timeCategory, dueDate, dueTime,"$title is due in 2 days")
        }
    }

    private fun setProgressbarVisibility(state: Boolean) {
        if (state) progressbar.visibility = View.VISIBLE
        else progressbar.visibility = View.INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = (menu.findItem(R.id.menu_search_toolbar)).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Search tasks"
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                todoAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                todoAdapter.filter.filter(newText)
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> true
            R.id.action_sort_date_created -> {
                isSortByDateCreated = true
                refreshData()
                true
            }
            R.id.action_sort_due_date -> {
                isSortByDateCreated = false
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
