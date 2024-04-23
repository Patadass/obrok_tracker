package com.obrok_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
        setContentView(R.layout.activity_sub)

        createActivity()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createActivity(){
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)
        val button4: Button = findViewById(R.id.button4)
        val button5: Button = findViewById(R.id.button5)
        val button6: Button = findViewById(R.id.button6)
        val button7: Button = findViewById(R.id.button7)
        val button8: Button = findViewById(R.id.button8)
        val button9: Button = findViewById(R.id.button9)
        val button0: Button = findViewById(R.id.button0)
        val buttonDone: Button = findViewById(R.id.buttonDone)
        val buttonBack: Button = findViewById(R.id.buttonBack)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.max = readFromFile(FILE_WEEKLY_BUDGET).toInt()
        progressBar.progress = readFromFile(FILE_BUDGET).toInt()
        buttonBack.text = "B"
        buttonDone.text = "E"
        button0.text = "0"
        button1.text = "1"
        button2.text = "2"
        button3.text = "3"
        button4.text = "4"
        button5.text = "5"
        button6.text = "6"
        button7.text = "7"
        button8.text = "8"
        button9.text = "9"

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
        val textView: TextView = findViewById(R.id.textInput)
        val button: Button = findViewById(view.id)
        val sb = StringBuilder()
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