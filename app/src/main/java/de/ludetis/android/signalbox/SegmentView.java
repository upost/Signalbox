package de.ludetis.android.signalbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import de.ludetis.android.signalbox.model.Segment;

public class SegmentView extends View {

    private static final float STROKE_WIDTH = 5f;
	private static final float BORDER_WIDTH = 1f;
    public static final float FONT_SIZE = 10f;
    private static final float FONT_SIZE_LARGE = 15f;
    private Paint white = new Paint();
    private Paint greyStroke = new Paint();
    private Paint greyFill = new Paint();
	private Paint black = new Paint();
    private Paint blackFill = new Paint();
    private Paint red = new Paint();
    private Paint redFill = new Paint();

    private Paint fontPaintLarge = new Paint();
	private Paint green = new Paint();
    private Paint greenFilled = new Paint();
    private Paint orangeFilled = new Paint();
	private float scale;
	private Segment segment;

	
	public SegmentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		commonConstructor(context);
	}
	
	public SegmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		commonConstructor(context);
	}
	
	public SegmentView(Context context) {
		super(context);
		commonConstructor(context);
	}

	private void commonConstructor(Context context) {
		scale = context.getResources().getDisplayMetrics().density;
		
		white.setARGB(255,255,255,255);
		white.setStyle(Style.FILL);

        black.setARGB(255,0,0,0);
        black.setStyle(Style.STROKE);
        black.setStrokeWidth(STROKE_WIDTH*scale);
        black.setAntiAlias(true);

        blackFill.setARGB(255,0,0,0);
        blackFill.setStyle(Style.FILL_AND_STROKE);
        blackFill.setAntiAlias(true);

//        fontPaint.setColor(Color.BLACK);
//        fontPaint.setStyle(Style.FILL);
//        fontPaint.setTextSize(FONT_SIZE * scale);
//        fontPaint.setAntiAlias(true);
//        fontPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        fontPaintLarge.setColor(Color.BLACK);
        fontPaintLarge.setStyle(Style.FILL);
        fontPaintLarge.setTextSize(FONT_SIZE_LARGE * scale);
        fontPaintLarge.setAntiAlias(true);
        fontPaintLarge.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        green.setARGB(255,40,180,40);
        green.setStyle(Style.STROKE);
        green.setStrokeWidth(STROKE_WIDTH*scale);
        green.setAntiAlias(true);

        greenFilled.setARGB(255,40,180,40);
        greenFilled.setStyle(Style.FILL);
        greenFilled.setAntiAlias(true);

        orangeFilled.setARGB(255,240,170,40);
        orangeFilled.setStyle(Style.FILL);
        orangeFilled.setAntiAlias(true);

        red.setARGB(255,160,40,40);
        red.setStyle(Style.STROKE);
        red.setStrokeWidth(STROKE_WIDTH*scale);
        red.setAntiAlias(true);

        redFill.setARGB(125,240,100,100);
        redFill.setStyle(Style.FILL);
        redFill.setAntiAlias(true);

        greyStroke.setARGB(255, 180, 180, 180);
        greyStroke.setStyle(Style.STROKE);
        greyStroke.setStrokeWidth(BORDER_WIDTH * scale);

        greyFill.setARGB(255, 200, 200, 200);
        greyFill.setStyle(Style.FILL);


	}

	@Override
	protected void onDraw(Canvas canvas) {

		float w = canvas.getWidth();
		float h = canvas.getHeight(); 
		
		canvas.drawPaint(white);

        if(segment!=null) {

            int state = segment.getState();

            if(segment.isRoutePoint()) {
                canvas.drawCircle(w/2,h/2, h/4, redFill);
            }

            switch (segment.getType()) {
                case STRAIGHT_PLATFORM_ABOVE:
                case STRAIGHT_PLATFORM_BELOW:
                case STRAIGHT_MARKER:
                case SEMAPHORE_BOTTOM:
                case SEMAPHORE_TOP:
                case SEMAPHORE3_BOTTOM:
                case SEMAPHORE3_TOP:
                case STRAIGHT:
                case STRAIGHT_ROUTE_MARKER:
                    canvas.drawLine(0, h / 2, w - 1, h / 2, black);
                    break;
                case CURVE_UP:
                    canvas.drawLine(0, h / 2, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, 0, black);
                    break;
                case CURVE_DOWN:
                    canvas.drawLine(0, h / 2, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, h - 1, black);
                    break;
                case UP_CURVE:
                    canvas.drawLine(0, h - 1, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, black);
                    break;
                case DOWN_CURVE:
                    canvas.drawLine(0, 0, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, black);
                    break;
                case SWITCH_UP:
                    canvas.drawLine(0, h / 2, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, 0, (state == 0 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, (state == 1 ? black : green));
                    break;
                case SWITCH_DOWN:
                    canvas.drawLine(0, h / 2, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, h - 1, (state == 0 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, (state == 1 ? black : green));
                    break;
                case UP_SWITCH:
                    canvas.drawLine(0, h - 1, w / 2, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(0, h / 2, w / 2, h / 2, (state == 1 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, black);
                    break;
                case DOWN_SWITCH:
                    canvas.drawLine(0, 0, w / 2, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(0, h / 2, w / 2, h / 2, (state == 1 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, black);
                    break;
                case SWITCHS_DOWN:
                    canvas.drawLine(0, 0, w / 2, h / 2, (state == 1 ? black : green));
                    canvas.drawLine(0, h / 2, w / 2, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h - 1, black);
                    break;
                case SWITCHS_UP:
                    canvas.drawLine(w / 2, h / 2, w - 1, h - 1, (state == 1 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(0, 0, w / 2, h / 2, black);
                    break;
                case DOWN_SWITCHS:
                    canvas.drawLine(w / 2, h / 2, w - 1, 0, (state == 1 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(0, h - 1, w / 2, h / 2, black);
                    break;
                case UP_SWITCHS:
                    canvas.drawLine(0, h - 1, w / 2, h / 2, (state == 1 ? black : green));
                    canvas.drawLine(0, h / 2, w / 2, h / 2, (state == 0 ? black : green));
                    canvas.drawLine(w / 2, h / 2, w - 1, 0, black);
                    break;
                case ACROSS_UP:
                case UP_ROUTE_MARKER:
                    canvas.drawLine(0, h - 1, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, 0, black);
                    break;
                case ACROSS_DOWN:
                case DOWN_ROUTE_MARKER:
                    canvas.drawLine(0, 0, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 2, w - 1, h - 1, black);
                    break;
                case BUMPER_LEFT:
                    canvas.drawLine(w / 2, h / 2, w - 1, h / 2, black);
                    canvas.drawLine(w / 2, h / 3, w / 2, 2 * h / 3, black);
                    break;
                case BUMPER_RIGHT:
                    canvas.drawLine(0, h / 2, w / 2, h / 2, black);
                    canvas.drawLine(w / 2, h / 3, w / 2, 2 * h / 3, black);
                    break;
            }

            // marker
            if (segment.isEditable()) {
                canvas.drawText(segment.getId(), 0, h / 3, fontPaintLarge);
            }

            // platform
            if (segment.getType().equals(Segment.Type.STRAIGHT_PLATFORM_ABOVE)) {
                canvas.drawRect(0, 0, w - 1, h / 3, greyFill);
            }
            if (segment.getType().equals(Segment.Type.STRAIGHT_PLATFORM_BELOW)) {
                canvas.drawRect(0, 2 * h / 3, w - 1, h - 1, greyFill);
            }

            // switch id
            if (segment.isSwitch()) {
                String display = "";
                if (!TextUtils.isEmpty(segment.getId())) {
                    display = segment.getId();
                } else {
                    if (segment.getBus() > 0 && segment.getAddress() > 0)
                        display = segment.getBus() + ":" + segment.getAddress();
                }
                canvas.drawText(display, w / 2, h / 4, fontPaintLarge);
            }

            // semaphore
            if (segment.isSemaphore()) {
                float dh = h/4 + (segment.getType()== Segment.Type.SEMAPHORE_TOP|| segment.getType()== Segment.Type.SEMAPHORE3_TOP ? 0 : h/2);
                float sl = w/4;
                if(segment.getType()== Segment.Type.SEMAPHORE3_TOP || segment.getType()== Segment.Type.SEMAPHORE3_BOTTOM) sl=w/3;
                float sh = h/6;
                canvas.drawRect(w/2-sl/2, dh-sh, w/2+sl/2, dh+sh, blackFill );
                if(segment.getType()== Segment.Type.SEMAPHORE_TOP || segment.getType()== Segment.Type.SEMAPHORE_BOTTOM) {
                    canvas.drawCircle(w / 2 - sl / 4, dh, sh / 4, segment.getState() == 0 ? red : greyFill);
                    canvas.drawCircle(w / 2 + sl / 4, dh, sh / 4, segment.getState() == 1 ? greenFilled : greyFill);
                } else if(segment.getType()== Segment.Type.SEMAPHORE3_TOP || segment.getType()== Segment.Type.SEMAPHORE3_BOTTOM) {
                    canvas.drawCircle(w / 2 - sl / 3, dh, sh / 4, segment.getState() == 0 ? red : greyFill);
                    canvas.drawCircle(w / 2         , dh, sh / 4, segment.getState() == 2 ? orangeFilled : greyFill);
                    canvas.drawCircle(w / 2 + sl / 3, dh, sh / 4, segment.getState() >= 1 ? greenFilled : greyFill);
                }

                String display = "";
                if (!TextUtils.isEmpty(segment.getId())) {
                    display = segment.getId();
                } else {
                    if (segment.getBus() > 0 && segment.getAddress() > 0)
                        display = segment.getBus() + ":" + segment.getAddress();
                }
                canvas.drawText(display, w / 2, h-dh, fontPaintLarge);
            }

            // generic accessory
            if(segment.isGenericAccessory()) {
                float dh = h/2;
                float sl = h/6; float sh = h/6;
                canvas.drawRect(w/2-sl, dh-sh, w/2+sl, dh+sh, blackFill );
                canvas.drawCircle(w/2, dh, sh/2, segment.getState()==0 ? greyFill : greenFilled);

                String display = "";
                if (!TextUtils.isEmpty(segment.getId())) {
                    display = segment.getId();
                } else {
                    if (segment.getBus() > 0 && segment.getAddress() > 0)
                        display = segment.getBus() + ":" + segment.getAddress();
                }
                canvas.drawText(display, w / 2, h/4, fontPaintLarge);
            }

            // generic function
            if(segment.isGenericFunction()) {
                float dh = h/4;
                float dw = h/6;

                canvas.drawRect(dw, dh, w-dw, h-dh, blackFill );
                float cd = (w-dw*2)/Segment.FUNCTION_DECODER_FUNCTIONS;
                float r = h/12;

                for(int i=0; i<Segment.FUNCTION_DECODER_FUNCTIONS; i++) {
                    canvas.drawCircle(dw+cd*i+r+cd/8, h/2, r, segment.getFunctionValue(i) ?  greenFilled : greyFill);
                }

                String display = "";
                if (!TextUtils.isEmpty(segment.getId())) {
                    display = segment.getId();
                } else {
                    if (segment.getBus() > 0 && segment.getAddress() > 0)
                        display = segment.getBus() + ":" + segment.getAddress();
                }
                canvas.drawText(display, dw, h/6, fontPaintLarge);
            }

        }

        canvas.drawRect(0, 0, w-1,h-1, greyStroke);

	}

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
        invalidate();
    }
}
