package org.dfhu.configurationchangeandrxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ConfigurationChangeAsyncRxJava extends AppCompatActivity {

    private EditText editText;
    private TextView textView;

    private static class TextChangeBus {
        private static TextChangeBus instance = new TextChangeBus();
        private static PublishSubject<String> subject = PublishSubject.create();

        static TextChangeBus getInstance() { return instance; }
        public void setText(String v) { subject.onNext(v); }
        public Observable<String> getEvents() { return subject; }
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

    private void doSomethingSlow(final String val) {

        Observable<String> o = Observable.just(val)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        String slowly = getStringSlowly(val);
                        return Observable.just(slowly);
                    }
                });
        o.observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
                @Override public void onCompleted() { l("somethingSlow onComplete"); }
                @Override public void onError(Throwable e) { l("something slow error"); }
                @Override public void onNext(String s) { updateTextView(s); }
            });
    }

    private void updateTextView(String s) {
        TextChangeBus.getInstance().setText(s);
    }

    /** gets a random string */
    private String getStringSlowly(String val) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return " gotten slowly: " + val;
    }

    private void l(String s) {
        Log.d("config-test-change", s + " - " + this.toString());
    }
}
