package com.hbb20;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hbb20.gthumbnaillibrary.R;
import com.squareup.picasso.Picasso;

/**
 * Created by hbb20 on 18/4/16.
 */
public class GThumb extends RelativeLayout {

    public static String SHAPE_ROUND = "0";
    public static String SHAPE_SQUARE = "1";
    private static String TAG = "GThumb :";
    Context context;
    View rootView;
    RelativeLayout relativeForeground, relativeHolder;
    TextView textViewInitials;
    ImageView imageViewRealImage, imageViewColorBg;
    String bgShape, textStyle = null;
    boolean flagBoldText = false;
    int blockSize = -1;
    int maxLength;
    int monoBGColor, monoTextColor;
    boolean flagMonoColor;
    private int bgColorEntropy;

    public GThumb(Context context) {
        super(context);
        this.context = context;
    }


    public GThumb(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public GThumb(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        log("Initialization");
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_user_thumb, this, true);
        relativeForeground = (RelativeLayout) rootView.findViewById(R.id.relative_foreground);
        relativeHolder = (RelativeLayout) rootView.findViewById(R.id.relative_holder);
        textViewInitials = (TextView) rootView.findViewById(R.id.textView_initials);
        imageViewRealImage = (ImageView) rootView.findViewById(R.id.imageView_real);
        imageViewColorBg = (ImageView) rootView.findViewById(R.id.imageView_color_bg);
        applyCustomAttributes(attrs);
    }

    private void applyCustomAttributes(AttributeSet attrs) {
        log("Applying custom attribute");
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GThumb, 0, 0);
        try {

            //custom text size
            int textSize = a.getDimensionPixelSize(R.styleable.GThumb_textSize, Math.round(24 * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)));
            setTextSize(textSize);

            //colors
            monoBGColor = a.getColor(R.styleable.GThumb_monoBGColor, -1);
            monoTextColor = a.getColor(R.styleable.GThumb_monoTextColor, -1);
            if (monoBGColor != -1 || monoTextColor != -1) {
                flagMonoColor = true;
            }

            if (monoBGColor == -1) {
                monoBGColor = context.getResources().getColor(R.color.google_deepPurple_500);
            }

            if (monoTextColor == -1) {
                monoTextColor = getContrastGrayColor(monoBGColor);
            }
            setColors();

            //preview text
            String preview = a.getString(R.styleable.GThumb_previewText);
            if (preview == null || preview.length() == 0) {
                preview = "hb";
            }
            setAsInitialText(preview);

            //set bg shape
            bgShape = a.getString(R.styleable.GThumb_backgroundShape);
            applyBackgroundShape();

