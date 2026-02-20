package common.lib.utils;

import java.util.HashMap;
import java.util.StringTokenizer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class CTextUtils {
	/**
	 * Text의 search에 해당하는 Text에 대해 highlight효과를 준다.
	 * 
	 * @param search highlight할 문자
	 * @param originalText 원본 문자
	 */
	public static SpannableStringBuilder highlight(String search, String originalText) {
		int start = originalText.indexOf(search);
		if (start < 0) {
			return new SpannableStringBuilder(originalText);
		} else {
			SpannableStringBuilder highlighted = new SpannableStringBuilder(originalText);
			while (start >= 0) {
				int spanStart = Math.min(start, originalText.length());
				int spanEnd = Math.min(start + search.length(), originalText.length());
				
				highlighted.setSpan(new ForegroundColorSpan(Color.BLUE), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				start = originalText.indexOf(search, spanEnd);
			}
			
			return highlighted;
		}
	}
	
	/**
	 * Text의 길이를 측정한다.
	 * 
	 * @param text 측정할 text
	 * @param paint text가 표현되는 정보가 담긴 Paint
	 * @return text의 width pixel
	 */
	public static int textWidth(String text, TextPaint paint) {
		StringTokenizer tokens = new StringTokenizer(text, "\n");
		int maxWidth = 0;
		for (int loop = 1; tokens.hasMoreElements(); loop++) {
			int textWidth = 0;
			String element = tokens.nextToken();
			float[] widths = new float[element.length()];
			paint.getTextWidths(element, widths);
			for (int i = 0; i < element.length(); i++) {
				textWidth += widths[i];
			}
			if (maxWidth < textWidth)
				maxWidth = textWidth;
		}
		return maxWidth;
	}
	
	private static HashMap<String,Typeface> mFonts = null;
	public static void initFonts(Context ctx, String path) {
		if (mFonts != null) {
			mFonts = new HashMap<String,Typeface>();
		}
		Typeface font = Typeface.createFromAsset(ctx.getAssets(), path);
		mFonts.put(path, font);
	}
	
	public static void setTextFont(Context ctx, String path, TextView v) {
		if (mFonts != null) {
			mFonts = new HashMap<String,Typeface>();
		}
		Typeface font = mFonts.get(path);
		if (font == null) {
			font = Typeface.createFromAsset(ctx.getAssets(), path);
			mFonts.put(path, font);
		}
		v.setTypeface(font);
	}
}
