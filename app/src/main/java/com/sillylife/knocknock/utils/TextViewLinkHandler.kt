package com.sillylife.knocknock.utils

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

abstract class TextViewLinkHandler : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return super.onTouchEvent(widget, buffer, event)
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout = widget.layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())
        val link = buffer.getSpans(off, off, URLSpan::class.java)
        if (link.isNotEmpty()) {
            onLinkClick(link[0].url)
        }
        return true
    }

    abstract fun onLinkClick(url: String?)
}