package com.obrok_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.lang.StringBuilder
import java.util.Date
import java.util.Calendar

class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        overridePendingTransition(0,0)

        setContentView(R.layout.activity_sub)

        createActivity()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createActivity(){
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.max = readFromFile(FILE_WEEKLY_BUDGET).toInt()
        progressBar.progress = readFromFile(FILE_BUDGET).toInt()
        val valueText: TextView = findViewById(R.id.valueText)
        val textInput: TextView = findViewById(R.id.textInput)
        textInput.text = null
        valueText.text = readFromFile(FILE_BUDGET)
    }

    private fun readFromFile(fileName: String): String {
        val ctx = applicationContext
        val file = File(ctx.filesDir, fileName)
        return FileInputStream(file).bufferedReader().use { it.readText() }
    }

    private fun writeToFile(fileName: String, text: String){
        val ctx = applicationContext
        val file = File(ctx.filesDir,fileName)
        file.delete()
        file.createNewFile()
        file.appendText(text)
    }

    private fun writeDateToFile(fileName: String){
        //write to file in [ weekOfYear;year ] csv format
        val sb = StringBuilder()
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        sb.setLength(0)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        sb.append(weekOfYear.toString()).append(";").append(year.toString())
        writeToFile(fileName,sb.toString())
    }

    fun buttonClick(view: View){
        //button click handling
        val textView: TextView = findViewById(R.id.textInput)
        val button: Button = findViewById(view.id)
        val sb = StringBuilder()
        if(view.id == R.id.buttonExit){
            val intent = Intent(this, MainActivity::class.java)
            setResult(RESULT_CANCELED, intent)
            this.finish()
        }
        if(view.id == R.id.buttonDone){
            if(textView.text.isNotEmpty()){
                sb.append(textView.text)
                val budget = readFromFile(FILE_BUDGET).toInt()
                val expense = sb.toString().toInt()
                if(expense > budget){
                    textView.text = null
                    Snackbar.make(findViewById(R.id.main),"Not enough funds",Snackbar.LENGTH_SHORT).show()
                    return
                }
                writeToFile(FILE_BUDGET, (budget - expense).toString())
                writeDateToFile(FILE_DATE)
                this.finish()// closes subActivity so it caned be accessed again by pressing back button
                val intent = Intent(this, SubActivity::class.java)
                setResult(RESULT_OK,intent)
            }
            return
        }
        if(view.id == R.id.buttonBack){
            if(textView.text.isNotEmpty()){
                sb.append(textView.text)
                sb.deleteAt(sb.length-1)
            }
        }else{// keypad input formatting
            if(textView.text.isEmpty() && button.text == "0"){
                return
            }
            sb.append(textView.text).append(button.text)
        }
        textView.text = sb.toString()
    }
}