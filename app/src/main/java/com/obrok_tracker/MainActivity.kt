package com.obrok_tracker

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.lang.StringBuilder
import java.util.Calendar
import java.util.Date

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

class MainActivity : AppCompatActivity(){
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
        findViewById<Button>(R.id.invisibleButton).visibility = View.INVISIBLE
        val settingsButton: Button = findViewById(R.id.settingButton)
        val valueText: TextView = findViewById(R.id.valueText)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        settingsButton.text = ""
        progressBar.max = readFromFile(FILE_WEEKLY_BUDGET).toInt()
        animateTextViewAndProgressBar(valueText, progressBar, readFromFile(FILE_BUDGET).toInt(), true)
        findViewById<Button>(R.id.subButton).setOnLongClickListener {
            buttonLongClick(findViewById(R.id.subButton))
            return@setOnLongClickListener true
        }
    }

    private fun animateTextViewAndProgressBar(textView: TextView, progressBar: ProgressBar, endNumber: Int, changeColor: Boolean = false) {
        //takes the desired end number and animates the current number to desired number
        //used to be 2 separate functions but this is prob faster and doesn't create 2 threads
        if (textView.text.isEmpty() || !textView.text.isDigitsOnly()) {
            textView.text = "0"
            progressBar.progress = 0
        }
        if (textView.text.toString().toInt() != endNumber) {
            val maxLoops = 5000
            Thread {
                run {
                    var i = 0
                    var curNum: Int = textView.text.toString().toInt()
                    //this is all color stuff [might remove progressBar color change its clunky and doesn't look that good]
                    val curTextViewColor = textView.currentTextColor // get current color so you can change it back
                    val curProgressBarSecondaryProgressTintList = progressBar.secondaryProgressTintList // get current color so you can change it back
                    val curProgressBarProgressTintList = progressBar.progressTintList // get current color so you can change it back
                    if(curNum < endNumber && changeColor){
                        textView.setTextColor(getColor(R.color.light_green))
                        progressBar.secondaryProgressTintList = ColorStateList.valueOf(getColor(R.color.light_green))
                        progressBar.progressTintList = ColorStateList.valueOf(getColor(R.color.light_green))
                    }
                    if(curNum > endNumber && changeColor){
                        textView.setTextColor(getColor(R.color.light_red))
                        progressBar.secondaryProgressTintList = ColorStateList.valueOf(getColor(R.color.light_red))
                        progressBar.progressTintList = ColorStateList.valueOf(getColor(R.color.light_red))
                    }
                    //
                    while (curNum != endNumber) {
                        if (curNum < endNumber) {
                            curNum += 1
                        }
                        if (curNum > endNumber) {
                            curNum -= 1
                        }
                        runOnUiThread {// i understand this but not really
                            run {
                                textView.text = curNum.toString()
                                progressBar.progress = curNum
                            }
                        }
                        try {
                            Thread.sleep(1)
                            i++
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        if (i >= maxLoops) {//fail safe if loop get stuck which it shouldn't
                            break
                        }
                    }
                    //change back to original color
                    if(changeColor){
                        textView.setTextColor(curTextViewColor)
                        progressBar.secondaryProgressTintList = curProgressBarSecondaryProgressTintList
                        progressBar.progressTintList = curProgressBarProgressTintList
                    }
                    //
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
            overridePendingTransition(0,0) //override transition animation so there isn't one
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

    fun layoutClick(view: View){
        if(findViewById<Button>(R.id.invisibleButton).visibility == View.VISIBLE){
            findViewById<Button>(R.id.invisibleButton).visibility = View.INVISIBLE
            //animation
            //val moveDown = AnimationUtils.loadAnimation(applicationContext,R.anim.move_down)
            //findViewById<Button>(R.id.invisibleButton).startAnimation(moveDown)
        }
    }

    private fun buttonLongClick(view: View){
        when(view.id){
            R.id.subButton -> {
                //TODO: Do something with this button
                findViewById<Button>(R.id.invisibleButton).visibility = View.VISIBLE
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