package com.hotstar.player.fonts;

import android.content.Context;
import android.graphics.Typeface;

/** Loads & caches typefaces used by the various Font_____ classes. **/
public class FontFactory {

	static Typeface gibsonRegular;
	static Typeface gibsonMedium;
	static Typeface gibsonSemiBold;
	static Typeface gibsonBold;
	static Typeface gibsonLight;

	public static final int SYS_DEFAULT = 0;
	public static final int GIBSON_REGULAR = 1;
	public static final int GIBSON_MEDIUM = 2;
	public static final int GIBSON_SEMIBOLD = 3;
	public static final int GIBSON_BOLD = 4;
	public static final int GIBSON_LIGHT = 5;

	/**
	 * Creates and caches the typefaces used by the application
	 * 
	 * @param context
	 * @param fontEnum
	 * @return
	 */
	public static Typeface getTypeface(Context context, int fontEnum) {
		Typeface font;

		switch (fontEnum) {
		case GIBSON_REGULAR:
			if (gibsonRegular == null) {
				gibsonRegular = Typeface.createFromAsset(context.getAssets(), "fonts/gibson-regular.ttf");
			}
			font = gibsonRegular;
			break;
		case GIBSON_MEDIUM:
			if (gibsonMedium == null) {
				gibsonMedium = Typeface.createFromAsset(context.getAssets(), "fonts/gibson-regularitalic.ttf");
			}
			font = gibsonMedium;
			break;
		case GIBSON_SEMIBOLD:
			if (gibsonSemiBold == null) {
				gibsonSemiBold = Typeface.createFromAsset(context.getAssets(), "fonts/gibsonnarrow-bold.ttf");
			}
			font = gibsonSemiBold;
			break;
		case GIBSON_BOLD:
			if (gibsonBold == null) {
				gibsonBold = Typeface.createFromAsset(context.getAssets(), "fonts/gibson-bold.ttf");
			}
			font = gibsonBold;
			break;
		case GIBSON_LIGHT:
			if (gibsonLight == null) {
				gibsonLight = Typeface.createFromAsset(context.getAssets(), "fonts/gibsonlight-regular.ttf");
			}
			font = gibsonLight;
			break;
		default:
				font = null;
		}

		if (font == null) throw new RuntimeException("Error: you must copy any fonts used to the assets/fonts directory");
		return font;
	}

}
