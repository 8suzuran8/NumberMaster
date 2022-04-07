package com.example.numbermaster

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.setMargins
import com.example.numbermaster.databinding.ActivityInputBinding
import com.google.android.material.textfield.TextInputEditText

class InputActivity : NumberMasterActivity() {
    private var checkActivityIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base)

        this.checkActivityIntent = Intent(application, CheckActivity::class.java)

        this.convertIntentExtraToGlobalActivityInfo()
        this.convertGlobalActivityInfoToIntentExtra(this.checkActivityIntent!!)

        val that = this

        val buttonContainerSize = (that.globalActivityInfo["meta:rootLayoutWidth"]!!.toFloat() - (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2)).toInt()
        val max = buttonContainerSize - that.globalActivityInfo["meta:otherSize"]!!.toFloat()
        val inflateRootLayout = findViewById<FrameLayout>(R.id.root_layout)
        val activityLayout = layoutInflater.inflate(R.layout.activity_input, inflateRootLayout)
        val layoutBinding: ActivityInputBinding = ActivityInputBinding.bind(activityLayout).apply {
            val buttonContainerLayoutParams = buttonContainer.layoutParams as LinearLayout.LayoutParams
            buttonContainerLayoutParams.topMargin = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            buttonContainer.apply {
                layoutParams.height = buttonContainerSize
                layoutParams = buttonContainerLayoutParams
            }
            updateButton.apply {
                translationX = max
                translationY = max
                stateListAnimator =
                    AnimatorInflater.loadStateListAnimator(that, R.xml.animate_input_update)
                layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
                layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            }
            checkButton.apply {
                translationX = max / 2
                translationY = max / 2 - (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2)
                layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
                layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            }
        }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt())
        }
        addContentView(layoutBinding.rootLayout, layoutParams)
    }

    fun buttonClickListener(view: View) {
        when (view.id) {
            R.id.prev_button -> {
                this.finish()
            }
            R.id.update_button -> {
                this.buttonClickUpdateProcess()
            }
            R.id.check_button -> {
                this.buttonClickCheckProcess()
            }
        }
    }

    private fun buttonClickUpdateProcess() {
        val textBox = findViewById<TextInputEditText>(R.id.text_box)
        val numbers = textBox.text.toString()

        val dbHelper = NumberMasterOpenHelper(this)
        if (Util.validate(numbers) || dbHelper.testModeNumbers.contains(numbers)) {
            dbHelper.writeByInput(numbers)

            textBox.setText("")
            textBox.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.xml.animate_input_textbox_ok)
        } else {
            textBox.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.xml.animate_input_textbox_ng)
        }
    }

    private fun buttonClickCheckProcess() {
        val textBox = findViewById<TextInputEditText>(R.id.text_box)
        val numbers = textBox.text.toString()

        if (Util.validate(numbers)) {
            this.checkActivityIntent!!.putExtra("numbers", numbers)
            startActivity(this.checkActivityIntent)
            return
        } else {
            textBox.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.xml.animate_input_textbox_ng)
        }
    }
}