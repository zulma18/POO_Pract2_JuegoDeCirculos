package com.example.ejerprac2_juegodecrculos

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class JuegoActivity : AppCompatActivity() {
    private lateinit var cuadrilla: GridLayout
    private lateinit var textoNivel: TextView
    private lateinit var textoCronometro: TextView
    private lateinit var circulos: List<View>
    private val manejador = Handler(Looper.getMainLooper())
    private var puntaje = 0
    private var nivelActual = 1
    private var circulosPorNivel = 5
    private var circulosVerdes = 0
    private var contadorIniciado = false
    private var cronometro: CountDownTimer? = null
    private lateinit var sonidoAcierto: MediaPlayer
    private lateinit var sonidoError: MediaPlayer
    private lateinit var sonidoFin: MediaPlayer
    private var clicRealizado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        cuadrilla = findViewById(R.id.gridLayout)
        textoNivel = findViewById(R.id.levelText)
        textoCronometro = findViewById(R.id.timerText)
        circulos = List(12) { crearCirculo() }
        circulos.forEach { cuadrilla.addView(it) }

        sonidoAcierto = MediaPlayer.create(this, R.raw.acierto)
        sonidoError = MediaPlayer.create(this, R.raw.error)
        sonidoFin = MediaPlayer.create(this, R.raw.fin)

        iniciarNivel()
    }

    private fun crearCirculo(): View {
        return View(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            background = ContextCompat.getDrawable(this@JuegoActivity, R.drawable.circle_red)
            setOnClickListener { alHacerClickEnCirculo(this) }
        }
    }

    private fun iniciarNivel() {
        textoNivel.text = "Nivel $nivelActual"
        clicRealizado = false
        circulosVerdes = 0
        circulosPorNivel = 5 + (nivelActual - 1) * 2
        contadorIniciado = false
        reiniciarCirculos()
        mostrarProximoCirculoVerde()
    }

    private fun reiniciarCirculos() {
        circulos.forEach { it.background = ContextCompat.getDrawable(this, R.drawable.circle_red) }
    }

    private fun mostrarProximoCirculoVerde() {
        if (circulosVerdes < circulosPorNivel) {
            manejador.postDelayed({
                cambiarCirculoAleatorio()
                circulosVerdes++
                mostrarProximoCirculoVerde()
            }, 1000)
        } else {
            // Solo avanzar si el usuario ha hecho clic
            if (nivelActual == 1 && !clicRealizado) {
                mostrarResultadosFinales()
            } else if (nivelActual == 4) {
                finalizarJuego(true)
            } else {
                nivelActual++
                iniciarNivel()
            }
        }
    }

    private fun cambiarCirculoAleatorio() {
        val circulosRojos = circulos.filter {
            it.background.constantState == ContextCompat.getDrawable(this, R.drawable.circle_red)?.constantState
        }
        if (circulosRojos.isNotEmpty()) {
            val circuloAleatorio = circulosRojos.random()
            circuloAleatorio.background = ContextCompat.getDrawable(this, R.drawable.circle_green)

            if (!contadorIniciado) {
                iniciarCronometro()
                contadorIniciado = true
            }
        }
    }

    private fun iniciarCronometro() {
        cronometro?.cancel()
        textoCronometro.visibility = View.VISIBLE

        val tiempoInicial = when (nivelActual) {
            1 -> 10000
            2 -> 8000
            3 -> 6000
            else -> 5000
        }

        cronometro = object : CountDownTimer(tiempoInicial.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textoCronometro.text = "Tiempo: ${millisUntilFinished / 1000} s"
            }

            override fun onFinish() {
                sonidoError.start()
                if (nivelActual == 1 && !clicRealizado) {
                    mostrarResultadosFinales()
                } else {
                    finalizarJuego(false)
                }
            }
        }.start()
    }

    private fun alHacerClickEnCirculo(vista: View) {
        if (vista.background.constantState == ContextCompat.getDrawable(this, R.drawable.circle_green)?.constantState) {
            puntaje++
            clicRealizado = true
            vista.background = ContextCompat.getDrawable(this, R.drawable.circle_red)
            cronometro?.cancel()
            textoCronometro.visibility = View.INVISIBLE
            sonidoAcierto.start()
        } else {
            sonidoError.start()
            finalizarJuego(false)
        }
    }

    private fun finalizarJuego(completado: Boolean) {
        manejador.removeCallbacksAndMessages(null)
        cronometro?.cancel()
        sonidoFin.start()

        val intent = Intent(this, ResultadosActivity::class.java)
        intent.putExtra("PUNTAJE", puntaje)
        intent.putExtra("COMPLETADO", completado)
        intent.putExtra("NIVEL", nivelActual)
        startActivity(intent)
        finish()
    }

    private fun mostrarResultadosFinales() {
        manejador.removeCallbacksAndMessages(null)
        cronometro?.cancel()
        sonidoFin.start()

        val intent = Intent(this, ResultadosActivity::class.java)
        intent.putExtra("PUNTAJE", 0) // En nivel 1 sin clics, puntaje es 0
        intent.putExtra("COMPLETADO", false)
        intent.putExtra("NIVEL", 1)
        startActivity(intent)
        finish()
    }
}
