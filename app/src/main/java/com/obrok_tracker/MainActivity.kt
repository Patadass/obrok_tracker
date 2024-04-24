package com.obrok_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.max

//RESULT CODES
//MainActivity start at 00 going 01,02,03....
//SubActivity start at 10 going 11,12,13....
//SetWeeklyBudgetActivity start at 20 going 21,22,23....
//settingActivity start at 30 going 31,32,33....

const val FILE_BUDGET = "current_budget.txt"
const val FILE_DATE = "date.txt"
const val FILE_WEEKLY_BUDGET = "weekly_budget.txt"
const val DEFAULT_WEEKLY_BUDGET = 840
val FILE_LIST = arrayListOf(FILE_BUDGET, FILE_DATE, FILE_WEEKLY_BUDGET)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        createNecessaryFiles()
        createActivity()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createActivity(){
        //redraws changes
        if(isNewWeek()){
            resetBudget()
        }
        val settingsButton: Button = findViewById(R.id.settingButton)
        val valueText: TextView = findViewById(R.id.valueText)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        valueText.text = readFromFile(FILE_BUDGET)
        settingsButton.text = ""
        progressBar.max = readFromFile(FILE_WEEKLY_BUDGET).toInt()
        animateProgressBar(progressBar,readFromFile(FILE_BUDGET).toInt())
    }
    
    private fun animateProgressBar(progressBar: ProgressBar, endProgress: Int){
        //takes the desired end progress and animates the current progress to desired progress
        if(progressBar.progress != endProgress){
            val maxLoops = 5000
            Thread {
                run {
                    var i = 0
                    while (progressBar.progress != endProgress) {
                        if (progressBar.progress < endProgress) {
                            progressBar.progress+=1
                        }
                        if (progressBar.progress > endProgress) {
                            progressBar.progress-=1
                        }
                        try {
                            Thread.sleep(1)
                            i++
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        if(i >= maxLoops){//fail safe if loop get stuck which it shouldn't
                            break
                        }
                    }
                }
            }.start()
        }
    }

    private fun resetBudget(){
        //resets the budgets to the set weekly budget
        writeToFile(FILE_BUDGET, readFromFile(FILE_WEEKLY_BUDGET))
    }

    private fun isNewWeek(): Boolean{
        //checks if a new week has passed from the last known change in budget which is saved in date.txt
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val text = readFromFile(FILE_DATE)
        if(text.isEmpty()){
            return false
        }
        val weekOfYear = StringBuilder()
        val year = StringBuilder()
        var switch = false
        for(letter in text){
            if(letter == ';'){
                switch = true
                continue
            }
            if(!switch){
                weekOfYear.append(letter)
            }else{
                year.append(letter)
            }
        }
        if(year.isEmpty()){
            year.append("0")
        }
        if(weekOfYear.isEmpty()){
            weekOfYear.append("0")
        }
        if(year.toString().toInt() != calendar.get(Calendar.YEAR)){
            return true
        }
        if(weekOfYear.toString().toInt() != calendar.get(Calendar.WEEK_OF_YEAR)){
            return true
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 0){
            overridePendingTransition(R.anim.static_animation,R.anim.static_animation)
        }
        if(resultCode == 30){// requestCode #1 means we are coming from SetWeeklyBudgetActivity
            Snackbar.make(findViewById(R.id.main),"Weekly budget set to ${readFromFile(
                FILE_WEEKLY_BUDGET)}",Snackbar.LENGTH_SHORT).show()
            resetBudget()
        }
        if(resultCode == 31){
            resetBudget()
            Snackbar.make(findViewById(R.id.main),"Budget has been reset",Snackbar.LENGTH_SHORT).show()
        }
        createActivity()
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun buttonClick(view: View) {
        //button click handling
        when(view.id){
            R.id.subButton -> {
                val intent = Intent(this, SubActivity::class.java)
                startActivityForResult(intent, 0)//no IDEA what requestCode is (but i use it to distinguish which activity we are coming from *see onActivityResult())
            }
            R.id.settingButton -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent,1)
            }
            R.id.buttonReset -> {
                resetBudget()
                createActivity()
            }
        }
    }

    private fun createNecessaryFiles(){
        //creates necessary files if they don't exist
        val ctx = applicationContext
        println("CREATING NECESSARY FILES")
        for(fileItem in FILE_LIST){
            val file = File(ctx.filesDir,fileItem)
            if(file.exists()){
                println("$fileItem GOOD")
            }else{
                file.createNewFile()
                writeToFile(fileItem, "0")
                if(fileItem == FILE_WEEKLY_BUDGET){
                    writeToFile(fileItem, DEFAULT_WEEKLY_BUDGET.toString())
                }
                println("$fileItem CREATED")
            }
        }
    }

    private fun writeToFile(fileName: String, text: String){
        //write to given file name in ctx dir
        val ctx = applicationContext
        val file = File(ctx.filesDir,fileName)
        file.delete()
        file.createNewFile()
        file.appendText(text)
    }

    private fun readFromFile(fileName: String): String {
        //read from given file name in ctx dir
        val ctx = applicationContext
        val file = File(ctx.filesDir, fileName)
        return FileInputStream(file).bufferedReader().use { it.readText() }
    }
}