package org.pl.android.drively.injection.module;

import org.pl.android.drively.contracts.repositories.CoordinatesRepository;
import org.pl.android.drively.contracts.repositories.NotificationsRepository;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.repositories.CoordinatesRepositoryImpl;
import org.pl.android.drively.repositories.NotificationsRepositoryImpl;
import org.pl.android.drively.repositories.UsersRepositoryImpl;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class RepositoriesModule {

    @Binds
    public abstract UsersRepository provideUserRepository(UsersRepositoryImpl usersRepository);

    @Binds
    public abstract NotificationsRepository provideNotificationsRepository(NotificationsRepositoryImpl notificationsRepository);

    @Binds
    public abstract CoordinatesRepository provideCoordinatesRepository(CoordinatesRepositoryImpl notificationsRepository);
}
