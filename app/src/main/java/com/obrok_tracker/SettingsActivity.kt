package com.obrok_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//overrides so i can call createActivity()
        if(resultCode == 20){// requestCode #1 means we are coming from SetWeeklyBudgetActivity
            val intent = Intent(this, SettingsActivity::class.java)
            setResult(30,intent)
            this.finish()
        }
        if(resultCode == RESULT_CANCELED){
            this.finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun buttonClick(view: View){
        when(view.id){
            R.id.buttonCWB -> {
                val intent = Intent(this, SetWeeklyBudgetActivity::class.java)
                startActivityForResult(intent, 0)
            }
            R.id.buttonReset -> {
                val intent = Intent(this, SettingsActivity::class.java)
                setResult(31,intent)
                this.finish()
            }
            R.id.buttonExit -> {
                val intent = Intent(this, MainActivity::class.java)
                setResult(RESULT_CANCELED,intent)
                this.finish()
            }
        }
    }
}