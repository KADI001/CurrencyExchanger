package org.kadirov.model;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        CurrencyResponse baseCurrency,
        CurrencyResponse targetCurrency,
        BigDecimal rate
) {
}
