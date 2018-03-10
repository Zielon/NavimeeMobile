package org.pl.android.drively.services;

import android.os.AsyncTask;

import com.annimon.stream.Stream;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.pl.android.drively.BuildConfig;

import java.util.List;

public class TranslationsServiceImpl extends AsyncTask<String, String, List<String>> {

    @Override
    protected List<String> doInBackground(String... strings) {
        Translate translate = TranslateOptions.newBuilder().setApiKey(BuildConfig.GOOGLE_TRANSLATIONS_KEY).build().getService();
        return Stream.of(strings).map(text -> {
            Detection detection = translate.detect(text);
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(detection.getLanguage()),
                    Translate.TranslateOption.targetLanguage("en"));

            return translation.getTranslatedText();
        }).toList();
    }
}
