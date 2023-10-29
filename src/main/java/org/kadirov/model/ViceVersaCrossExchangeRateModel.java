package org.kadirov.model;

import java.math.BigDecimal;

public record ViceVersaCrossExchangeRateModel(
        CurrencyModel targetCurrency,
        CurrencyModel firstCurrency,
        CurrencyModel secondCurrency,
        BigDecimal firstExchangeRate,
        BigDecimal secondExchangeRate
) {
}
