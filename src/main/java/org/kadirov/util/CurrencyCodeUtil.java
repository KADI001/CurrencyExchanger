package org.kadirov.util;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyCodeUtil {
    private static Set<String> currencyCodes;

    public static boolean exists(String code){
        if (currencyCodes == null) {
            Set<Currency> currencies = Currency.getAvailableCurrencies();
            currencyCodes = currencies.stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());
        }

        return currencyCodes.contains(code);
    }
}
