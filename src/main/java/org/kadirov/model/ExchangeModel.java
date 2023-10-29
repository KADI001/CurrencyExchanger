package org.kadirov.model;

import java.math.BigDecimal;

public record ExchangeModel(
        CurrencyModel baseCurrency,
        CurrencyModel targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}
