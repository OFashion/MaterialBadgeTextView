package com.matrixxun.starry.badgetextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

/** Created by matrixxun on 2016/8/30. */
public class MaterialBadgeTextView extends AppCompatTextView {

  private static final int DEFAULT_FILL_TYPE = 0;
  private static final float SHADOW_RADIUS = 3.5f;
  private static final int FILL_SHADOW_COLOR = 0x55000000;
  private static final int KEY_SHADOW_COLOR = 0x55000000;
  private static final float X_OFFSET = 0f;
  private static final float Y_OFFSET = 1.75f;
  public static final String TAG = "MaterialBadgeTextView";
  private int backgroundColor;
  private int borderColor;
  private float borderWidth;
  private float borderAlpha;
  private int ctType;
  private float density;
  private int mShadowRadius;
  private int shadowYOffset;
  private int shadowXOffset;

  private int basePadding;
  private int diffWH;

  private boolean isHighLightMode;

  public MaterialBadgeTextView(final Context context) {
    this(context, null);
  }

  public MaterialBadgeTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialBadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public static int dp2px(Context context, float dpValue) {
    try {
      final float scale = context.getResources().getDisplayMetrics().density;
      return (int) (dpValue * scale + 0.5f);
    } catch (Exception e) {
      return (int) (dpValue + 0.5f);
    }
  }

