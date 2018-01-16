package org.pl.android.drively.test.common.injection.component;

import javax.inject.Singleton;

import dagger.Component;
import org.pl.android.drively.injection.component.ApplicationComponent;
import org.pl.android.drively.test.common.injection.module.ApplicationTestModule;

@Singleton
@Component(modules = ApplicationTestModule.class)
public interface TestComponent extends ApplicationComponent {

}
