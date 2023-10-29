package org.kadirov.model;

import java.math.BigDecimal;

public record ExchangeRateModel(
        CurrencyModel baseCurrency,
        CurrencyModel targetCurrency,
        BigDecimal rate
) {
}
