package com.example.steven.sjtu_lib_v2.activity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.steven.sjtu_lib_v2.R;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.Call;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    VarifyLog varifyLog;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        varifyLog=new VarifyLog();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (varifyLog.getStatus() == AsyncTask.Status.FINISHED) {
                    varifyLog = new VarifyLog();
                    varifyLog.execute();
                } else if (varifyLog.getStatus() == AsyncTask.Status.PENDING) {
                    varifyLog.execute();
                } else {
                    Toast.makeText(getApplicationContext(),"请等待",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private class VarifyLog extends AsyncTask<Void,Void,Boolean>{
        Boolean result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),"开始登陆",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpUtils.get()
                .url("http://electsys.sjtu.edu.cn/edu/login.aspx")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        result=Boolean.FALSE;
                    }

                    @Override
                    public void onResponse(String response) {
                        System.out.println("length: "+response.length());

                        final Document doc = Jsoup.parse(response);
                        final String sid = doc.getElementsByAttributeValueMatching("name", "sid").first().attr("value").toString();
                        final String returl = doc.getElementsByAttributeValueMatching("name", "returl").first().attr("value").toString();
                        final String se = doc.getElementsByAttributeValueMatching("name", "se").first().attr("value").toString();
                            final String v = doc.getElementsByAttributeValueMatching("name", "v").first().attr("value").toString();
                            OkHttpUtils.get()
                                    .url("https://jaccount.sjtu.edu.cn/jaccount/captcha")
                                    .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Safari/537.36")
                                    .build()
                                    .execute(new BitmapCallback() {
                                        @Override
                                        public void onError(Call call, Exception e) {
                                            System.out.print("cannot get image");
                                            result=Boolean.FALSE;

                                        }

                                        @Override
                                        public void onResponse(Bitmap response) {
                                            System.out.print("get image yet ");
                                            final TessBaseAPI baseAPI = new TessBaseAPI();
                                            baseAPI.init("/sdcard/", "eng");
                                            baseAPI.setImage(response);
                                            String captcha_text = baseAPI.getUTF8Text().toString();
                                            System.out.println(captcha_text);

                                            OkHttpUtils.post()
                                                    .url("https://jaccount.sjtu.edu.cn/jaccount/ulogin")
                                                    .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Safari/537.36")
                                                    .addParams("sid", sid)
                                                    .addParams("returl", returl)
                                                    .addParams("se", se)
                                                    .addParams("v", v)
                                                    .addParams("captcha", captcha_text)
                                                    .addParams("user",mEmailView.getText().toString())
                                                    .addParams("pass", mPasswordView.getText().toString())
                                                    .addParams("imageField.x", "55")
                                                    .addParams("imageField.y", "3")
                                                    .build()
                                                    .execute(new StringCallback() {

                                                        @Override
                                                        public void onError(Call call, Exception e) {
                                                            result=Boolean.FALSE;
                                                        }

                                                        @Override
                                                        public void onResponse(String response) {
                                                            System.out.println("hello");
                                                            int index = response.indexOf("loginfail");
                                                            if (index == -1) {
                                                                System.out.print("success");
                                                                result=Boolean.TRUE;
                                                            } else {
                                                                System.out.print("you should retry");
                                                                result=Boolean.FALSE;
                                                            }
                                                        }
                                                    });

                                        }
                                    });
                    }
                });
            while (result == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("result is:"+result);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(getApplicationContext(),"登陆成功",Toast.LENGTH_SHORT).show();
                try {
                    DB snappydb= DBFactory.open(getApplication(),"notvital");
                    snappydb.put("name",mEmailView.getText().toString());
                    snappydb.put("pass",mPasswordView.getText().toString());
                    snappydb.close();
                } catch (SnappydbException e) {
                    e.printStackTrace();
                }
                finish();
            }else {
                Toast.makeText(getApplicationContext(),"登陆失败",Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aBoolean);
        }
    }
}
