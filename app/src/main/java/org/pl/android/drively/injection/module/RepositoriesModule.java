package org.pl.android.drively.injection.module;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.repositories.UsersRepositoryImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoriesModule {

    @Provides
    public UsersRepository provideUserRepository() {
        return new UsersRepositoryImpl();
    }
}