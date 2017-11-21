package org.pl.android.navimee.util;

import android.location.Address;

import io.reactivex.functions.Function;

public class AddressToStringFunc implements Function<Address, String> {
    @Override
    public String apply(Address address) {
        if (address == null) return "";
        return address.getLocality();
    }
}
