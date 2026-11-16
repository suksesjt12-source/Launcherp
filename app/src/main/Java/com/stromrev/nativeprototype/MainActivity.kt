package com.stormrev.nativeprototype

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var glView: ArenaView
    private lateinit var controls: TouchOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        glView = ArenaView(this)
        controls = TouchOverlay(this) { x, y, attack, guard, jump, chakra ->
            glView.queueEvent { NativeBridge.setInput(x, y, attack, guard, jump, chakra) }
        }
        val root = FrameLayout(this)
        root.addView(glView, FrameLayout.LayoutParams(-1, -1))
        root.addView(controls, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    override fun onPause() { super.onPause(); if (::glView.isInitialized) glView.onPause() }
    override fun onResume() { super.onResume(); if (::glView.isInitialized) glView.onResume() }
}

private class ArenaView(activity: Activity) : GLSurfaceView(activity) {
    init {
        setEGLContextClientVersion(3)
        setRenderer(ArenaRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }
}

private class ArenaRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) = NativeBridge.init()
    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) = NativeBridge.resize(width, height)
    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) = NativeBridge.draw()
}

private object NativeBridge {
    init { System.loadLibrary("stormnative") }
    external fun init()
    external fun resize(width: Int, height: Int)
    external fun draw()
    external fun setInput(x: Float, y: Float, attack: Boolean, guard: Boolean, jump: Boolean, chakra: Boolean)
}

private class TouchOverlay(activity: Activity, val send: (Float, Float, Boolean, Boolean, Boolean, Boolean) -> Unit) : View(activity) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var joyX = 0f; private var joyY = 0f
    private var attack = false; private var guard = false; private var jump = false; private var chakra = false

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat(); val h = height.toFloat(); val unit = min(w, h)
        p.color = 0x55303030; p.style = Paint.Style.FILL
        c.drawCircle(unit * .15f, h * .77f, unit * .105f, p)
        p.color = 0x99FFFFFF; p.style = Paint.Style.STROKE; p.strokeWidth = 3f
        c.drawCircle(unit * .15f, h * .77f, unit * .105f, p)
        p.color = 0x77FFFFFF; c.drawCircle(unit * (.15f + joyX * .065f), h * (.77f - joyY * .065f), unit * .045f, p)
        drawButton(c, w - unit*.13f, h*.75f, unit*.055f, "ATK", attack)
        drawButton(c, w - unit*.25f, h*.87f, unit*.052f, "GUARD", guard)
        drawButton(c, w - unit*.13f, h*.53f, unit*.052f, "JUMP", jump)
        drawButton(c, w - unit*.28f, h*.58f, unit*.052f, "CHAKRA", chakra)
        p.style = Paint.Style.FILL; p.color = Color.WHITE; p.textSize = unit*.025f; p.typeface = Typeface.DEFAULT_BOLD
        c.drawText("NATIVE ANDROID TEST BUILD  •  NOT THE ORIGINAL GAME", 24f, 34f, p)
    }

    private fun drawButton(c: Canvas, x: Float, y: Float, r: Float, label: String, active: Boolean) {
        p.style = Paint.Style.FILL; p.color = if (active) 0xCCFF9F43.toInt() else 0x66505050
        c.drawCircle(x, y, r, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = Color.WHITE; c.drawCircle(x, y, r, p)
        p.style = Paint.Style.FILL; p.textAlign = Paint.Align.CENTER; p.textSize = r*.34f; p.typeface = Typeface.DEFAULT_BOLD
        c.drawText(label, x, y + r*.12f, p); p.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w = width.toFloat(); val h = height.toFloat(); val unit = min(w,h)
        val cx = unit*.15f; val cy = h*.77f; val jr = unit*.12f
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                joyX = 0f; joyY = 0f; attack = false; guard = false; jump = false; chakra = false
                for (i in 0 until e.pointerCount) {
                    val x = e.getX(i); val y = e.getY(i)
                    if (x < w*.42f && kotlin.math.hypot((x-cx).toDouble(), (y-cy).toDouble()) < jr*1.8) {
                        joyX = ((x-cx)/jr).coerceIn(-1f,1f); joyY = ((cy-y)/jr).coerceIn(-1f,1f)
                    } else {
                        if (x > w-unit*.19f && y > h*.60f && y < h*.90f) attack = true
                        if (x > w-unit*.33f && x < w-unit*.19f && y > h*.72f) guard = true
                        if (x > w-unit*.19f && y < h*.65f) jump = true
                        if (x > w-unit*.36f && x < w-unit*.20f && y < h*.70f) chakra = true
                    }
                }
                send(joyX, joyY, attack, guard, jump, chakra); invalidate(); return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                joyX = 0f; joyY = 0f; attack = false; guard = false; jump = false; chakra = false
                send(0f,0f,false,false,false,false); invalidate(); return true
            }
        }
        return true
    }
}
