package org.kadirov.mapper.model;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CurrencyResponse;
import org.kadirov.model.ExchangeRateResponse;

public class ExchangeRateResponseMapper implements Mapper<ExchangeRateEntity, ExchangeRateResponse> {

    private final Mapper<CurrencyEntity, CurrencyResponse> currencyResponseMapper;

    public ExchangeRateResponseMapper(Mapper<CurrencyEntity, CurrencyResponse> currencyResponseMapper) {
        this.currencyResponseMapper = currencyResponseMapper;
    }

    @Override
    public ExchangeRateResponse map(ExchangeRateEntity exchangeRateEntity) throws MappingException {
        return new ExchangeRateResponse(
                currencyResponseMapper.map(exchangeRateEntity.getBaseCurrency()),
                currencyResponseMapper.map(exchangeRateEntity.getTargetCurrency()),
                exchangeRateEntity.getRate()
        );
    }
}
