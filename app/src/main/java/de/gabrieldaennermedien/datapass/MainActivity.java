package de.gabrieldaennermedien.datapass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {
    //private instances
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private TelephonyManager telephonyManager;
    private WebView webView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        progressBar = findViewById(R.id.progressbar);
        swipeRefresh = findViewById(R.id.swiperefresh);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        webView = findViewById(R.id.webview);

        progressBar.setMax(100);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
        });
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
            Toast.makeText(MainActivity.this, getString(R.string.msg_error_url), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_checkBalance) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                doUSSDRequest("*100#");
            }

            return true;
        }

        if (id == R.id.action_checkInclusiveMinutes) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                doUSSDRequest("*100*3#");
            }

            return true;
        }

        if (id == R.id.action_addBalance) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                showCashCodeDialog();
            }

            return true;
        }

        if (id == R.id.action_reloadPage) {
            if (webView != null) {
                webView.reload();
            }

            return true;
        }

        if (id == R.id.action_aboutApp) {
            Toast.makeText(MainActivity.this, getString(R.string.app_name) + "\n" +
                    getString(R.string.app_version) + "\n" + getString(R.string.copyright_info) +
                    " " + getString(R.string.company_name), Toast.LENGTH_LONG).show();

            return true;
        }

        if (id == R.id.action_closeApp) {
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
        if (webView != null && webView.canGoBack()) {
            webView.goBack();

        } else {
            super.onBackPressed();
        }
    }

    /**
     * doUSSDRequest performs a USSD-request and displays the result. (only Android Oreo or higher)
     *
     * @param ussdCode the request code
     */
    private void doUSSDRequest(String ussdCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                telephonyManager.sendUssdRequest(ussdCode, new TelephonyManager.UssdResponseCallback() {
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);
                        showInfoAlert(getString(R.string.dialog_checkBalance_title), response.toString());
                    }

                    @Override
                    public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                        super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                        showInfoAlert(getString(R.string.dialog_ussd_failed_title), getString(R.string.dialog_ussd_failed_msg));
                    }
                }, new Handler());
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
        } else {
            showInfoAlert(getString(R.string.dialog_device_not_supported_title), getString(R.string.dialog_device_not_supported_msg));
        }
    }

    /**
     * showCashCodeDialog creates a dialog where the user can enter his/her cashcode.
     */
    @SuppressLint("InflateParams")
    private void showCashCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogsView = inflater.inflate(R.layout.dialog_cashcode, null);

        final EditText editTextCashCode = dialogsView.findViewById(R.id.editText_cashcode);

        builder.setView(dialogsView)
                .setTitle(getString(R.string.dialog_addBalance_title))
                .setPositiveButton(getString(R.string.button_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userInput = editTextCashCode.getText().toString();

                        if (TextUtils.isEmpty(userInput)) {
                            showInfoAlert(getString(R.string.dialog_emptyInput_title), getString(R.string.dialog_emptyInput_msg));
                            return;
                        }

                        dialog.dismiss();
                        doUSSDRequest("*101*" + userInput + "#");
                    }
                })
                .setNegativeButton(getString(R.string.button_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    /**
     * showInfoAlert creates a simple dialog with a title, text and an okay button
     *
     * @param title title of the dialog
     * @param msg   main text/message of the dialog
     */
    private void showInfoAlert(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.button_positive), null);
        builder.show();
    }
}
