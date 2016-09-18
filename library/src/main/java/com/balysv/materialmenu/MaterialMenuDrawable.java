/*
 * Copyright (C) 2014 Balys Valentukevicius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.balysv.materialmenu;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.util.TypedValue;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import static android.graphics.Paint.Style;

public class MaterialMenuDrawable extends Drawable implements MaterialMenu, Animatable {

    public enum IconState {
        BURGER, ARROW, X, CHECK, HIDE
    }

    public enum AnimationState {
        BURGER_ARROW, BURGER_X, BURGER_CHECK, BURGER_HIDE,
        ARROW_X, ARROW_CHECK, ARROW_HIDE,
        X_CHECK, X_HIDE,
        CHECK_HIDE;

        public IconState getFirstState() {
            switch (this) {
                case BURGER_ARROW:
                    return IconState.BURGER;
                case BURGER_X:
                    return IconState.BURGER;
                case BURGER_CHECK:
                    return IconState.BURGER;
                case BURGER_HIDE:
                    return IconState.BURGER;
                case ARROW_X:
                    return IconState.ARROW;
                case ARROW_CHECK:
                    return IconState.ARROW;
                case ARROW_HIDE:
                    return IconState.ARROW;
                case X_CHECK:
                    return IconState.X;
                case X_HIDE:
                    return IconState.X;
                case CHECK_HIDE:
                    return IconState.CHECK;
                default:
                    return null;
            }
        }

        public IconState getSecondState() {
            switch (this) {
                case BURGER_ARROW:
                    return IconState.ARROW;
                case BURGER_X:
                    return IconState.X;
                case BURGER_CHECK:
                    return IconState.CHECK;
                case BURGER_HIDE:
                    return IconState.HIDE;
                case ARROW_X:
                    return IconState.X;
                case ARROW_CHECK:
                    return IconState.CHECK;
                case ARROW_HIDE:
                    return IconState.HIDE;
                case X_CHECK:
                    return IconState.CHECK;
                case X_HIDE:
                    return IconState.HIDE;
                case CHECK_HIDE:
                    return IconState.HIDE;
                default:
                    return null;
            }
        }
    }

    public enum Stroke {
        /**
         * 3 dip
         */
        BOLD(3),
        /**
         * 2 dip
         */
        REGULAR(2),
        /**
         * 1 dip
         */
        THIN(1);

        private final int strokeWidth;

        Stroke(int strokeWidth) {
            this.strokeWidth = strokeWidth;
        }

        protected static Stroke valueOf(int strokeWidth) {
            switch (strokeWidth) {
                case 3:
                    return BOLD;
                case 2:
                default:
                    return REGULAR;
                case 1:
                    return THIN;
            }
        }
    }

    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_SCALE = 1;
    public static final int DEFAULT_TRANSFORM_DURATION = 800;
    public static final boolean DEFAULT_VISIBLE = true;

    private static final int BASE_DRAWABLE_WIDTH = 40;
    private static final int BASE_DRAWABLE_HEIGHT = 40;
    private static final int BASE_ICON_WIDTH = 20;
    private static final int BASE_CIRCLE_RADIUS = 18;

    private static final float ARROW_MID_LINE_ANGLE = 180;
    private static final float ARROW_TOP_LINE_ANGLE = 135;
    private static final float ARROW_BOT_LINE_ANGLE = 225;
    private static final float X_TOP_LINE_ANGLE = 44;
    private static final float X_BOT_LINE_ANGLE = -44;
    private static final float X_ROTATION_ANGLE = 90;
    private static final float CHECK_MIDDLE_ANGLE = 135;
    private static final float CHECK_BOTTOM_ANGLE = -90;

    private static final float TRANSFORMATION_START = 0;
    private static final float TRANSFORMATION_MID = 1.0f;
    private static final float TRANSFORMATION_END = 2.0f;

    private static final int DEFAULT_CIRCLE_ALPHA = 200;

    private final float mDipH;
    private final float mDip1;
    private final float mDip2;
    private final float mDip3;
    private final float mDip4;
    private final float mDip8;

    private final int mWidth;
    private final int mHeight;
    private final float mStrokeWidth;
    private final float mIconWidth;
    private final float mTopPadding;
    private final float mSidePadding;
    private final float mCircleRadius;

    private final Stroke mStroke;

    private final Object mLock = new Object();

    private final Paint mIconPaint = new Paint();
    private final Paint mCirclePaint = new Paint();

    private float mTransformationValue = 0f;
    private boolean mTransformationRunning = false;

    private IconState mCurrentIconState = IconState.BURGER;
    private AnimationState mAnimationState = AnimationState.BURGER_ARROW;

    private IconState mAnimatingIconState;
    private boolean mVisible;
    private boolean mRtlEnabled;

    private ObjectAnimator mTransformation;
    private AnimatorListener mAnimatorListener;

    private MaterialMenuState mMaterialMenuState;

    public MaterialMenuDrawable(Context context, int color, Stroke stroke) {
        this(context, color, stroke, DEFAULT_SCALE, DEFAULT_TRANSFORM_DURATION);
    }

    public MaterialMenuDrawable(Context context, int color, Stroke stroke, int transformDuration) {
        this(context, color, stroke, DEFAULT_SCALE, transformDuration);
    }

    public MaterialMenuDrawable(Context context, int color, Stroke stroke, int scale, int transformDuration) {
        Resources resources = context.getResources();
        // convert each separately due to various densities
        mDip1 = dpToPx(resources, 1) * scale;
        mDip2 = dpToPx(resources, 2) * scale;
        mDip3 = dpToPx(resources, 3) * scale;
        mDip4 = dpToPx(resources, 4) * scale;
        mDip8 = dpToPx(resources, 8) * scale;
        mDipH = mDip1 / 2;

        mStroke = stroke;
        mVisible = DEFAULT_VISIBLE;
        mWidth = (int) (dpToPx(resources, BASE_DRAWABLE_WIDTH) * scale);
        mHeight = (int) (dpToPx(resources, BASE_DRAWABLE_HEIGHT) * scale);
        mIconWidth = dpToPx(resources, BASE_ICON_WIDTH) * scale;
        mCircleRadius = dpToPx(resources, BASE_CIRCLE_RADIUS) * scale;
        mStrokeWidth = dpToPx(resources, stroke.strokeWidth) * scale;

        mSidePadding = (mWidth - mIconWidth) / 2;
        mTopPadding = (mHeight - 5 * mDip3) / 2;

        initPaint(color);
        initAnimations(transformDuration);

        mMaterialMenuState = new MaterialMenuState();
    }

    private MaterialMenuDrawable(
            int color, Stroke stroke, long transformDuration, int width, int height,
            float iconWidth, float circleRadius, float strokeWidth, float dip1
    ) {
        mDip1 = dip1;
        mDip2 = dip1 * 2;
        mDip3 = dip1 * 3;
        mDip4 = dip1 * 4;
        mDip8 = dip1 * 8;
        mDipH = dip1 / 2;
        mStroke = stroke;
        mWidth = width;
        mHeight = height;
        mIconWidth = iconWidth;
        mCircleRadius = circleRadius;
        mStrokeWidth = strokeWidth;
        mSidePadding = (width - iconWidth) / 2;
        mTopPadding = (height - 5 * mDip3) / 2;

        initPaint(color);
        initAnimations((int) transformDuration);

        mMaterialMenuState = new MaterialMenuState();
    }

    private void initPaint(int color) {
        mIconPaint.setAntiAlias(true);
        mIconPaint.setStyle(Style.STROKE);
        mIconPaint.setStrokeWidth(mStrokeWidth);
        mIconPaint.setColor(color);

        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Style.FILL);
        mCirclePaint.setColor(color);
        mCirclePaint.setAlpha(DEFAULT_CIRCLE_ALPHA);

        setBounds(0, 0, mWidth, mHeight);
    }

    /*
     * Drawing
     */

    @Override
    public void draw(Canvas canvas) {
        if (!mVisible) return;

        final float ratio = mTransformationValue <= 1 ? mTransformationValue : 2 - mTransformationValue;

        if (mRtlEnabled) {
            canvas.save();
            canvas.scale(-1, 1, 0, 0);
            canvas.translate(-getIntrinsicWidth(), 0);
        }

        drawTopLine(canvas, ratio);
        drawMiddleLine(canvas, ratio);
        drawBottomLine(canvas, ratio);

        if (mRtlEnabled) {
            canvas.restore();
        }
    }

    private void drawTopLine(Canvas canvas, float ratio) {
        canvas.save();

        float transformRatio;

        float rotation = 0, pivotX = 0, pivotY = 0;
        float rotation2 = 0;
        // pivot at center of line
        float pivotX2 = mWidth / 2 + mDip3 / 2;
        float pivotY2 = mTopPadding + mDip2;

        float startX = mSidePadding;
        float startY = mTopPadding + mDip2;
        float stopX = mWidth - mSidePadding;
        float stopY = mTopPadding + mDip2;
        int alpha = 255;

        switch (mAnimationState) {

            case BURGER_ARROW:
                if (isMorphingForward()) {
                    // rotate until required angle
                    rotation = ratio * ARROW_BOT_LINE_ANGLE;
                } else {
                    // rotate back to start doing a 360
                    rotation = ARROW_BOT_LINE_ANGLE + (1 - ratio) * ARROW_TOP_LINE_ANGLE;
                }
                // rotate by middle
                pivotX = mWidth / 2;
                pivotY = mHeight / 2;
                // shorten both ends
                stopX -= resolveStrokeModifier(ratio);
                startX += mDip3 * ratio;
                break;

            case BURGER_X:
                // rotate until required angles
                rotation = X_TOP_LINE_ANGLE * ratio;
                // pivot at left corner of line
                pivotX = mSidePadding + mDip4;
                pivotY = mTopPadding + mDip3;
                // shorten one end
                startX += mDip3 * ratio;
                break;

            case BURGER_CHECK:
                // fade out
                alpha = (int) ((1 - ratio) * 255);
                break;

            case BURGER_HIDE:
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, 0, .6f);
                } else {
                    transformRatio = transformRatio(ratio, .4f, .9f);
                }
                startX = (1 - transformRatio) * startX + transformRatio * startX / 1.5f;
                stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                break;

            case ARROW_X:
                // rotate from ARROW angle to X angle
                rotation = ARROW_BOT_LINE_ANGLE + (X_TOP_LINE_ANGLE - ARROW_BOT_LINE_ANGLE) * ratio;
                rotation2 = X_ROTATION_ANGLE * ratio;
                // move pivot from ARROW pivot to X pivot
                pivotX = mWidth / 2 + (mSidePadding + mDip4 - mWidth / 2) * ratio;
                pivotY = mHeight / 2 + (mTopPadding + mDip3 - mHeight / 2) * ratio;
                // lengthen both ends
                stopX -= resolveStrokeModifier(ratio);
                startX += mDip3;
                break;

            case ARROW_CHECK:
                // fade out
                alpha = (int) ((1 - ratio) * 255);
                // retain starting arrow configuration
                rotation = ARROW_BOT_LINE_ANGLE;
                pivotX = mWidth / 2;
                pivotY = mHeight / 2;
                // shorted both ends
                stopX -= resolveStrokeModifier(1);
                startX += mDip3;
                break;

            case ARROW_HIDE:
                // rotate to required angle
                rotation = ARROW_BOT_LINE_ANGLE;
                // rotate by middle
                pivotX = mWidth / 2;
                pivotY = mHeight / 2;
                // slide
                float slide = isMorphingForward() ? 0 : transformRatio(ratio, .1f, 1) * mSidePadding / 8;
                startY += slide;
                stopY += slide;
                // shorten left ends
                stopX -= resolveStrokeModifier(1) + slide;
                // shorten right end
                transformRatio = transformRatio(ratio,
                        isMorphingForward() ? .0f : 0,
                        isMorphingForward() ? .5f : .4f);
                startX = (1 - transformRatio) * (startX - slide + mDip3) + transformRatio * (stopX + mDip2);
                if (startX > stopX) {
                    startX = stopX;
                }
                break;

            case X_CHECK:
                // retain X configuration
                rotation = X_TOP_LINE_ANGLE;
                rotation2 = X_ROTATION_ANGLE;
                pivotX = mSidePadding + mDip4;
                pivotY = mTopPadding + mDip3;
                stopX += mDip3 - mDip3 * (1 - ratio);
                startX += mDip3;
                // fade out
                alpha = (int) ((1 - ratio) * 255);
                break;

            case X_HIDE:
                // rotate to required angles
                rotation = X_TOP_LINE_ANGLE;
                rotation2 = X_ROTATION_ANGLE;
                // pivot at left corner of line
                pivotX = mSidePadding + mDip4;
                pivotY = mTopPadding + mDip3;
                // shorten one end
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, .4f, .92f);
                } else {
                    transformRatio = transformRatio(ratio, 0, .5f);
                }
                if (isMorphingForward()) {
                    startX += mDip3;
                    stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                } else {
                    startX = (1 - transformRatio) * (startX + mDip3) + transformRatio * stopX;
                }
                break;

            case CHECK_HIDE:
                // hide
                alpha = 0;
                break;
        }

        mIconPaint.setAlpha(alpha);
        canvas.rotate(rotation, pivotX, pivotY);
        canvas.rotate(rotation2, pivotX2, pivotY2);
        canvas.drawLine(startX, startY, stopX, stopY, mIconPaint);
        mIconPaint.setAlpha(255);
    }

    private void drawMiddleLine(Canvas canvas, float ratio) {
        canvas.restore();
        canvas.save();

        float transformRatio;

        float rotation = 0;
        float pivotX = mWidth / 2;
        float pivotY = mWidth / 2;
        float startX = mSidePadding;
        float startY = mTopPadding + mDip3 / 2 * 5;
        float stopX = mWidth - mSidePadding;
        float stopY = mTopPadding + mDip3 / 2 * 5;
        int alpha = 255;

        switch (mAnimationState) {

            case BURGER_ARROW:
                // rotate by 180
                if (isMorphingForward()) {
                    rotation = ratio * ARROW_MID_LINE_ANGLE;
                } else {
                    rotation = ARROW_MID_LINE_ANGLE + (1 - ratio) * ARROW_MID_LINE_ANGLE;
                }
                // shorten one end
                stopX -= ratio * resolveStrokeModifier(ratio) / 2;
                break;

            case BURGER_X:
                // shorten the length
                transformRatio = transformRatio(ratio, 0, .9f);
                stopX -= transformRatio * (stopX - startX);
                break;

            case BURGER_CHECK:
                // rotate until required angle
                rotation = ratio * CHECK_MIDDLE_ANGLE;
                // lengthen both ends
                startX += ratio * (mDip4 + mDip3 / 2);
                stopX += ratio * mDip1;
                pivotX = mWidth / 2 + mDip3 + mDipH;
                break;

            case BURGER_HIDE:
                transformRatio = transformRatio(ratio, .2f, .8f);
                startX = (1 - transformRatio) * startX + transformRatio * startX / 1.5f;
                stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                break;

            case ARROW_X:
                // shorten the length
                transformRatio = transformRatio(ratio, 0, .6f);
                startX += mDip2 + transformRatio * (stopX - startX - mDip2);
                break;

            case ARROW_CHECK:
                if (isMorphingForward()) {
                    // rotate until required angle
                    rotation = ratio * CHECK_MIDDLE_ANGLE;
                } else {
                    // rotate back to starting angle
                    rotation = CHECK_MIDDLE_ANGLE - CHECK_MIDDLE_ANGLE * (1 - ratio);
                }
                // shorten one end and lengthen the other
                startX += mDip3 / 2 + mDip4 - (1 - ratio) * mDip2;
                stopX += ratio * mDip1;
                pivotX = mWidth / 2 + mDip3 + mDipH;
                break;

            case ARROW_HIDE:
                // shorten left end
                if (isMorphingForward()) {
                    float slideRatio = transformRatio(ratio, .4f, .9f);
                    transformRatio = transformRatio(ratio, .5f, .9f);
                    startX = (1 - slideRatio) * (startX + resolveStrokeModifier(1) / 2) + slideRatio * (startX - mSidePadding / 6);
                    stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                } else {
                    float slide = transformRatio(ratio, .1f, 1) * mSidePadding;
                    transformRatio = transformRatio(ratio, .6f, 1);
                    stopX += slide / 2;
                    startX = (1 - transformRatio) * (startX + slide / 4 + resolveStrokeModifier(1) / 2) + transformRatio * stopX;
                }
                break;

            case X_CHECK:
                // fade in
                alpha = (int) (ratio * 255);
                // rotation to check angle
                rotation = ratio * CHECK_MIDDLE_ANGLE;
                // lengthen both ends
                startX += ratio * (mDip4 + mDip3 / 2);
                stopX += ratio * mDip1;
                pivotX = mWidth / 2 + mDip3 + mDipH;
                break;

            case X_HIDE:
                // hide
                alpha = 0;
                break;

            case CHECK_HIDE:
                // rotate to required angle
                rotation = CHECK_MIDDLE_ANGLE;
                pivotX = mWidth / 2 + mDip3 + mDipH;
                // change length
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, .3f, .9f);
                    startX += mDip4 + mDip3 / 2;
                    stopX = (1 - transformRatio) * (stopX + mDip1 + mDipH / 2) + transformRatio * startX;
                } else {
                    transformRatio = transformRatio(ratio, 0, 0.7f);
                    stopX -= 1.5 * mDipH;
                    startX = (1 - transformRatio) * (startX + mDip4 + mDip3 / 2) + transformRatio * stopX;
                }
                break;
        }

        mIconPaint.setAlpha(alpha);
        canvas.rotate(rotation, pivotX, pivotY);
        canvas.drawLine(startX, startY, stopX, stopY, mIconPaint);
        mIconPaint.setAlpha(255);
    }

    private void drawBottomLine(Canvas canvas, float ratio) {
        canvas.restore();
        canvas.save();

        float transformRatio;

        float rotation = 0, pivotX = 0, pivotY = 0;
        float rotation2 = 0;
        // pivot at center of line
        float pivotX2 = mWidth / 2 + mDip3 / 2;
        float pivotY2 = mHeight - mTopPadding - mDip2;

        float startX = mSidePadding;
        float startY = mHeight - mTopPadding - mDip2;
        float stopX = mWidth - mSidePadding;
        float stopY = mHeight - mTopPadding - mDip2;
        int alpha = 255;

        switch (mAnimationState) {

            case BURGER_ARROW:
                if (isMorphingForward()) {
                    // rotate to required angle
                    rotation = ARROW_TOP_LINE_ANGLE * ratio;
                } else {
                    // rotate back to start doing a 360
                    rotation = ARROW_TOP_LINE_ANGLE + (1 - ratio) * ARROW_BOT_LINE_ANGLE;
                }
                // pivot center of canvas
                pivotX = mWidth / 2;
                pivotY = mHeight / 2;
                // shorten both ends
                stopX = mWidth - mSidePadding - resolveStrokeModifier(ratio);
                startX = mSidePadding + mDip3 * ratio;
                break;

            case BURGER_X:
                // rotate until required angles
                rotation = X_BOT_LINE_ANGLE * ratio;
                // pivot left corner of line
                pivotX = mSidePadding + mDip4;
                pivotY = mHeight - mTopPadding - mDip3;
                // shorten one end
                startX += mDip3 * ratio;
                break;

            case BURGER_CHECK:
                // rotate from ARROW angle to CHECK angle
                rotation = ratio * (CHECK_BOTTOM_ANGLE + ARROW_TOP_LINE_ANGLE);
                // move pivot from BURGER pivot to CHECK pivot
                pivotX = mWidth / 2 + mDip3 * ratio;
                pivotY = mHeight / 2 - mDip3 * ratio;
                // length stays same as BURGER
                startX += mDip8 * ratio;
                stopX -= resolveStrokeModifier(ratio);
                break;

            case BURGER_HIDE:
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, .4f, .9f);
                } else {
                    transformRatio = transformRatio(ratio, 0, .6f);
                }
                startX = (1 - transformRatio) * startX + transformRatio * startX / 1.5f;
                stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                break;

            case ARROW_X:
                // rotate from ARROW angle to X angle
                rotation = ARROW_TOP_LINE_ANGLE + (360 + X_BOT_LINE_ANGLE - ARROW_TOP_LINE_ANGLE) * ratio;
                rotation2 = -X_ROTATION_ANGLE * ratio;
                // move pivot from ARROW pivot to X pivot
                pivotX = mWidth / 2 + (mSidePadding + mDip4 - mWidth / 2) * ratio;
                pivotY = mHeight / 2 + (mHeight / 2 - mTopPadding - mDip3) * ratio;
                // lengthen both ends
                stopX -= resolveStrokeModifier(ratio);
                startX += mDip3;
                break;

            case ARROW_CHECK:
                // rotate from ARROW angle to CHECK angle
                rotation = ARROW_TOP_LINE_ANGLE + ratio * CHECK_BOTTOM_ANGLE;
                // move pivot from ARROW pivot to CHECK pivot
                pivotX = mWidth / 2 + mDip3 * ratio;
                pivotY = mHeight / 2 - mDip3 * ratio;
                // length stays same as ARROW
                stopX -= resolveStrokeModifier(1);
                startX += mDip3 + (mDip4 + mDip1) * ratio;
                break;

            case ARROW_HIDE:
                // rotate to required angle
                rotation = ARROW_TOP_LINE_ANGLE;
                // pivot center of canvas
                pivotX = mWidth / 2;
                pivotY = mHeight / 2;
                // slide
                float slide = isMorphingForward() ? 0 : transformRatio(ratio, .1f, 1) * mSidePadding / 8;
                startY -= slide;
                stopY -= slide;
                // shorten left ends
                stopX = mWidth - mSidePadding - resolveStrokeModifier(1) - slide;
                // shorten right ends
                transformRatio = transformRatio(ratio,
                        isMorphingForward() ? .3f : .1f,
                        isMorphingForward() ? .8f : .6f);
                startX = (1 - transformRatio) * (mSidePadding - slide + mDip3) + transformRatio * (stopX + mDip2);
                if (startX > stopX) {
                    startX = stopX;
                }
                break;

            case X_CHECK:
                // rotate from X to CHECK angles
                rotation2 = -X_ROTATION_ANGLE * (1 - ratio);
                rotation = X_BOT_LINE_ANGLE + (CHECK_BOTTOM_ANGLE + ARROW_TOP_LINE_ANGLE - X_BOT_LINE_ANGLE) * ratio;
                // move pivot from X to CHECK
                pivotX = mSidePadding + mDip4 + (mWidth / 2 + mDip3 - mSidePadding - mDip4) * ratio;
                pivotY = mHeight - mTopPadding - mDip3 + (mTopPadding + mHeight / 2 - mHeight) * ratio;
                // shorten both ends
                startX += mDip8 - (mDip4 + mDip1) * (1 - ratio);
                stopX -= resolveStrokeModifier(1 - ratio);
                break;

            case X_HIDE:
                // rotate to required angles
                rotation = X_BOT_LINE_ANGLE;
                rotation2 = -X_ROTATION_ANGLE;
                // pivot left corner of line
                pivotX = mSidePadding + mDip4;
                pivotY = mHeight - mTopPadding - mDip3;
                // shorten one end
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, 0, .6f);
                } else {
                    transformRatio = transformRatio(ratio, .3f, 1);
                }
                if (isMorphingForward()) {
                    startX += mDip3;
                    stopX = (1 - transformRatio) * stopX + transformRatio * startX;
                } else {
                    startX = (1 - transformRatio) * (startX + mDip3) + transformRatio * stopX;
                }
                break;

            case CHECK_HIDE:
                // rotate to required angle
                rotation = CHECK_BOTTOM_ANGLE + ARROW_TOP_LINE_ANGLE;
                // move pivot from BURGER pivot to CHECK pivot
                pivotX = mWidth / 2 + mDip3;
                pivotY = mHeight / 2 - mDip3;
                // change length
                if (isMorphingForward()) {
                    transformRatio = transformRatio(ratio, 0, .3f);
                    stopX -= resolveStrokeModifier(1) + mDip2;
                    startX = (1 - transformRatio) * (startX + mDip8) + transformRatio * stopX;
                } else {
                    transformRatio = transformRatio(ratio, .7f, 1);
                    startX += mDip8;
                    stopX = (1 - transformRatio) * (stopX - resolveStrokeModifier(1)) + transformRatio * startX;
                }
                break;
        }

        mIconPaint.setAlpha(alpha);
        canvas.rotate(rotation, pivotX, pivotY);
        canvas.rotate(rotation2, pivotX2, pivotY2);
        canvas.drawLine(startX, startY, stopX, stopY, mIconPaint);
        mIconPaint.setAlpha(255);
    }

    private boolean isMorphingForward() {
        return mTransformationValue <= TRANSFORMATION_MID;
    }

    private float resolveStrokeModifier(float ratio) {
        switch (mStroke) {
            case BOLD:
                if (mAnimationState == AnimationState.ARROW_X || mAnimationState == AnimationState.X_CHECK) {
                    return mDip3 - (mDip3 * ratio);
                }
                return ratio * mDip3;
            case REGULAR:
                if (mAnimationState == AnimationState.ARROW_X || mAnimationState == AnimationState.X_CHECK) {
                    return mDip3 + mDipH - (mDip3 + mDipH) * ratio;
                }
                return ratio * (mDip3 + mDipH);
            case THIN:
                if (mAnimationState == AnimationState.ARROW_X || mAnimationState == AnimationState.X_CHECK) {
                    return mDip4 - ((mDip3 + mDip1) * ratio);
                }
                return ratio * mDip4;
        }
        return 0;
    }

    /**
     * Translate ratio to specific start and end points
     *
     * @param ratio      current ratio
     * @param startPoint start point [0, 1)
     * @param endPoint   end point (0, 1]
     * @return Translated current ratio value depends on start and end points
     */
    private float transformRatio(float ratio, float startPoint, float endPoint) {
        if (ratio <= startPoint) {
            return 0;
        } else if (ratio >= endPoint) {
            return 1;
        } else {
            return (ratio - startPoint) / (endPoint - startPoint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mIconPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mIconPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    /*
     * Accessor methods
     */

    public void setColor(int color) {
        mIconPaint.setColor(color);
        mCirclePaint.setColor(color);
        invalidateSelf();
    }

    public void setTransformationDuration(int duration) {
        mTransformation.setDuration(duration);
    }

    public void setInterpolator(Interpolator interpolator) {
        mTransformation.setInterpolator(interpolator);
    }

    public void setAnimationListener(AnimatorListener listener) {
        if (mAnimatorListener != null) {
            mTransformation.removeListener(mAnimatorListener);
        }

        if (listener != null) {
            mTransformation.addListener(listener);
        }

        mAnimatorListener = listener;
    }

    public void setIconState(IconState iconState) {
        synchronized (mLock) {
            if (mTransformationRunning) {
                mTransformation.cancel();
                mTransformationRunning = false;
            }

            if (mCurrentIconState == iconState) return;

            switch (iconState) {
                case BURGER:
                    mAnimationState = AnimationState.BURGER_ARROW;
                    mTransformationValue = TRANSFORMATION_START;
                    break;
                case ARROW:
                    mAnimationState = AnimationState.BURGER_ARROW;
                    mTransformationValue = TRANSFORMATION_MID;
                    break;
                case X:
                    mAnimationState = AnimationState.BURGER_X;
                    mTransformationValue = TRANSFORMATION_MID;
                    break;
                case CHECK:
                    mAnimationState = AnimationState.BURGER_CHECK;
                    mTransformationValue = TRANSFORMATION_MID;
            }
            mCurrentIconState = iconState;
            invalidateSelf();
        }
    }

    public void animateIconState(IconState state) {
        synchronized (mLock) {
            if (mTransformationRunning) {
                mTransformation.end();
            }
            mAnimatingIconState = state;
            start();
        }
    }

    public IconState setTransformationOffset(AnimationState animationState, float offset) {
        if (offset < TRANSFORMATION_START || offset > TRANSFORMATION_END) {
            throw new IllegalArgumentException(
                    String.format("Value must be between %s and %s", TRANSFORMATION_START, TRANSFORMATION_END)
            );
        }

        mAnimationState = animationState;

        final boolean isFirstIcon = offset < TRANSFORMATION_MID || offset == TRANSFORMATION_END;

        mCurrentIconState = isFirstIcon ? animationState.getFirstState() : animationState.getSecondState();
        mAnimatingIconState = isFirstIcon ? animationState.getSecondState() : animationState.getFirstState();

        setTransformationValue(offset);

        return mCurrentIconState;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
        invalidateSelf();
    }

    public void setRTLEnabled(boolean rtlEnabled) {
        mRtlEnabled = rtlEnabled;
        invalidateSelf();
    }

    public IconState getIconState() {
        return mCurrentIconState;
    }

    public boolean isDrawableVisible() {
        return mVisible;
    }

    /*
     * Animations
     */
    private Property<MaterialMenuDrawable, Float> transformationProperty
            = new Property<MaterialMenuDrawable, Float>(Float.class, "transformation") {
        @Override
        public Float get(MaterialMenuDrawable object) {
            return object.getTransformationValue();
        }

        @Override
        public void set(MaterialMenuDrawable object, Float value) {
            object.setTransformationValue(value);
        }
    };

    public Float getTransformationValue() {
        return mTransformationValue;
    }

    public void setTransformationValue(Float value) {
        mTransformationValue = value;
        invalidateSelf();
    }

    private void initAnimations(int transformDuration) {
        mTransformation = ObjectAnimator.ofFloat(this, transformationProperty, 0);
        mTransformation.setInterpolator(new DecelerateInterpolator(3));
        mTransformation.setDuration(transformDuration);
        mTransformation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTransformationRunning = false;
                setIconState(mAnimatingIconState);
            }
        });
    }

    private boolean resolveTransformation() {
        boolean isCurrentBurger = mCurrentIconState == IconState.BURGER;
        boolean isCurrentArrow = mCurrentIconState == IconState.ARROW;
        boolean isCurrentX = mCurrentIconState == IconState.X;
        boolean isCurrentCheck = mCurrentIconState == IconState.CHECK;
        boolean isCurrentHide = mCurrentIconState == IconState.HIDE;
        boolean isAnimatingBurger = mAnimatingIconState == IconState.BURGER;
        boolean isAnimatingArrow = mAnimatingIconState == IconState.ARROW;
        boolean isAnimatingX = mAnimatingIconState == IconState.X;
        boolean isAnimatingCheck = mAnimatingIconState == IconState.CHECK;
        boolean isAnimatingHide = mAnimatingIconState == IconState.HIDE;

        if ((isCurrentBurger && isAnimatingArrow) || (isCurrentArrow && isAnimatingBurger)) {
            mAnimationState = AnimationState.BURGER_ARROW;
            return isCurrentBurger;
        }

        if ((isCurrentBurger && isAnimatingX) || (isCurrentX && isAnimatingBurger)) {
            mAnimationState = AnimationState.BURGER_X;
            return isCurrentBurger;
        }

        if ((isCurrentBurger && isAnimatingCheck) || (isCurrentCheck && isAnimatingBurger)) {
            mAnimationState = AnimationState.BURGER_CHECK;
            return isCurrentBurger;
        }

        if ((isCurrentBurger && isAnimatingHide) || (isCurrentHide && isAnimatingBurger)) {
            mAnimationState = AnimationState.BURGER_HIDE;
            return isCurrentBurger;
        }

        if ((isCurrentArrow && isAnimatingX) || (isCurrentX && isAnimatingArrow)) {
            mAnimationState = AnimationState.ARROW_X;
            return isCurrentArrow;
        }

        if ((isCurrentArrow && isAnimatingCheck) || (isCurrentCheck && isAnimatingArrow)) {
            mAnimationState = AnimationState.ARROW_CHECK;
            return isCurrentArrow;
        }

        if ((isCurrentArrow && isAnimatingHide) || (isCurrentHide && isAnimatingArrow)) {
            mAnimationState = AnimationState.ARROW_HIDE;
            return isCurrentArrow;
        }

        if ((isCurrentX && isAnimatingCheck) || (isCurrentCheck && isAnimatingX)) {
            mAnimationState = AnimationState.X_CHECK;
            return isCurrentX;
        }

        if ((isCurrentX && isAnimatingHide) || (isCurrentHide && isAnimatingX)) {
            mAnimationState = AnimationState.X_HIDE;
            return isCurrentX;
        }

        if ((isCurrentCheck && isAnimatingHide) || (isCurrentHide && isAnimatingCheck)) {
            mAnimationState = AnimationState.CHECK_HIDE;
            return isCurrentCheck;
        }

        throw new IllegalStateException(
                String.format("Animating from %s to %s is not supported", mCurrentIconState, mAnimatingIconState)
        );
    }

    @Override
    public void start() {
        if (mTransformationRunning) return;

        if (mAnimatingIconState != null && mAnimatingIconState != mCurrentIconState) {
            mTransformationRunning = true;

            final boolean direction = resolveTransformation();
            mTransformation.setFloatValues(
                    direction ? TRANSFORMATION_START : TRANSFORMATION_MID,
                    direction ? TRANSFORMATION_MID : TRANSFORMATION_END
            );
            mTransformation.start();
        }

        invalidateSelf();
    }

    @Override
    public void stop() {
        if (isRunning() && mTransformation.isRunning()) {
            mTransformation.end();
        } else {
            mTransformationRunning = false;
            invalidateSelf();
        }
    }

    @Override
    public boolean isRunning() {
        return mTransformationRunning;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public ConstantState getConstantState() {
        mMaterialMenuState.changingConfigurations = getChangingConfigurations();
        return mMaterialMenuState;
    }

    @Override
    public Drawable mutate() {
        mMaterialMenuState = new MaterialMenuState();
        return this;
    }

    private final class MaterialMenuState extends ConstantState {
        private int changingConfigurations;

        private MaterialMenuState() {
        }

        @Override
        public Drawable newDrawable() {
            MaterialMenuDrawable drawable = new MaterialMenuDrawable(
                    mCirclePaint.getColor(), mStroke, mTransformation.getDuration(),
                    mWidth, mHeight, mIconWidth, mCircleRadius, mStrokeWidth, mDip1
            );
            drawable.setIconState(mAnimatingIconState != null ? mAnimatingIconState : mCurrentIconState);
            drawable.setVisible(mVisible);
            drawable.setRTLEnabled(mRtlEnabled);
            return drawable;
        }

        @Override
        public int getChangingConfigurations() {
            return changingConfigurations;
        }
    }

    static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
