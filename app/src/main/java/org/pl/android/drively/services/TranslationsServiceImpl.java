package org.pl.android.drively.services;


import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.pl.android.drively.contracts.services.TranslationsService;

public class TranslationsServiceImpl implements TranslationsService {

    private Translate translate = TranslateOptions.getDefaultInstance().getService();

    @Override
    public String translateToEnglish(String text) {
        Detection detection = translate.detect(text);
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(detection.getLanguage()),
                Translate.TranslateOption.targetLanguage("en"));

        return translation.getTranslatedText();
    }
}
