package com.example.ejerprac2_juegodecrculos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lottieBackground = findViewById<LottieAnimationView>(R.id.lottieBackground)
        lottieBackground.playAnimation()  // Iniciar animación manualmente

        findViewById<Button>(R.id.ButtonIniciar).setOnClickListener {
            startActivity(Intent(this, JuegoActivity::class.java))
        }
    }
}
