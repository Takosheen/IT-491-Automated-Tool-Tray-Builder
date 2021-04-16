package com.jiangpeng.android.antrace;

import com.jiangpeng.android.antrace.Objects.ImageInteraction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class PreviewImageView extends androidx.appcompat.widget.AppCompatImageView {
    private ImageInteraction m_interaction = null;
    
    public void setInteraction(ImageInteraction interaction)
    {
    	m_interaction = interaction;
    }
    
    public ImageInteraction getInteraction()
    {
    	return m_interaction;
    }

    public void startCrop()
    {
    	m_interaction.startCrop();

    }
    
    public void endCrop()
    {
    	m_interaction.endCrop();

    }


  
    public PreviewImageView(Context context) {
        super(context);
        init(context);
    }
 
    public PreviewImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
 
    public PreviewImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

	public static DisplayMetrics getDisplayMetrics(Context context)
	{
	    DisplayMetrics metrics = new DisplayMetrics();
	    WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	    wm.getDefaultDisplay().getMetrics(metrics); 
	    
	    return metrics;
	}

    private void init(Context context)
    {
        super.setClickable(true);

    }
    
    public void init()
    {
        m_interaction.init();
        setOnTouchListener(m_touchListener);
    }
    
    public void setImage(Bitmap bm) { 
        super.setImageBitmap(bm);
        if(m_interaction != null)
        {
        	m_interaction.setBitmap(bm);
        }

    }



    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		m_interaction.draw(canvas);

	}

	private OnTouchListener m_touchListener = new OnTouchListener()
    {
		@Override
		public boolean onTouch(View v, MotionEvent rawEvent) {
			return m_interaction.onTouch(v, rawEvent);

		}
    };
    

}
