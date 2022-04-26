package com.example.numbermaster

import android.animation.AnimatorInflater
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams

open class NumberMasterActivity : AppCompatActivity() {
    private var onWindowFocusChangedCalled = false

    open var globalActivityInfo: MutableMap<String, String> = mutableMapOf(
        "meta:rootLayoutWidth" to 0.toString(),
        "meta:rootLayoutHeight" to 0.toString(),
        "meta:rootLayoutShort" to 0.toString(),
        "meta:rootLayoutLong" to 0.toString(),
        // 上下左右マージン
        // ステータス部分の高さ
        // 戻るボタンの高さ
        // サイズ変更等のボタンの高さ
        // 1割(左右1割ずつなので、body部分は8割)
        "meta:otherSize" to 0.toString(),
        "gameSpaceSize" to 0.toString(),
        "boardFrameWidth" to 0.toString(),
        "numberPanelSize:1" to 0.toString(),
        "numberPanelSize:2" to 0.toString(),
        "numberPanelSize:3" to 0.toString(),

        // top and bottom margin(left and right is 0)
        "swipeButtonMargin" to 0.toString(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.windowInsetsController?.apply {
            hide(WindowInsets.Type.statusBars())
            hide(WindowInsets.Type.navigationBars())
            hide(WindowInsets.Type.systemBars())
            hide(WindowInsets.Type.captionBar())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    open fun convertGlobalActivityInfoToIntentExtra(targetIntent: Intent) {
        val globalActivityInfoKeys = this.globalActivityInfo.keys
        targetIntent.putExtra("keys", globalActivityInfoKeys.joinToString(","))

        for (globalActivityInfoKey in globalActivityInfoKeys) {
            targetIntent.putExtra(globalActivityInfoKey, this.globalActivityInfo[globalActivityInfoKey])
        }
    }

    open fun convertIntentExtraToGlobalActivityInfo() {
        val keys = intent.getStringExtra("keys")!!.split(",")

        for (key in keys) {
            this.globalActivityInfo[key] = intent.getStringExtra(key)!!
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (!hasFocus) return // 離れるときは実行しない
        if (onWindowFocusChangedCalled) return // 実行ずみなら実行しない

        this.onWindowFocusChangedCalled = true
        this.initialProcess(this.globalActivityInfo)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this.initialProcess(this.globalActivityInfo)
    }

    open fun initialProcess(globalActivityInfo: MutableMap<String, String>, prevButtonAnimation: Boolean = true) {
        val that = this
        // 背景のアニメーション用
        findViewById<ImageView>(R.id.background_image1).apply {
            pivotX = if (this.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                (globalActivityInfo["meta:rootLayoutShort"]!!.toFloat() / 2)
            } else {
                (globalActivityInfo["meta:rootLayoutLong"]!!.toFloat() / 2)
            }
            pivotY = 0F
            stateListAnimator =
                AnimatorInflater.loadStateListAnimator(that, R.xml.animate_all_background1)
        }

        findViewById<ImageView>(R.id.background_image2).apply {
            if (this.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                pivotX = globalActivityInfo["meta:rootLayoutShort"]!!.toFloat()
                pivotY = globalActivityInfo["meta:rootLayoutLong"]!!.toFloat()
            } else {
                pivotX = globalActivityInfo["meta:rootLayoutLong"]!!.toFloat()
                pivotY = globalActivityInfo["meta:rootLayoutShort"]!!.toFloat()
            }
            stateListAnimator =
                AnimatorInflater.loadStateListAnimator(that, R.xml.animate_all_background2)
        }

        findViewById<ImageView>(R.id.background_image3).apply {
            if (this.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                pivotX = globalActivityInfo["meta:rootLayoutShort"]!!.toFloat()
                pivotY = globalActivityInfo["meta:rootLayoutLong"]!!.toFloat()
            } else {
                pivotX = globalActivityInfo["meta:rootLayoutLong"]!!.toFloat()
                pivotY = globalActivityInfo["meta:rootLayoutShort"]!!.toFloat()
            }
            stateListAnimator =
                AnimatorInflater.loadStateListAnimator(that, R.xml.animate_all_background3)
        }

        // 戻るボタン
        findViewById<ImageView>(R.id.prev_button_image).apply {
            translationX = if (this.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                globalActivityInfo["meta:rootLayoutShort"]!!.toFloat() - globalActivityInfo["meta:otherSize"]!!.toFloat()
            } else {
                globalActivityInfo["meta:rootLayoutLong"]!!.toFloat() - globalActivityInfo["meta:otherSize"]!!.toFloat()
            }
            updateLayoutParams {
                width = globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
                height = globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            }
            visibility = ImageView.VISIBLE
            pivotX = globalActivityInfo["meta:otherSize"]!!.toFloat() / 2
            pivotY = globalActivityInfo["meta:otherSize"]!!.toFloat()

            if (prevButtonAnimation) {
                stateListAnimator =
                    AnimatorInflater.loadStateListAnimator(that, R.xml.animate_all_prev)
            }
        }

        findViewById<TextView>(R.id.prev_button_text).apply {
            updateLayoutParams {
                height = globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            }
            visibility = TextView.VISIBLE
        }

        findViewById<ImageButton>(R.id.prev_button).apply {
            updateLayoutParams {
                height = globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            }
            visibility = ImageButton.VISIBLE
            isEnabled = true
        }
    }
}