  private void init(Context context, AttributeSet attrs) {
    setGravity(Gravity.CENTER);
    density = getContext().getResources().getDisplayMetrics().density;
    mShadowRadius = (int) (density * SHADOW_RADIUS);
    shadowYOffset = (int) (density * Y_OFFSET);
    shadowXOffset = (int) (density * X_OFFSET);
    basePadding = (mShadowRadius * 2);
    float textHeight = getTextSize();
    float textWidth = textHeight / 4;
    diffWH = (int) (Math.abs(textHeight - textWidth) / 2);
    int horizontalPadding = basePadding + diffWH;
    setPadding(horizontalPadding, basePadding, horizontalPadding, basePadding);
    TypedArray typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialBadgeTextView);
    backgroundColor =
        typedArray.getColor(R.styleable.MaterialBadgeTextView_android_background, Color.WHITE);
    borderColor =
        typedArray.getColor(R.styleable.MaterialBadgeTextView_mbtv_border_color, Color.TRANSPARENT);
    borderWidth = typedArray.getDimension(R.styleable.MaterialBadgeTextView_mbtv_border_width, 0);
    borderAlpha = typedArray.getFloat(R.styleable.MaterialBadgeTextView_mbtv_border_alpha, 1);
    ctType = typedArray.getInt(R.styleable.MaterialBadgeTextView_mbtv_type, DEFAULT_FILL_TYPE);
    typedArray.recycle();
  }

  @Override
  protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    super.onTextChanged(text, start, lengthBefore, lengthAfter);
    /** 纯色小红点模式下若有文本需要从无变为有, 要归位view的大小. */
    String strText = text == null ? "" : text.toString().trim();
    if (isHighLightMode && !"".equals(strText)) {
      resetLayoutParam();
      isHighLightMode = false;
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    refreshBackgroundDrawable(w, h);
  }

  private void refreshBackgroundDrawable(int targetWidth, int targetHeight) {
    if (targetWidth <= 0 || targetHeight <= 0) {
      return;
    }
    CharSequence text = getText();
    if (text == null) {
      return;
    }
    if (text.length() == 1) {
      /** 第一种背景是一个正圆形, 当文本为个位数字时 */
      int max = Math.max(targetWidth, targetHeight);
      ShapeDrawable circle;
      final int diameter = max - (2 * mShadowRadius);
      OvalShape oval = new OvalShadow(mShadowRadius, diameter);
      circle = new ShapeDrawable(oval);
      this.setLayerType(View.LAYER_TYPE_SOFTWARE, circle.getPaint());
      circle
          .getPaint()
          .setShadowLayer(mShadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
      circle.getPaint().setColor(backgroundColor);
      setBackground(circle);
    } else if (text.length() > 1) {
      /** 第二种背景是上下两边为直线的椭圆, 当文本长度大于1时 */
      SemiCircleRectDrawable sr = new SemiCircleRectDrawable();
      setLayerType(View.LAYER_TYPE_SOFTWARE, sr.getPaint());
      sr.getPaint().setShadowLayer(mShadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
      sr.getPaint().setColor(backgroundColor);
      setBackground(sr);
    }
  }

  public void setBadgeCount(String count) {
    setBadgeCount(count, false);
  }

  public void setBadgeCount(String count, boolean goneWhenZero) {
    int temp = -1;
    try {
      temp = Integer.parseInt(count);
    } catch (Exception e) {
      Log.e(TAG, "setBadgeCount", e);
    }
    if (temp != -1) {
      setBadgeCount(temp, goneWhenZero);
    }
  }

  public void setBadgeCount(int count) {
    setBadgeCount(count, true);
  }

  @SuppressLint("SetTextI18n")
  public void setBadgeCount(int count, boolean goneWhenZero) {
    if (count > 0 && count <= 99) {
      setText(String.valueOf(count));
      setVisibility(View.VISIBLE);
    } else if (count > 99) {
      setText("99+");
      setVisibility(View.VISIBLE);
    } else {
      setText("0");
      if (goneWhenZero) {
        setVisibility(View.GONE);
      } else {
        setVisibility(View.VISIBLE);
      }
    }
  }

  /** 明确的展现一个无任何文本的红色圆点, 主要是通过设置文本setText("")触发onTextChanged(), 再连锁触发onSizeChanged()最后更新了背景. */
  public void setHighLightMode() {
    setHighLightMode(false);
  }

  public void clearHighLightMode() {
    isHighLightMode = false;
    setBadgeCount(0);
  }

  /** @param isDisplayInToolbarMenu */
  public void setHighLightMode(boolean isDisplayInToolbarMenu) {
    isHighLightMode = true;
    updateLayoutParam(isDisplayInToolbarMenu);
    ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
    setLayerType(View.LAYER_TYPE_SOFTWARE, drawable.getPaint());
    drawable.getPaint().setColor(backgroundColor);
    drawable.getPaint().setAntiAlias(true);
    setBackground(drawable);
    setText("");
    setVisibility(View.VISIBLE);
  }

  private void updateLayoutParam(boolean isDisplayInToolbarMenu) {
    ViewGroup.LayoutParams params = getLayoutParams();
    params.width = dp2px(getContext(), 8);
    params.height = params.width;
    if (isDisplayInToolbarMenu && params instanceof FrameLayout.LayoutParams) {
      ((FrameLayout.LayoutParams) params).topMargin = dp2px(getContext(), 8);
      ((FrameLayout.LayoutParams) params).rightMargin = dp2px(getContext(), 8);
    }
    setLayoutParams(params);
  }

  private void resetLayoutParam() {
    ViewGroup.LayoutParams param = getLayoutParams();
    if (param instanceof FrameLayout.LayoutParams) {
      FrameLayout.LayoutParams lp = (LayoutParams) param;
      lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
      lp.topMargin = 0;
      lp.rightMargin = 0;
      setLayoutParams(lp);
    }
  }

  public void setBackgroundColor(int color) {
    backgroundColor = color;
    refreshBackgroundDrawable(getWidth(), getHeight());
  }

  private class OvalShadow extends OvalShape {
    private RadialGradient mRadialGradient;
    private Paint mShadowPaint;
    private int mCircleDiameter;

    public OvalShadow(int shadowRadius, int circleDiameter) {
      super();
      mShadowPaint = new Paint();
      mShadowRadius = shadowRadius;
      mCircleDiameter = circleDiameter;
      mRadialGradient =
          new RadialGradient(
              mCircleDiameter / 2,
              mCircleDiameter / 2,
              mShadowRadius,
              new int[] {FILL_SHADOW_COLOR, Color.TRANSPARENT},
              null,
              Shader.TileMode.CLAMP);
      mShadowPaint.setShader(mRadialGradient);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      final int viewWidth = MaterialBadgeTextView.this.getWidth();
      final int viewHeight = MaterialBadgeTextView.this.getHeight();
      canvas.drawCircle(
          viewWidth / 2, viewHeight / 2, (mCircleDiameter / 2 + mShadowRadius), mShadowPaint);
      canvas.drawCircle(viewWidth / 2, viewHeight / 2, (mCircleDiameter / 2), paint);
    }
  }

  class SemiCircleRectDrawable extends Drawable {
    private final Paint mPaint;
    private RectF rectF;

    public SemiCircleRectDrawable() {
      mPaint = new Paint();
      mPaint.setAntiAlias(true);
    }

    public Paint getPaint() {
      return mPaint;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
      super.setBounds(left, top, right, bottom);
      if (rectF == null) {
        rectF =
            new RectF(
                left + diffWH, top + mShadowRadius + 4, right - diffWH, bottom - mShadowRadius - 4);
      } else {
        rectF.set(
            left + diffWH, top + mShadowRadius + 4, right - diffWH, bottom - mShadowRadius - 4);
      }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      float R = (float) (rectF.bottom * 0.4);
      if (rectF.right < rectF.bottom) {
        R = (float) (rectF.right * 0.4);
      }
      canvas.drawRoundRect(rectF, R, R, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
      mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
      mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSPARENT;
    }
  }
}
