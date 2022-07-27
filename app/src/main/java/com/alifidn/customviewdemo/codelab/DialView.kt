package com.alifidn.customviewdemo.codelab

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.alifidn.customviewdemo.R
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var radius = 0.0f                   // Radius of the circle. / Radius dari bulat-nya
    private var fanSpeed = FanSpeed.OFF         // The active selection. / Pilihan yang aktif

    /** position variable which will be used to draw label and indicator circle position **/
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedHighColor = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        /** isClickable = true **/ // Main Method of isClickable
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedHighColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
        updateContentDescription()

        /** Optional TalkBack Accessibility **/
         /** Note : for emulator and in some older devices "Accessibility" is unavailable,
         *          you need to download the "Google Accessibility Suite" app
         *          from Google Play Store (The Play Store needs to be enabled
         *          for the virtual device)
         **/
        /**
            ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View?,
                    info: AccessibilityNodeInfoCompat?
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfo.ACTION_CLICK,
                        context.getString(if (fanSpeed != FanSpeed.HIGH) R.string.change else R.string.reset)
                    )
                    info.addAction(customClick)
                }
            })
        */
    }

    /** isClickable = true **/ // Other Method of isClickable
    override fun isClickable(): Boolean {
        if (super.isClickable()) return true
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        updateContentDescription()

        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        /** Angles are in radians / Sudut dalam radian **/
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        /** Set the Dial Background color to Green if the selection is not Off **/
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedHighColor
        } as Int

        /** Draw the Dial / Menggambar Dial-Nya **/
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        /** Draw the Indicator Circle / Menggambar Indicator Circle-Nya **/
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        /** Draw the Text Labels / Menggambar Text Labels-Nya **/
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }

    /** ContentDescription **/
    fun updateContentDescription() {
        contentDescription = resources.getString(fanSpeed.label)
    }
}