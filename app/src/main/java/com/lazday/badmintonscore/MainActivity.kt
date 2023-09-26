package com.lazday.badmintonscore

import android.os.Bundle
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.lazday.badmintonscore.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var vibrator: Vibrator
    private var textToSpeech: TextToSpeech? = null

    private var scoreA: Int = 0
    private var scoreB: Int = 0
    private var isAddedA: Boolean = false
    private var isPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        actionBar?.hide()
        supportActionBar?.hide()

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        textToSpeech = TextToSpeech(this, this)

        binding.imgSpeak.setOnClickListener { speakOut() }

        setScore()
        binding.btnPlusA.setOnClickListener { view ->
            if (scoreA < maxScore()) {
                scoreA += 1
                isPlaying = true
                isAddedA = true
            }
            setScore()
        }
        binding.btnMinA.setOnClickListener { view ->
            if (scoreA != 0) {
                scoreA -= 1
//                isAddedA = false
            }
            setScore()
        }
        binding.btnPlusB.setOnClickListener { view ->
            if (scoreB < maxScore()) {
                scoreB += 1
                isPlaying = true
                isAddedA = false
            }
            setScore()
        }
        binding.btnMinB.setOnClickListener { view ->
            if (scoreB != 0) {
                scoreB -= 1
//                isAddedA = true
            }
            setScore()
        }

        binding.textReset.setOnClickListener { view ->
            scoreA = 0; scoreB = 0
            isPlaying = false
            vibrate()
            buttonEnable(isEnable = true)
            setScore()
        }
    }

    private fun speakOut() {
        var scoreText =
            if (isAddedA) "${getNumberText(scoreA)} ${getNumberText(scoreB)}"
            else "${getNumberText(scoreB)} ${getNumberText(scoreA)}"

        if (scoreA == 0 && scoreB == 0) {
            scoreText = "kosong kosong, ayo main!"
        }

        textToSpeech!!.speak(
            scoreText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )
    }

    private fun setScore(){

        Log.e("setScore", "maxScore ${maxScore().toString()} scoreA ${scoreA.toString()} scoreB ${scoreB.toString()}")

        binding.textInformation.apply {
            background = AppCompatResources.getDrawable(
                this@MainActivity,
                if (scoreA == 0 && scoreB == 0) R.color.purple_500
                else if (scoreA == 20 || scoreA == 22 && scoreB != maxScore()) android.R.color.holo_red_dark
                else if (scoreB == 20 || scoreB == 22 && scoreA != maxScore()) android.R.color.holo_red_dark
                else if (scoreA == maxScore() || scoreB == maxScore()) android.R.color.holo_green_dark
                else R.color.purple_700
            )
            text = if (scoreA == 0 && scoreB == 0) "BADMINTON SCORE"
            else if (scoreA == 20 || scoreA == 22 && scoreB != maxScore()) "GAME POINT"
            else if (scoreB == 20 || scoreB == 22 && scoreA != maxScore()) "GAME POINT"
            else if (scoreA == maxScore() || scoreB == maxScore()) "GAME"
            else "PLAYING"
        }

        binding.textScoreA.apply {
            text = scoreA.toString()
            setTextColor(
                AppCompatResources.getColorStateList(
                    this@MainActivity,
                    when (scoreA) {
                        0 -> android.R.color.darker_gray
                        20 -> android.R.color.holo_red_dark
                        22 -> android.R.color.holo_red_dark
                        maxScore() -> android.R.color.holo_green_dark
                        else -> R.color.purple_500
                    }
                )
            )
        }
        binding.textScoreB.apply {
            text = scoreB.toString()
            setTextColor(
                AppCompatResources.getColorStateList(
                    this@MainActivity,
                    when (scoreB) {
                        0 -> android.R.color.darker_gray
                        20 -> android.R.color.holo_red_dark
                        22 -> android.R.color.holo_red_dark
                        maxScore() -> android.R.color.holo_green_dark
                        else -> R.color.purple_500
                    }
                )
            )
        }

        if (isPlaying) {
            binding.imgShuttlecockA.visibility = if (isAddedA) View.VISIBLE else View.GONE
            binding.imgShuttlecockB.visibility = if (isAddedA) View.GONE else View.VISIBLE
        } else {
            listOf<View>( binding.imgShuttlecockA, binding.imgShuttlecockB )
                .forEach { it.visibility = View.GONE }
        }

        binding.textReset.visibility = if (isPlaying) View.VISIBLE else View.GONE

        if (scoreA == 0 && scoreB == 0) {
            isPlaying = false
        } else if (scoreA == maxScore() || scoreB == maxScore()) {
            vibrate()
            buttonEnable(isEnable = false)
            isPlaying = false
        } else if (scoreA > 19 || scoreB > 19) {
            vibrate()
        }

    }

    private fun maxScore(): Int {
        val maxScore = if (scoreA > 19 && scoreB > 19 && isPlaying) 23 else 21
//        Log.e("", "maxScore ${maxScore.toString()} scoreA ${scoreA.toString()} scoreB ${scoreB.toString()}")
        return maxScore
    }

    private fun buttonEnable(
        isEnable: Boolean
    ){
        listOf<View>(
            binding.btnPlusA,
            binding.btnPlusB,
            binding.btnMinA,
            binding.btnMinB,
        ).forEach {
            it.isEnabled = isEnable
        }
    }

    private fun vibrate() {
        vibrator.vibrate(111)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            } else {
                binding.imgSpeak.isEnabled = true
            }
        }
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }
}