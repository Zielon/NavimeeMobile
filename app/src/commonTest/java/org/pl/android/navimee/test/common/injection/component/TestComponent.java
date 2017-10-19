package org.pl.android.navimee.test.common.injection.component;

import javax.inject.Singleton;

import dagger.Component;
import org.pl.android.navimee.injection.component.ApplicationComponent;
import org.pl.android.navimee.test.common.injection.module.ApplicationTestModule;

@Singleton
@Component(modules = ApplicationTestModule.class)
public interface TestComponent extends ApplicationComponent {

}
