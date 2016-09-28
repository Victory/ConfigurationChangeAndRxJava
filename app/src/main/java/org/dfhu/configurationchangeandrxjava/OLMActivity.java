package org.dfhu.configurationchangeandrxjava;

import android.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import rx.Observable;
import rx.Subscriber;

public class OLMActivity extends AppCompatActivity {

    private android.app.LoaderManager loaderManager;
    private final int loaderId = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olm);
    }

    public void observe(View view) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                loaderManager.initLoader(loaderId, null,
                        new LoaderManager.LoaderCallbacks<Object>() {
                            @Override
                            public android.content.Loader<Object> onCreateLoader(int id, Bundle args) {
                                return null;
                            }

                            @Override
                            public void onLoadFinished(android.content.Loader<Object> loader, Object data) {

                            }

                            @Override
                            public void onLoaderReset(android.content.Loader<Object> loader) {

                            }
                        });
            }
        });
    }
}
