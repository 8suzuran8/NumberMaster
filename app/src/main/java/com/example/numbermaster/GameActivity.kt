package com.example.numbermaster

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.AudioManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.setPadding
import com.example.numbermaster.databinding.ActivityGameBinding

class GameActivity : NumberMasterActivity() {
    var numberMaster: NumberMaster? = null

    private val dialogs: MutableMap<String, AlertDialog.Builder?> = mutableMapOf(
        "3x3" to null,
        "6x6" to null,
        "9x9" to null,
        "secret" to null,
        "finish" to null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base)

        // BGMの音量をスマホのボタンに対応
        volumeControlStream = AudioManager.STREAM_MUSIC

        this.convertIntentExtraToGlobalActivityInfo()

        val that = this

        val inflateRootLayout = findViewById<FrameLayout>(R.id.root_layout)
        val activityLayout = layoutInflater.inflate(R.layout.activity_game, inflateRootLayout)
        val layoutBinding: ActivityGameBinding = ActivityGameBinding.bind(activityLayout).apply {
            status.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

            fullSpace.layoutParams.width = (that.globalActivityInfo["meta:rootLayoutShort"]!!.toFloat() - (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2)).toInt()
            fullSpace.layoutParams.height = (that.globalActivityInfo["meta:rootLayoutShort"]!!.toFloat() - (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2)).toInt()
            layoutMiddle.layoutParams.height = that.globalActivityInfo["gameSpaceSize"]!!.toFloat().toInt()
            boardStandLayout.layoutParams.width = that.globalActivityInfo["gameSpaceSize"]!!.toFloat().toInt()
            boardStandLayout.layoutParams.height = that.globalActivityInfo["gameSpaceSize"]!!.toFloat().toInt()
            boardStandLayout.setPadding(that.globalActivityInfo["boardFrameWidth"]!!.toFloat().toInt())

            buttonSwipeBottom.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            buttonSwipeLeft.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            buttonSwipeRight.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
            buttonSwipeTop.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        }

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
            setMargins(
                0,
                0,
                0,
                if (that.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
                } else {
                    0
                }
            )
        }
        addContentView(layoutBinding.rootLayout, layoutParams)

        this.dialogs["3x3"] = this.createDialog(R.id.button_3x3)
        this.dialogs["6x6"] = this.createDialog(R.id.button_6x6)
        this.dialogs["9x9"] = this.createDialog(R.id.button_9x9)
        this.dialogs["secret"] = this.createDialog(R.id.button_secret)
        this.dialogs["finish"] = this.createDialog(R.id.button_finish)
    }

    /*
    // thunderエフェクトの確認用の処理
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_Z) {
            this.numberMaster!!.counterStopEffect()
        }

        return super.onKeyUp(keyCode, event)
    }
     */

    override fun onResume() {
        super.onResume()
        if (this.numberMaster != null && this.numberMaster!!.bgmMediaPlayer != null) {
            this.numberMaster!!.bgmMediaPlayer!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this.numberMaster != null && this.numberMaster!!.bgmMediaPlayer != null) {
            this.numberMaster!!.bgmMediaPlayer!!.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.numberMaster != null && this.numberMaster!!.bgmMediaPlayer != null) {
            this.numberMaster!!.bgmMediaPlayer!!.reset()
            this.numberMaster!!.bgmMediaPlayer!!.release()
            this.numberMaster!!.bgmMediaPlayer = null
        }
    }

    private fun onFinish() {
        if (this.numberMaster != null && this.numberMaster!!.bgmMediaPlayer != null) {
            this.numberMaster!!.bgmMediaPlayer!!.reset()
            this.numberMaster!!.bgmMediaPlayer!!.release()
            this.numberMaster!!.bgmMediaPlayer = null
        }
    }

    fun buttonClickListener(view: View) {
        when (view.id) {
            R.id.prev_button -> {
                this.onFinish()
                if (this.numberMaster!!.status["stop"]!!.toInt() == 1) this.finish()
            }
            R.id.button_swipe_top -> {
                this.numberMaster!!.numberMasterOnSwipeTouchListener!!.onSwipeTop()
            }
            R.id.button_swipe_right -> {
                this.numberMaster!!.numberMasterOnSwipeTouchListener!!.onSwipeRight()
            }
            R.id.button_swipe_bottom -> {
                this.numberMaster!!.numberMasterOnSwipeTouchListener!!.onSwipeBottom()
            }
            R.id.button_swipe_left -> {
                this.numberMaster!!.numberMasterOnSwipeTouchListener!!.onSwipeLeft()
            }
            R.id.button_3x3 -> {
                this.dialogs["3x3"]!!.show()
            }
            R.id.button_6x6 -> {
                this.dialogs["6x6"]!!.show()
            }
            R.id.button_9x9 -> {
                this.dialogs["9x9"]!!.show()
            }
            R.id.button_secret -> {
                this.dialogs["secret"]!!.show()
            }
            R.id.button_finish -> {
                this.dialogs["finish"]!!.show()
            }
            R.id.button_stop -> {
                if (!this.numberMaster!!.buttons["3x3"]!!.isEnabled) {
                    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    this.requestedOrientation = if (this.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }

                this.numberMaster!!.buttonClickStopProcess()
            }
        }
    }

    private fun drawCube() {
        val that = this
        this.numberMaster!!.cube = GLSurfaceView(this).apply {
            setEGLConfigChooser(8,8,8,8,16,0)
            setRenderer(that.numberMaster!!.numberMasterRenderer)
            setZOrderOnTop(true)
            contentDescription = "cube"
            holder.setFormat(PixelFormat.TRANSPARENT)
            visibility = View.VISIBLE
        }

        findViewById<RelativeLayout>(R.id.full_space).addView(this.numberMaster!!.cube)

        // 時間のかかる初期表示を行っておく
        this.numberMaster!!.numberMasterRenderer!!.rotateStart(this.numberMaster!!.numberMasterRenderer!!.rotateDown)
        this.numberMaster!!.invisibleCubeEvent()
    }

    override fun initialProcess(globalActivityInfo: MutableMap<String, String>) {
        super.initialProcess(globalActivityInfo)

        val that = this


        findViewById<RelativeLayout>(R.id.button_container).apply {
            if (that.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                layoutParams.width = that.globalActivityInfo["meta:rootLayoutShort"]!!.toFloat().toInt()
                layoutParams.height =
                    (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2).toInt()
            } else {
                layoutParams.width =
                    (that.globalActivityInfo["meta:otherSize"]!!.toFloat() * 2).toInt()
                layoutParams.height =that.globalActivityInfo["meta:rootLayoutShort"]!!.toFloat().toInt()
            }
        }

        val button3x3 = findViewById<ImageButton>(R.id.button_3x3)
        button3x3.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        button3x3.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        val button6x6 = findViewById<ImageButton>(R.id.button_6x6)
        button6x6.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        button6x6.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        val button9x9 = findViewById<ImageButton>(R.id.button_9x9)
        button9x9.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        button9x9.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        val buttonSecret = findViewById<ImageButton>(R.id.button_secret)
        buttonSecret.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        buttonSecret.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        val buttonFinish = findViewById<ImageButton>(R.id.button_finish)
        buttonFinish.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        buttonFinish.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        val buttonStop = findViewById<ImageButton>(R.id.button_stop)
        buttonStop.layoutParams.width = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()
        buttonStop.layoutParams.height = that.globalActivityInfo["meta:otherSize"]!!.toFloat().toInt()

        this.numberMaster = NumberMaster(this, resources, this.globalActivityInfo)

        // boardStandの設定
        this.numberMaster!!.boardStandLayout = findViewById(R.id.board_stand_layout)
        this.numberMaster!!.boardStandForeground = findViewById(R.id.board_stand_foreground)

        // 0 is background
        for (numberPanelIndex in 1 until this.numberMaster!!.boardStandLayout!!.childCount) {
            this.numberMaster!!.numberPanels[numberPanelIndex - 1] = this.numberMaster!!.boardStandLayout!!.getChildAt(numberPanelIndex) as ImageButton
            this.numberMaster!!.numberPanels[numberPanelIndex - 1]!!.apply {
                setOnTouchListener(that.numberMaster!!.numberMasterOnSwipeTouchListener)
                layoutParams.apply {
                    width = that.globalActivityInfo["numberPanelSize:1"]!!.toFloat().toInt()
                    height = that.globalActivityInfo["numberPanelSize:1"]!!.toFloat().toInt()
                }
                visibility = ImageButton.INVISIBLE
                isEnabled = false
                x = 0F
                y = 0F
            }
        }

        // effectの設定
        this.numberMaster!!.effect = findViewById(R.id.effect)
        this.numberMaster!!.effect2 = findViewById(R.id.effect2)

        // buttonsの設定
        val buttonContainer = findViewById<RelativeLayout>(R.id.button_container)
        this.numberMaster!!.buttons["prev"] = findViewById(R.id.prev_button)
        this.numberMaster!!.buttons["3x3"] = buttonContainer.getChildAt(0) as ImageButton
        this.numberMaster!!.buttons["6x6"] = buttonContainer.getChildAt(1) as ImageButton
        this.numberMaster!!.buttons["9x9"] = buttonContainer.getChildAt(2) as ImageButton
        this.numberMaster!!.buttons["secret"] = buttonContainer.getChildAt(3) as ImageButton
        this.numberMaster!!.buttons["finish"] = buttonContainer.getChildAt(4) as ImageButton
        this.numberMaster!!.buttons["stop"] = buttonContainer.getChildAt(5) as ImageButton
        this.numberMaster!!.buttons["swipe_bottom"] = findViewById(R.id.button_swipe_bottom)
        this.numberMaster!!.buttons["swipe_left"] = findViewById(R.id.button_swipe_left)
        this.numberMaster!!.buttons["swipe_right"] = findViewById(R.id.button_swipe_right)
        this.numberMaster!!.buttons["swipe_top"] = findViewById(R.id.button_swipe_top)

        for (key in listOf("prev", "3x3", "6x6", "9x9", "secret", "finish", "stop")) {
            this.numberMaster!!.buttons[key]!!.apply {
                isEnabled = true
            }
        }
        for (key in listOf("swipe_bottom", "swipe_left", "swipe_right", "swipe_top")) {
            this.numberMaster!!.buttons[key]!!.apply {
                isEnabled = false
            }
        }

        // statusの設定
        this.numberMaster!!.statusText = findViewById(R.id.status)

        // cube
        this.drawCube()
        this.numberMaster!!.numberMasterRenderer!!.changeTexture(this.numberMaster!!.status["size"]!!.toInt())

        this.numberMaster!!.setEvent(window)

        if (!this.numberMaster!!.loadGame() || this.numberMaster!!.numbers[0][0][0] == 0) {
            this.numberMaster!!.updateStatus()
            this.numberMaster!!.shuffle()
        }
        this.numberMaster!!.updateNumberPanel()

        // elseの場合はxmlでOK
        if (this.numberMaster!!.settings["enabledCube"]!!.toInt() == 1) {
            val id = this.resources.getIdentifier("button_enabled_cube", "drawable", this.packageName)
            this.numberMaster!!.buttons["secret"]!!.setImageResource(id)
        }

        // ステータスの文字色
        val statusTextColor: Int

        when (this.numberMaster!!.settings["counterStopCount"]!!.toInt()) {
            0 -> {
                // 白
                statusTextColor = Color.argb(255, 255, 255, 255)
            }

            1 -> {
                // 黄
                statusTextColor = Color.argb(255, 218, 165, 32)
            }

            2 -> {
                // 赤
                statusTextColor = Color.argb(255, 218, 112, 214)
            }

            3 -> {
                // 青
                statusTextColor = Color.argb(255, 85, 51, 238)
            }

            else -> {
                // 緑
                statusTextColor = Color.argb(255, 153, 204, 51)
            }
        }

        this.numberMaster!!.statusText!!.apply {
            setTextColor(statusTextColor)
        }
    }

    private fun createDialog(buttonId: Int): AlertDialog.Builder {
        val that = this

        val messageId = when (buttonId) {
            R.id.button_finish -> R.string.message_complete_this_game
            else -> R.string.message_resize_and_shuffle
        }

        val size = when (buttonId) {
            R.id.button_3x3 -> 1
            R.id.button_6x6 -> 2
            R.id.button_9x9 -> 3
            else -> 1
        }

        return AlertDialog.Builder(this).apply {
            setTitle(R.string.message_title_game)
            setMessage(messageId)
            setPositiveButton(R.string.message_button_text_ok) { _, _ ->
                when (buttonId) {
                    R.id.button_finish -> {
                        that.numberMaster!!.buttonClickFinishProcess()
                    }
                    R.id.button_secret -> {
                        that.numberMaster!!.buttonClickSecretProcess()
                    }
                    else -> {
                        that.numberMaster!!.buttonClickSizeProcess(size)
                    }
                }
            }
            setNegativeButton(R.string.message_button_text_cancel) { _, _ ->
            }

            create()
        }
    }
}