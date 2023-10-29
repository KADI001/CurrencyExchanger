package org.kadirov.model;

import java.math.BigDecimal;

public record CrossExchangeRateModel(
        CurrencyModel baseCurrency,
        CurrencyModel firstCurrency,
        CurrencyModel secondCurrency,
        BigDecimal firstExchangeRate,
        BigDecimal secondExchangeRate
) {
}
