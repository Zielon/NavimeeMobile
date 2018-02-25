package org.pl.android.drively.injection.module;


import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.repositories.UsersRepositoryImpl;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class RepositoriesModule {

    @Binds
    public abstract UsersRepository provideUserRepository(UsersRepositoryImpl usersRepository);
}