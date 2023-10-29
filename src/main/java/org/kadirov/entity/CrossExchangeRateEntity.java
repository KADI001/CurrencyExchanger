package org.kadirov.entity;

public record CrossExchangeRateEntity(
        ExchangeRateEntity firstExchangeRate,
        ExchangeRateEntity secondExchangeRate
) {

}
