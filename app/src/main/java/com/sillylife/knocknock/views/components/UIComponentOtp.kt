/*
 * Copyright 2017 Chaos Leong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sillylife.knocknock.views.components

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.sillylife.knocknock.R

/**
 * Provides a widget for enter PIN/OTP/password etc.
 *
 * @author Chaos Leong
 * 01/04/2017
 */
class UIComponentOtp @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.UIComponentOtp
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val mViewType: Int

    private var mPinItemCount: Int = 0

    private var mPinItemWidth: Int = 0
    private var mPinItemHeight: Int = 0
    private var mPinItemRadius: Int = 0
    private var mPinItemSpacing: Int = 0

    private val mPaint: Paint
    private val mAnimatorTextPaint = TextPaint()

    /**
     * Gets the line colors for the different states (normal, selected, focused) of the PinView.
     *
     * @attr ref R.styleable#PinView_lineColor
     * @see .setLineColor
     * @see .setLineColor
     */
    var lineColors: ColorStateList? = null
        private set

    /**
     *
     * Return the current color selected for normal line.
     *
     * @return Returns the current item's line color.
     */
    @get:ColorInt
    var currentLineColor = Color.BLACK
        private set
    private var mLineWidth: Int = 0

    private val mTextRect = Rect()
    private val mItemBorderRect = RectF()
    private val mItemLineRect = RectF()
    private val mPath = Path()
    private val mItemCenterPoint = PointF()

    private var mDefaultAddAnimator: ValueAnimator? = null
    private var isAnimationEnable = false

    private var mBlink: Blink? = null
    private var isCursorVisible: Boolean = false
    private var drawCursor: Boolean = false
    private var mCursorHeight: Float = 0.toFloat()
    private var mCursorWidth: Int = 0
    private var mCursorColor: Int = 0

    private var mItemBackgroundResource: Int = 0
    private var mItemBackground: Drawable? = null

    private var mHideLineWhenFilled: Boolean = false

    /**
     * @return Returns the width of the item's line.
     * @see .setLineWidth
     */
    /**
     * Sets the line width.
     *
     * @attr ref R.styleable#PinView_lineWidth
     * @see .getLineWidth
     */
    var lineWidth: Int
        get() = mLineWidth
        set(@Px borderWidth) {
            mLineWidth = borderWidth
            checkItemRadius()
            requestLayout()
        }

    /**
     * @return Returns the count of items.
     * @see .setItemCount
     */
    /**
     * Sets the count of items.
     *
     * @attr ref R.styleable#PinView_itemCount
     * @see .getItemCount
     */
    var itemCount: Int
        get() = mPinItemCount
        set(count) {
            mPinItemCount = count
            setMaxLength(count)
            requestLayout()
        }

    /**
     * @return Returns the radius of square.
     * @see .setItemRadius
     */
    /**
     * Sets the radius of square.
     *
     * @attr ref R.styleable#PinView_itemRadius
     * @see .getItemRadius
     */
    var itemRadius: Int
        get() = mPinItemRadius
        set(@Px itemRadius) {
            mPinItemRadius = itemRadius
            checkItemRadius()
            requestLayout()
        }

    /**
     * @return Returns the spacing between two items.
     * @see .setItemSpacing
     */
    /**
     * Specifies extra space between two items.
     *
     * @attr ref R.styleable#PinView_itemSpacing
     * @see .getItemSpacing
     */
    var itemSpacing: Int
        @Px
        get() = mPinItemSpacing
        set(@Px itemSpacing) {
            mPinItemSpacing = itemSpacing
            requestLayout()
        }

    /**
     * @return Returns the height of item.
     * @see .setItemHeight
     */
    /**
     * Sets the height of item.
     *
     * @attr ref R.styleable#PinView_itemHeight
     * @see .getItemHeight
     */
    var itemHeight: Int
        get() = mPinItemHeight
        set(@Px itemHeight) {
            mPinItemHeight = itemHeight
            updateCursorHeight()
            requestLayout()
        }

    /**
     * @return Returns the width of item.
     * @see .setItemWidth
     */
    /**
     * Sets the width of item.
     *
     * @attr ref R.styleable#PinView_itemWidth
     * @see .getItemWidth
     */
    var itemWidth: Int
        get() = mPinItemWidth
        set(@Px itemWidth) {
            mPinItemWidth = itemWidth
            checkItemRadius()
            requestLayout()
        }

    /**
     * @return Returns the width (in pixels) of cursor.
     * @see .setCursorWidth
     */
    //endregion

    //region Cursor

    /**
     * Sets the width (in pixels) of cursor.
     *
     * @attr ref R.styleable#PinView_cursorWidth
     * @see .getCursorWidth
     */
    var cursorWidth: Int
        get() = mCursorWidth
        set(@Px width) {
            mCursorWidth = width
            if (isCursorVisible()) {
                invalidateCursor(true)
            }
        }

    /**
     * Gets the cursor color.
     *
     * @return Return current cursor color.
     * @see .setCursorColor
     */
    /**
     * Sets the cursor color.
     *
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * [getColor][androidx.core.content.ContextCompat.getColor].
     * @attr ref R.styleable#PinView_cursorColor
     * @see .getCursorColor
     */
    var cursorColor: Int
        get() = mCursorColor
        set(@ColorInt color) {
            mCursorColor = color
            if (isCursorVisible()) {
                invalidateCursor(true)
            }
        }

    init {

        val res = resources

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.STROKE

        mAnimatorTextPaint.set(paint)

        val theme = context.theme

        val a = theme.obtainStyledAttributes(attrs, R.styleable.UIComponentOtp, defStyleAttr, 0)

        mViewType = a.getInt(R.styleable.UIComponentOtp_viewType, VIEW_TYPE_RECTANGLE)
        mPinItemCount = a.getInt(R.styleable.UIComponentOtp_itemCount, DEFAULT_COUNT)
        mPinItemHeight = a.getDimension(
                R.styleable.UIComponentOtp_itemHeight,
                res.getDimensionPixelSize(R.dimen.pv_pin_view_item_size).toFloat()
        ).toInt()
        mPinItemWidth = a.getDimension(
                R.styleable.UIComponentOtp_itemWidth,
                res.getDimensionPixelSize(R.dimen.pv_pin_view_item_size).toFloat()
        ).toInt()
        mPinItemSpacing = a.getDimensionPixelSize(
                R.styleable.UIComponentOtp_itemSpacing,
                res.getDimensionPixelSize(R.dimen.pv_pin_view_item_spacing)
        )
        mPinItemRadius = a.getDimension(R.styleable.UIComponentOtp_itemRadius, 0f).toInt()
        mLineWidth = a.getDimension(
                R.styleable.UIComponentOtp_lineWidth,
                res.getDimensionPixelSize(R.dimen.pv_pin_view_item_line_width).toFloat()
        ).toInt()
        lineColors = a.getColorStateList(R.styleable.UIComponentOtp_lineColor)
        isCursorVisible = a.getBoolean(R.styleable.UIComponentOtp_android_cursorVisible, true)
        mCursorColor = a.getColor(R.styleable.UIComponentOtp_cursorColor, currentTextColor)
        mCursorWidth = a.getDimensionPixelSize(
                R.styleable.UIComponentOtp_cursorWidth,
                res.getDimensionPixelSize(R.dimen.pv_pin_view_cursor_width)
        )

        mItemBackground = a.getDrawable(R.styleable.UIComponentOtp_android_itemBackground)
        mHideLineWhenFilled = a.getBoolean(R.styleable.UIComponentOtp_hideLineWhenFilled, false)

        a.recycle()

        if (lineColors != null) {
            currentLineColor = lineColors!!.defaultColor
        }
        updateCursorHeight()

        checkItemRadius()

        setMaxLength(mPinItemCount)
        mPaint.strokeWidth = mLineWidth.toFloat()
        setupAnimator()

        super.setCursorVisible(false)
        disableSelectionMenu()
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(tf, style)
    }

    override fun setTypeface(tf: Typeface?) {
        super.setTypeface(tf)
        mAnimatorTextPaint?.set(paint)
    }

    private fun setMaxLength(maxLength: Int) {
        if (maxLength >= 0) {
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        } else {
            filters = NO_FILTERS
        }
    }

    private fun setupAnimator() {
        mDefaultAddAnimator = ValueAnimator.ofFloat(0.5f, 1f)
        mDefaultAddAnimator!!.duration = 150
        mDefaultAddAnimator!!.interpolator = DecelerateInterpolator()
        mDefaultAddAnimator!!.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            val alpha = (255 * scale).toInt()
            mAnimatorTextPaint.textSize = textSize * scale
            mAnimatorTextPaint.alpha = alpha
            postInvalidate()
        }
    }

    private fun checkItemRadius() {
        if (mViewType == VIEW_TYPE_LINE) {
            val halfOfLineWidth = mLineWidth.toFloat() / 2
            if (mPinItemRadius > halfOfLineWidth) {
                throw IllegalArgumentException("The itemRadius can not be greater than lineWidth when viewType is line")
            }
        } else if (mViewType == VIEW_TYPE_RECTANGLE) {
            val halfOfItemWidth = mPinItemWidth.toFloat() / 2
            if (mPinItemRadius > halfOfItemWidth) {
                throw IllegalArgumentException("The itemRadius can not be greater than itemWidth")
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        val height: Int

        val boxHeight = mPinItemHeight

        if (widthMode == View.MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize
        } else {
            val boxesWidth = (mPinItemCount - 1) * mPinItemSpacing + mPinItemCount * mPinItemWidth
            width = boxesWidth + ViewCompat.getPaddingEnd(this) + ViewCompat.getPaddingStart(this)
            if (mPinItemSpacing == 0) {
                width -= (mPinItemCount - 1) * mLineWidth
            }
        }

        if (heightMode == View.MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize
        } else {
            height = boxHeight + paddingTop + paddingBottom
        }

        setMeasuredDimension(width, height)
    }

    override fun onTextChanged(
            text: CharSequence,
            start: Int,
            lengthBefore: Int,
            lengthAfter: Int
    ) {
        if (start != text.length) {
            moveSelectionToEnd()
        }

        makeBlink()

        if (isAnimationEnable) {
            val isAdd = lengthAfter - lengthBefore > 0
            if (isAdd) {
                if (mDefaultAddAnimator != null) {
                    mDefaultAddAnimator!!.end()
                    mDefaultAddAnimator!!.start()
                }
            }
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (focused) {
            moveSelectionToEnd()
            makeBlink()
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)

        if (selEnd != text!!.length) {
            moveSelectionToEnd()
        }
    }

    private fun moveSelectionToEnd() {
        setSelection(text!!.length)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (lineColors == null || lineColors!!.isStateful) {
            updateColors()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()

        updatePaints()
        drawPinView(canvas)

        canvas.restore()
    }

    private fun updatePaints() {
        mPaint.color = currentLineColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mLineWidth.toFloat()
        paint.color = currentTextColor
    }

    private fun drawPinView(canvas: Canvas) {
        val highlightIdx = text!!.length
        for (i in 0 until mPinItemCount) {
            val highlight = isFocused && highlightIdx == i
            mPaint.color =
                    if (highlight) getLineColorForState(*HIGHLIGHT_STATES) else currentLineColor

            updateItemRectF(i)
            updateCenterPoint()

            canvas.save()
            if (mViewType == VIEW_TYPE_RECTANGLE) {
                updatePinBoxPath(i)
                canvas.clipPath(mPath)
            }
            drawItemBackground(canvas, highlight)
            canvas.restore()

            if (highlight) {
                drawCursor(canvas)
            }

            if (mViewType == VIEW_TYPE_RECTANGLE) {
                drawPinBox(canvas, i)
            } else if (mViewType == VIEW_TYPE_LINE) {
                drawPinLine(canvas, i)
            }

            if (DBG) {
                drawAnchorLine(canvas)
            }

            if (text!!.length > i) {
                if (isPasswordInputType(inputType)) {
                    drawCircle(canvas, i)
                } else {
                    drawText(canvas, i)
                }
            } else if (!TextUtils.isEmpty(hint) && hint.length == mPinItemCount) {
                drawHint(canvas, i)
            }
        }

        // highlight the next item
        if (isFocused && text!!.length != mPinItemCount && mViewType == VIEW_TYPE_RECTANGLE) {
            val index = text!!.length
            updateItemRectF(index)
            updateCenterPoint()
            updatePinBoxPath(index)
            mPaint.color = getLineColorForState(*HIGHLIGHT_STATES)
            drawPinBox(canvas, index)
        }
    }

    private fun getLineColorForState(vararg states: Int): Int {
        return if (lineColors != null) lineColors!!.getColorForState(
                states,
                currentLineColor
        ) else currentLineColor
    }

    private fun drawItemBackground(canvas: Canvas, highlight: Boolean) {
        if (mItemBackground == null) {
            return
        }
        val delta = mLineWidth.toFloat() / 2
        val left = Math.round(mItemBorderRect.left - delta)
        val top = Math.round(mItemBorderRect.top - delta)
        val right = Math.round(mItemBorderRect.right + delta)
        val bottom = Math.round(mItemBorderRect.bottom + delta)

        mItemBackground!!.setBounds(left, top, right, bottom)
        mItemBackground!!.state = if (highlight) HIGHLIGHT_STATES else drawableState
        mItemBackground!!.draw(canvas)
    }

    private fun updatePinBoxPath(i: Int) {
        var drawRightCorner = false
        var drawLeftCorner = false
        if (mPinItemSpacing != 0) {
            drawRightCorner = true
            drawLeftCorner = drawRightCorner
        } else {
            if (i == 0 && i != mPinItemCount - 1) {
                drawLeftCorner = true
            }
            if (i == mPinItemCount - 1 && i != 0) {
                drawRightCorner = true
            }
        }
        updateRoundRectPath(
                mItemBorderRect,
                mPinItemRadius.toFloat(),
                mPinItemRadius.toFloat(),
                drawLeftCorner,
                drawRightCorner
        )
    }

    private fun drawPinBox(canvas: Canvas, i: Int) {
        if (mHideLineWhenFilled && i < text!!.length) {
            return
        }
        canvas.drawPath(mPath, mPaint)
    }

    private fun drawPinLine(canvas: Canvas, i: Int) {
        if (mHideLineWhenFilled && i < text!!.length) {
            return
        }
        var l: Boolean
        var r: Boolean
        r = true
        l = r
        if (mPinItemSpacing == 0 && mPinItemCount > 1) {
            if (i == 0) {
                // draw only left round
                r = false
            } else if (i == mPinItemCount - 1) {
                // draw only right round
                l = false
            } else {
                // draw rect
                r = false
                l = r
            }
        }
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = mLineWidth.toFloat() / 10
        val halfLineWidth = mLineWidth.toFloat() / 2
        mItemLineRect.set(
                mItemBorderRect.left - halfLineWidth,
                mItemBorderRect.bottom - halfLineWidth,
                mItemBorderRect.right + halfLineWidth,
                mItemBorderRect.bottom + halfLineWidth
        )

        updateRoundRectPath(mItemLineRect, mPinItemRadius.toFloat(), mPinItemRadius.toFloat(), l, r)
        canvas.drawPath(mPath, mPaint)
    }

    private fun drawCursor(canvas: Canvas) {
        if (drawCursor) {
            val cx = mItemCenterPoint.x
            val cy = mItemCenterPoint.y
            val y = cy - mCursorHeight / 2

            val color = mPaint.color
            val width = mPaint.strokeWidth
            mPaint.color = mCursorColor
            mPaint.strokeWidth = mCursorWidth.toFloat()

            canvas.drawLine(cx, y, cx, y + mCursorHeight, mPaint)

            mPaint.color = color
            mPaint.strokeWidth = width
        }
    }

    private fun updateRoundRectPath(rectF: RectF, rx: Float, ry: Float, l: Boolean, r: Boolean) {
        updateRoundRectPath(rectF, rx, ry, l, r, r, l)
    }

    private fun updateRoundRectPath(
            rectF: RectF, rx: Float, ry: Float,
            tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean
    ) {
        mPath.reset()

        val l = rectF.left
        val t = rectF.top
        val r = rectF.right
        val b = rectF.bottom

        val w = r - l
        val h = b - t

        val lw = w - 2 * rx// line width
        val lh = h - 2 * ry// line height

        mPath.moveTo(l, t + ry)

        if (tl) {
            mPath.rQuadTo(0f, -ry, rx, -ry)// top-left corner
        } else {
            mPath.rLineTo(0f, -ry)
            mPath.rLineTo(rx, 0f)
        }

        mPath.rLineTo(lw, 0f)

        if (tr) {
            mPath.rQuadTo(rx, 0f, rx, ry)// top-right corner
        } else {
            mPath.rLineTo(rx, 0f)
            mPath.rLineTo(0f, ry)
        }

        mPath.rLineTo(0f, lh)

        if (br) {
            mPath.rQuadTo(0f, ry, -rx, ry)// bottom-right corner
        } else {
            mPath.rLineTo(0f, ry)
            mPath.rLineTo(-rx, 0f)
        }

        mPath.rLineTo(-lw, 0f)

        if (bl) {
            mPath.rQuadTo(-rx, 0f, -rx, -ry)// bottom-left corner
        } else {
            mPath.rLineTo(-rx, 0f)
            mPath.rLineTo(0f, -ry)
        }

        mPath.rLineTo(0f, -lh)

        mPath.close()
    }

    private fun updateItemRectF(i: Int) {
        val halfLineWidth = mLineWidth.toFloat() / 2
        var left =
                scrollX.toFloat() + ViewCompat.getPaddingStart(this).toFloat() + (i * (mPinItemSpacing + mPinItemWidth)).toFloat() + halfLineWidth
        if (mPinItemSpacing == 0 && i > 0) {
            left = left - mLineWidth * i
        }
        val right = left + mPinItemWidth - mLineWidth
        val top = scrollY.toFloat() + paddingTop.toFloat() + halfLineWidth
        val bottom = top + mPinItemHeight - mLineWidth

        mItemBorderRect.set(left, top, right, bottom)
    }

    private fun drawText(canvas: Canvas, i: Int) {
        val paint = getPaintByIndex(i)
        // 1, Rect(4, -39, 20, 0)
        // 您, Rect(2, -47, 51, 3)
        // *, Rect(0, -39, 23, -16)
        // =, Rect(4, -26, 26, -10)
        // -, Rect(1, -19, 14, -14)
        // +, Rect(2, -32, 29, -3)
        drawTextAtBox(canvas, paint, text!!, i)
    }

    private fun drawHint(canvas: Canvas, i: Int) {
        val paint = getPaintByIndex(i)
        paint.color = currentHintTextColor
        drawTextAtBox(canvas, paint, hint, i)
    }

    private fun drawTextAtBox(canvas: Canvas, paint: Paint, text: CharSequence, charAt: Int) {
        paint.getTextBounds(text.toString(), charAt, charAt + 1, mTextRect)
        val cx = mItemCenterPoint.x
        val cy = mItemCenterPoint.y
        val x = cx - Math.abs(mTextRect.width().toFloat()) / 2 - mTextRect.left.toFloat()
        val y =
                cy + Math.abs(mTextRect.height().toFloat()) / 2 - mTextRect.bottom// always center vertical
        canvas.drawText(text, charAt, charAt + 1, x, y, paint)
    }

    private fun drawCircle(canvas: Canvas, i: Int) {
        val paint = getPaintByIndex(i)
        val cx = mItemCenterPoint.x
        val cy = mItemCenterPoint.y
        canvas.drawCircle(cx, cy, paint.textSize / 2, paint)
    }

    private fun getPaintByIndex(i: Int): Paint {
        if (isAnimationEnable && i == text!!.length - 1) {
            mAnimatorTextPaint.color = paint.color
            return mAnimatorTextPaint
        } else {
            return paint
        }
    }

    /**
     * For seeing the font position
     */
    private fun drawAnchorLine(canvas: Canvas) {
        var cx = mItemCenterPoint.x
        var cy = mItemCenterPoint.y
        mPaint.strokeWidth = 1f
        cx -= mPaint.strokeWidth / 2
        cy -= mPaint.strokeWidth / 2

        mPath.reset()
        mPath.moveTo(cx, mItemBorderRect.top)
        mPath.lineTo(cx, mItemBorderRect.top + Math.abs(mItemBorderRect.height()))
        canvas.drawPath(mPath, mPaint)

        mPath.reset()
        mPath.moveTo(mItemBorderRect.left, cy)
        mPath.lineTo(mItemBorderRect.left + Math.abs(mItemBorderRect.width()), cy)
        canvas.drawPath(mPath, mPaint)

        mPath.reset()

        mPaint.strokeWidth = mLineWidth.toFloat()
    }

    private fun updateColors() {
        var inval = false

        val color: Int
        if (lineColors != null) {
            color = lineColors!!.getColorForState(drawableState, 0)
        } else {
            color = currentTextColor
        }

        if (color != currentLineColor) {
            currentLineColor = color
            inval = true
        }

        if (inval) {
            invalidate()
        }
    }

    private fun updateCenterPoint() {
        val cx = mItemBorderRect.left + Math.abs(mItemBorderRect.width()) / 2
        val cy = mItemBorderRect.top + Math.abs(mItemBorderRect.height()) / 2
        mItemCenterPoint.set(cx, cy)
    }

    /*@Override
    protected MovementMethod getDefaultMovementMethod() {
        return DefaultMovementMethod.getInstance();
    }*/

    /**
     * Sets the line color for all the states (normal, selected,
     * focused) to be this color.
     *
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * [getColor][androidx.core.content.ContextCompat.getColor].
     * @attr ref R.styleable#PinView_lineColor
     * @see .setLineColor
     * @see .getLineColors
     */
    fun setLineColor(@ColorInt color: Int) {
        lineColors = ColorStateList.valueOf(color)
        updateColors()
    }

    /**
     * Sets the line color.
     *
     * @attr ref R.styleable#PinView_lineColor
     * @see .setLineColor
     * @see .getLineColors
     */
    fun setLineColor(colors: ColorStateList?) {
        if (colors == null) {
            throw NullPointerException()
        }

        lineColors = colors
        updateColors()
    }

    /**
     * Specifies whether the text animation should be enabled or disabled.
     * By the default, the animation is disabled.
     *
     * @param enable True to start animation when adding text, false to transition immediately
     */
    fun setAnimationEnable(enable: Boolean) {
        isAnimationEnable = enable
    }

    /**
     * Specifies whether the line (border) should be hidden or visible when text entered.
     * By the default, this flag is false and the line is always drawn.
     *
     * @param hideLineWhenFilled true to hide line on a position where text entered,
     * false to always show line
     * @attr ref R.styleable#PinView_hideLineWhenFilled
     */
    fun setHideLineWhenFilled(hideLineWhenFilled: Boolean) {
        this.mHideLineWhenFilled = hideLineWhenFilled
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        updateCursorHeight()
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        updateCursorHeight()
    }

    //region ItemBackground

    /**
     * Set the item background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the item background.
     *
     * @param resId The identifier of the resource.
     * @attr ref R.styleable#PinView_android_itemBackground
     */
    fun setItemBackgroundResources(@DrawableRes resId: Int) {
        if (resId != 0 && mItemBackgroundResource != resId) {
            return
        }
        mItemBackground = ResourcesCompat.getDrawable(resources, resId, context.theme)
        setItemBackground(mItemBackground)
        mItemBackgroundResource = resId
    }

    /**
     * Sets the item background color for this view.
     *
     * @param color the color of the item background
     */
    fun setItemBackgroundColor(@ColorInt color: Int) {
        if (mItemBackground is ColorDrawable) {
            (mItemBackground!!.mutate() as ColorDrawable).color = color
            mItemBackgroundResource = 0
        } else {
            setItemBackground(ColorDrawable(color))
        }
    }

    /**
     * Set the item background to a given Drawable, or remove the background.
     *
     * @param background The Drawable to use as the item background, or null to remove the
     * item background
     */
    fun setItemBackground(background: Drawable?) {
        mItemBackgroundResource = 0
        mItemBackground = background
        invalidate()
    }

    override fun setCursorVisible(visible: Boolean) {
        if (isCursorVisible != visible) {
            isCursorVisible = visible
            invalidateCursor(isCursorVisible)
            makeBlink()
        }
    }

    override fun isCursorVisible(): Boolean {
        return isCursorVisible
    }

    override fun onScreenStateChanged(screenState: Int) {
        super.onScreenStateChanged(screenState)
        when (screenState) {
            View.SCREEN_STATE_ON -> resumeBlink()
            View.SCREEN_STATE_OFF -> suspendBlink()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resumeBlink()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        suspendBlink()
    }

    private fun shouldBlink(): Boolean {
        return isCursorVisible() && isFocused
    }

    private fun makeBlink() {
        if (shouldBlink()) {
            if (mBlink == null) {
                mBlink = Blink()
            }
            removeCallbacks(mBlink)
            drawCursor = false
            postDelayed(mBlink, BLINK.toLong())
        } else {
            if (mBlink != null) {
                removeCallbacks(mBlink)
            }
        }
    }

    private fun suspendBlink() {
        if (mBlink != null) {
            mBlink!!.cancel()
            invalidateCursor(false)
        }
    }

    private fun resumeBlink() {
        if (mBlink != null) {
            mBlink!!.uncancel()
            makeBlink()
        }
    }

    private fun invalidateCursor(showCursor: Boolean) {
        if (drawCursor != showCursor) {
            drawCursor = showCursor
            invalidate()
        }
    }

    private fun updateCursorHeight() {
        val delta = 2 * dpToPx(2f)
        mCursorHeight = if (mPinItemHeight - textSize > delta) textSize + delta else textSize
    }

    private inner class Blink : Runnable {
        private var mCancelled: Boolean = false

        override fun run() {
            if (mCancelled) {
                return
            }

            removeCallbacks(this)

            if (shouldBlink()) {
                invalidateCursor(!drawCursor)
                postDelayed(this, BLINK.toLong())
            }
        }

        fun cancel() {
            if (!mCancelled) {
                removeCallbacks(this)
                mCancelled = true
            }
        }

        internal fun uncancel() {
            mCancelled = false
        }
    }
    //endregion

    //region Selection Menu
    private fun disableSelectionMenu() {
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                // no-op
            }
        }
        isLongClickable = false
    }

    override fun isSuggestionsEnabled(): Boolean {
        return false
    }
    //endregion

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    companion object {

        private val TAG = "PinView"

        private val DBG = false

        private val BLINK = 500

        private val DEFAULT_COUNT = 6

        private val NO_FILTERS = arrayOfNulls<InputFilter>(0)

        private val HIGHLIGHT_STATES = intArrayOf(android.R.attr.state_selected)

        private val VIEW_TYPE_RECTANGLE = 0
        private val VIEW_TYPE_LINE = 1
        private val VIEW_TYPE_NONE = 2

        private fun isPasswordInputType(inputType: Int): Boolean {
            val variation =
                    inputType and (EditorInfo.TYPE_MASK_CLASS or EditorInfo.TYPE_MASK_VARIATION)
            return (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                    || variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
                    || variation == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
        }
    }
}