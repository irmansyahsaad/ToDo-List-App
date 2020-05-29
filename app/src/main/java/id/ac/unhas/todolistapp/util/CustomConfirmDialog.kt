package id.ac.unhas.todolistapp.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import id.ac.unhas.todolistapp.R
import kotlinx.android.synthetic.main.dialog_confirm.*

class CustomConfirmDialog(context: Context, private val title: String, private val message: String, private val isCancelable: Boolean = true, private var btnPositiveText: String ="Yes", private var btnNegativeText: String = "No", private val yesAction: () -> Unit): Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.dialog_confirm)

        setCanceledOnTouchOutside(isCancelable)

        tv_title.text = title
        tv_message.text = message
        btn_no.text = btnNegativeText
        btn_yes.text = btnPositiveText
        btn_no.setOnClickListener { dismiss() }
        btn_yes.setOnClickListener {
            yesAction()
            dismiss()
        }
    }
}