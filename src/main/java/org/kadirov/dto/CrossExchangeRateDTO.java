package org.kadirov.dto;

import org.kadirov.entity.ExchangeRateEntity;

public record CrossExchangeRateDTO(
        ExchangeRateEntity firstExchangeRate,
        ExchangeRateEntity secondExchangeRate
) {

}
