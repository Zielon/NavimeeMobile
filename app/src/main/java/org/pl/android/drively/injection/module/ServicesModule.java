package org.pl.android.drively.injection.module;

import org.pl.android.drively.contracts.services.TranslationsService;
import org.pl.android.drively.services.TranslationsServiceImpl;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ServicesModule {

    @Binds
    public abstract TranslationsService provideTranslationService(TranslationsServiceImpl translationsService);
}
