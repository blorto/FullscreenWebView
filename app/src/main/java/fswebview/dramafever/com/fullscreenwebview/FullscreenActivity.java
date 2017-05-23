package fswebview.dramafever.com.fullscreenwebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) in order to show a webview
 */
public class FullscreenActivity extends AppCompatActivity {
    private static final String TAG = "FSWebView";
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private WebView webView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                                  | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                  | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hide();

        setContentView(R.layout.activity_fullscreen);

        webView = (WebView) findViewById(R.id.webview);
        //user agent stuff,  might come in handy someday
        String desktop = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
        String androidDevice = "Mozilla/5.0 (Linux; Android 4.3; Nexus 7 Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(androidDevice);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setUseWideViewPort(true);
        String databasePath = this.getApplicationContext().getDir("database",
                                                                  Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(databasePath);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onExceededDatabaseQuota(String url,
                                                String databaseIdentifier,
                                                long currentQuota,
                                                long estimatedSize,
                                                long totalUsedQuota,
                                                WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(5 * 1024 * 1024);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient() {

            /**
             * Report web resource loading error to the host application. These errors usually indicate
             * inability to connect to the server. Note that unlike the deprecated version of the callback,
             * the new version will be called for any resource (iframe, image, etc), not just for the main
             * page. Thus, it is recommended to perform minimum required work in this callback.
             *
             * @param view    The WebView that is initiating the callback.
             * @param request The originating request.
             * @param error   Information about the error occured.
             */
            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            /**
             * Report an error to the host application. These errors are unrecoverable
             * (i.e. the main resource is unavailable). The errorCode parameter
             * corresponds to one of the ERROR_* constants.
             *
             * @param view        The WebView that is initiating the callback.
             * @param errorCode   The error code corresponding to an ERROR_* value.
             * @param description A String describing the error.
             * @param failingUrl  The url that failed to load.
             * @deprecated Use {@link #onReceivedError(WebView, WebResourceRequest, WebResourceError)
             * onReceivedError(WebView, WebResourceRequest, WebResourceError)} instead.
             */
            @Override
            public void onReceivedError(WebView view,
                                        int errorCode,
                                        String description,
                                        String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           final SslErrorHandler handler,
                                           SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.v(TAG, "url = " + url);
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v(TAG, "url = " + url);
                view.loadUrl(url);
                return true;
            }
        });


        if (savedInstanceState == null) {
            webView.loadUrl(getString(R.string.url_to_load));
//            webView.loadUrl("https://framer.com/");
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        webView.setPadding(0, 0, 0, 0);
//        webView.setInitialScale(getScale());

        hide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
}