            //text style (bold/normal)
            flagBoldText = a.getBoolean(R.styleable.GThumb_boldText, false);
            setBoldText(flagBoldText);

        } catch (Exception ex) {
            ex.printStackTrace();
            log("Error in applying custom attributes");
        }
    }

    private void applyBackgroundShape() {
        if (bgShape == null) {
            bgShape = SHAPE_ROUND;
        }

        if (bgShape.equals(SHAPE_SQUARE)) {
            imageViewColorBg.setImageDrawable(context.getResources().getDrawable(R.drawable.square_white));
        } else {
            imageViewColorBg.setImageDrawable(context.getResources().getDrawable(R.drawable.oval_white));
        }
    }

    private void setAsInitialText(String initials) {
        if (initials == null || initials.trim().length() == 0) {
            initials="";
        }else {
            initials = initials.trim();
            if(initials.length()>2){
                initials=initials.substring(0,2);
            }
            initials = initials.trim().toUpperCase();
        }
        textViewInitials.setText(initials);
    }

    private void setTextColor(int color) {
        textViewInitials.setTextColor(color);
    }

    private void setThumbBackground(int color) {
        imageViewColorBg.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    private int getFitSize() {
        int minDim = getWidth();
        if (minDim > getHeight()) {
            minDim = getHeight();
        }
        textViewInitials.setText("" + minDim);
        return minDim == 0 ? 25 : minDim;
    }

    /**
     * This will update the size of text
     *
     * @param textSize size of text in pixel
     */
    public void setTextSize(int textSize) {
        textViewInitials.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private int getContrastGrayColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int avg = (blue + green + red) / 3;
        int gray = 128;
        if (avg >= 128) {
            gray = 64 - (avg / 4);
        } else {
            gray = 192 + (avg / 2);
        }
        textViewInitials.setText("" + gray);
        return Color.rgb(gray, gray, gray);
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    public void setBoldText(boolean boldText) {
        this.flagBoldText = boldText;
        if (isInEditMode()) {
            if (boldText) {
                textViewInitials.setTypeface(textViewInitials.getTypeface(), Typeface.BOLD);
            } else {
                textViewInitials.setTypeface(textViewInitials.getTypeface(), Typeface.NORMAL);
            }
        } else {
            if (boldText) {
                textViewInitials.setTypeface(null, Typeface.BOLD);
            } else {
                textViewInitials.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void loadImage(String imageURL) {
        if (URLUtil.isValidUrl(imageURL)) {
            relativeForeground.setVisibility(View.VISIBLE);
            if (bgShape.equals(SHAPE_SQUARE)) {
                Picasso.with(context).load(imageURL).into(imageViewRealImage);
            } else {
                Picasso.with(context).load(imageURL).transform(new CircleTransform()).into(imageViewRealImage);
            }
        } else { //if URL is not valid
            relativeForeground.setVisibility(GONE);
        }
    }

    /*Public APIs*/
    private void loadThumbForInitials(String imageURL, String initials, int colorEntropy) {
        this.bgColorEntropy = colorEntropy;
        setAsInitialText(initials);
        setColors();
        loadImage(imageURL);
    }

    private void setColors() {
        if (flagMonoColor) {
            setThumbBackground(monoBGColor);
            setTextColor(monoTextColor);
        } else {
            GThumbSwatch gThumbSwatch = GThumbSwatch.getGThumbSwatchForEntropy(bgColorEntropy);
            setThumbBackground(gThumbSwatch.colorBG);
            setTextColor(gThumbSwatch.colorText);
        }
    }

    public void loadThumbForName(String imageURL, String firstName) {
        String initials = "";
        if (firstName != null && firstName.trim().length() >= 1) {
            firstName = firstName.trim();
            initials = firstName.charAt(0) + "";
        }
        loadThumbForInitials(initials, imageURL, getEntropy(imageURL + firstName));
    }

    private int getEntropy(String s) {
        return s.length();
    }

    public void loadThumbForName(String imageURL, String firstName, String secondName) {
        if (secondName == null || secondName.trim().length() == 0) {
            firstName = firstName.trim();
            loadThumbForName(imageURL, firstName);
            return;
        }


        if (firstName == null || firstName.trim().length() == 0) {
            secondName = secondName.trim();
            loadThumbForName(imageURL, secondName);
            return;
        }

        String initials = firstName.substring(0, 1) + secondName.substring(0, 1);
        loadThumbForInitials(imageURL, initials, getEntropy(imageURL + firstName + secondName));
    }

    @Override
    public void setOnClickListener(View.OnClickListener clickListener) {
        if (clickListener == null) {
            relativeHolder.setEnabled(false);
            relativeHolder.setClickable(false);
            relativeHolder.setOnClickListener(null);
        } else {
            relativeHolder.setClickable(true);
            relativeHolder.setEnabled(true);
            relativeHolder.setOnClickListener(clickListener);
        }
    }

    public void setBackgroundShape(String shapeOption) {
        if (shapeOption.equals(SHAPE_ROUND) || shapeOption.equals(SHAPE_SQUARE)) {
            bgShape = shapeOption;
            applyBackgroundShape();
        }
    }

    public void applyMultiColor(boolean applyMultiColor) {
        if (applyMultiColor) {
            flagMonoColor = false;
        }
        setColors();
    }

    public void setMonoColor(int monoBackgroundColor) {
        setMonoColor(monoBackgroundColor, getContrastGrayColor(monoBackgroundColor));
    }

    public void setMonoColor(int monoBackgroundColor, int monoTextColor) {
        flagMonoColor = true;
        this.monoBGColor = monoBackgroundColor;
        this.monoTextColor = monoTextColor;
        setColors();
    }
}