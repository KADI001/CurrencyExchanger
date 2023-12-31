package org.kadirov.dto;

import org.kadirov.entity.CurrencyEntity;

import java.math.BigDecimal;

public record CrossExchangeRateDTO(
        CurrencyEntity baseCurrency,
        CurrencyEntity firstCurrency,
        CurrencyEntity secondCurrency,
        BigDecimal firstExchangeRate,
        BigDecimal secondExchangeRate
) {
}
