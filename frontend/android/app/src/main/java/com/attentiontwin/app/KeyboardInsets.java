package com.attentiontwin.app;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

/** The native window owns keyboard space once; the WebView fills the remaining content bounds. */
final class KeyboardInsets {
    static void apply(View content,WindowInsetsCompat insets,boolean manualIme){Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());Insets ime=insets.getInsets(WindowInsetsCompat.Type.ime());boolean shown=insets.isVisible(WindowInsetsCompat.Type.ime())||ime.bottom>0;content.setPadding(bars.left,bars.top,bars.right,manualIme?Math.max(bars.bottom,ime.bottom):shown?0:bars.bottom);}
}
