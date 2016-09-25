package org.dfhu.configurationchangeandrxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ConfigurationChangeAsyncRxJava extends AppCompatActivity {

    private EditText editText;
    private TextView textView;

    /**
     * Event bus for updating the text view
     */
    private static class TextChangeBus {
        private static final TextChangeBus instance = new TextChangeBus();
        private static final PublishSubject<String> subject = PublishSubject.create();

        static TextChangeBus getInstance() { return instance; }
        void setText(String v) { subject.onNext(v); }
        Observable<String> getEvents() { return subject; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_change_async_rx_java);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);

        // Submit editText creates an observable that is subscribed to
        // and calls TextChangeBus.getInstance().setText()
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String val = editText.getText().toString();
                doSomethingSlow(val);
                Log.d("config-change-test", editText.getText().toString());
                return false;
            }
        });

        // Want this to change textView of current, visible activity
        Observable<String> textChanger = TextChangeBus.getInstance().getEvents();
        textChanger.observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
            @Override public void onCompleted() { l("textChanger onComplete"); }
            @Override public void onError(Throwable e) { l("textChanger onError"); }
            @Override public void onNext(String s) {
                textView.setText(s);
            }
        });
    }

    /** Creates an example long running observable and subscribes to it */
    private void doSomethingSlow(final String val) {

        Observable.just(val)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        String slowly = getStringSlowly(val);
                        return Observable.just(slowly);
                    }
                })
                .flatMap(new Func1<String, Observable<InputStream>>() {
                    @Override
                    public Observable<InputStream> call(String s) {
                        URL url = null;
                        try {
                            url = new URL("http://192.168.1.6:3000/");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        URLConnection urlConnection = null;
                        try {
                            //noinspection ConstantConditions
                            urlConnection = url.openConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            //noinspection ConstantConditions
                            return Observable.just(urlConnection.getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<InputStream, Observable<String>>() {
                    @Override
                    public Observable<String> call(InputStream inputStream) {
                        byte[] buffer = new byte[30000];
                        try {
                            //noinspection ResultOfMethodCallIgnored,ResultOfMethodCallIgnored
                            inputStream.read(buffer);
                            return Observable.just(new String(buffer));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() { l("somethingSlow onComplete"); }
                    @Override public void onError(Throwable e) { l("something slow error"); }
                    @Override public void onNext(String s) { updateTextView(s); }
                });
    }

    private void updateTextView(String s) { TextChangeBus.getInstance().setText(s); }

    private void l(String s) { Log.d("config-test-change", s + " - " + this.toString()); }

    /** gets a user string after a delay */
    private String getStringSlowly(String val) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return " gotten slowly: " + val;
    }
}
