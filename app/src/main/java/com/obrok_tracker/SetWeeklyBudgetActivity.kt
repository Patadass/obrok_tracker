package com.obrok_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.lang.StringBuilder

class SetWeeklyBudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_weekly_budget)

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
        val inputText: TextView = findViewById(R.id.textInput)
        inputText.text = null
    }

    fun buttonClick(view: View){
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
                if(sb.length > 8){
                    textView.text = null
                    Snackbar.make(findViewById(R.id.main),"Number too high",Snackbar.LENGTH_SHORT).show()
                    return
                }
                setWeeklyBudget(sb.toString().toInt())
                val intent = Intent(this, SetWeeklyBudgetActivity::class.java)
                setResult(20,intent)
                this.finish()// closes subActivity so it caned be accessed again by pressing back button
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

    private fun setWeeklyBudget(budget: Int){
        writeToFile(FILE_WEEKLY_BUDGET, budget.toString())
    }

    private fun writeToFile(fileName: String, text: String){
        val ctx = applicationContext
        val file = File(ctx.filesDir,fileName)
        file.delete()
        file.createNewFile()
        file.appendText(text)
    }
}