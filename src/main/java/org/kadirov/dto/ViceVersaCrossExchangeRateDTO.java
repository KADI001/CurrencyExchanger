package org.kadirov.dto;

import org.kadirov.entity.CurrencyEntity;

import java.math.BigDecimal;

public record ViceVersaCrossExchangeRateDTO(
        CurrencyEntity targetCurrency,
        CurrencyEntity firstCurrency,
        CurrencyEntity secondCurrency,
        BigDecimal firstExchangeRate,
        BigDecimal secondExchangeRate
) {
}
