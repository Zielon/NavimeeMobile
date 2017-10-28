package org.pl.android.navimee.util;

/**
 * Created by Wojtek on 2017-10-28.
 */
import android.widget.TextView;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class DisplayTextOnViewAction implements Consumer<String> {

    public DisplayTextOnViewAction() {
    }

    @Override
    public void accept(String s) throws Exception {
        Timber.d("location "+s);
    }
}