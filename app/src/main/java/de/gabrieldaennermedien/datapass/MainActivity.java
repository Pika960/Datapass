package de.gabrieldaennermedien.datapass;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    //private instances
    private WebView webView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        try {
            webView.loadUrl("https://datapass.de/");
        } catch (Exception exc) {
            Toast.makeText(MainActivity.this, getString(R.string.msg_error_url),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_reloadPage) {
            if(webView != null)
            {
                webView.reload();
            }

            return true;
        }

        if(id == R.id.action_aboutApp) {
            Toast.makeText(MainActivity.this, getString(R.string.app_name) + "\n" +
                    getString(R.string.app_version) + "\n" + getString(R.string.copyright_info) +
                    " " + getString(R.string.company_name), Toast.LENGTH_LONG).show();

            return true;
        }

        if(id == R.id.action_closeApp) {
            finishAffinity();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if(webView != null && webView.canGoBack()) {
            webView.goBack();
        }

        else {
            super.onBackPressed();
        }
    }
}
