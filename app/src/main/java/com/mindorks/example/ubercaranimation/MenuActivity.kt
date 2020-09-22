package com.mindorks.example.ubercaranimation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        img_lo_trinh.setOnClickListener {
            startActivity(Intent(this@MenuActivity, LoTrinhActivity::class.java))
        }
        img_giam_sat.setOnClickListener {
            startActivity(Intent(this@MenuActivity, GiamSatActivity::class.java))
        }
    }
}