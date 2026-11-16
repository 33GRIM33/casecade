package com.attentiontwin.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import android.graphics.Color;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

public class MainActivity extends BridgeActivity {
    private boolean imeVisible;
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration){super.onConfigurationChanged(configuration);applyTheme(this);}
    static boolean isDark(android.content.Context context){String theme=context.getSharedPreferences("appearance",0).getString("theme","system");return theme.equals("dark")||theme.equals("system")&&(context.getResources().getConfiguration().uiMode&android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;}
    static void applyTheme(android.app.Activity activity){boolean dark=isDark(activity);int color=Color.parseColor(dark?"#14221D":"#F7F5ED");activity.getWindow().setStatusBarColor(color);activity.getWindow().setNavigationBarColor(color);activity.getWindow().getDecorView().setBackgroundColor(color);WindowInsetsControllerCompat controller=new WindowInsetsControllerCompat(activity.getWindow(),activity.getWindow().getDecorView());controller.setAppearanceLightStatusBars(!dark);controller.setAppearanceLightNavigationBars(!dark);}
    @Override protected void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);setIntent(intent);if(getBridge()!=null)getBridge().getWebView().post(()->getBridge().getWebView().evaluateJavascript("window.dispatchEvent(new Event('attention-route'))",null));}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FocusPlugin.class);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        boolean manualKeyboard=android.os.Build.VERSION.SDK_INT>=30;
        getWindow().setSoftInputMode(manualKeyboard?android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING:android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if(android.os.Build.VERSION.SDK_INT>=33)getBridge().getWebView().getSettings().setAlgorithmicDarkeningAllowed(false);
        // One native inset owner. The WebView never sits under Android's bars or keyboard.
        getWindow().setStatusBarColor(Color.rgb(247,245,237));
        getWindow().setNavigationBarColor(Color.rgb(250,249,241));
        WindowInsetsControllerCompat controller=new WindowInsetsControllerCompat(getWindow(),getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);controller.setAppearanceLightNavigationBars(true);
        applyTheme(this);
        View content=findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content,(view,insets)->{
            KeyboardInsets.apply(view,insets,manualKeyboard);
            boolean visible=insets.isVisible(WindowInsetsCompat.Type.ime());
            if(visible!=imeVisible){imeVisible=visible;getBridge().getWebView().post(()->getBridge().getWebView().evaluateJavascript("window.dispatchEvent(new CustomEvent('attention-keyboard',{detail:{visible:"+visible+"}}))",null));}
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);
        // Old global DND hides even app-allowed alerts. This UI captures and removes individual ordinary alerts.
        DndController.restoreNow(this);
        WellnessReceiver.restore(this);
        FocusScheduler.scheduleNext(this);
        DndController.reconcile(this);
        FocusListenerService.recheckAndRebind(this);
    }
}
