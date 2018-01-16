package org.pl.android.drively.util;

import java.util.ArrayList;
import java.util.List;

public class ExternalProviders {

    public static List<String> getExternalProviders(){
        List<String> providers = new ArrayList<>();

        providers.add("facebook.com");
        providers.add("google.com");

        return providers;
    }
}
