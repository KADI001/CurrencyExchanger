package org.kadirov.mapper.model;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CurrencyModel;
import org.kadirov.model.ExchangeRateModel;

public class ExchangeRateModelMapper implements Mapper<ExchangeRateEntity, ExchangeRateModel> {

    private final Mapper<CurrencyEntity, CurrencyModel> currencyModelMapper;

    public ExchangeRateModelMapper(Mapper<CurrencyEntity, CurrencyModel> currencyModelMapper) {
        this.currencyModelMapper = currencyModelMapper;
    }

    @Override
    public ExchangeRateModel map(ExchangeRateEntity exchangeRateEntity) throws MappingException {
        return new ExchangeRateModel(
                currencyModelMapper.map(exchangeRateEntity.getBaseCurrency()),
                currencyModelMapper.map(exchangeRateEntity.getTargetCurrency()),
                exchangeRateEntity.getRate());
    }
}
