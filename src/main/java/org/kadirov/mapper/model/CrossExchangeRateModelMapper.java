package org.kadirov.mapper.model;

import org.kadirov.entity.CrossExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CrossExchangeRateModel;

public class CrossExchangeRateModelMapper implements Mapper<CrossExchangeRateEntity, CrossExchangeRateModel> {

    private final CurrencyModelMapper currencyModelMapper;

    public CrossExchangeRateModelMapper(CurrencyModelMapper currencyModelMapper) {
        this.currencyModelMapper = currencyModelMapper;
    }

    @Override
    public CrossExchangeRateModel map(CrossExchangeRateEntity crossExchangeRateEntity) throws MappingException {
        return new CrossExchangeRateModel(
                currencyModelMapper.map(crossExchangeRateEntity.firstExchangeRate().getBaseCurrency()),
                currencyModelMapper.map(crossExchangeRateEntity.firstExchangeRate().getTargetCurrency()),
                currencyModelMapper.map(crossExchangeRateEntity.secondExchangeRate().getTargetCurrency()),
                crossExchangeRateEntity.firstExchangeRate().getRate(),
                crossExchangeRateEntity.secondExchangeRate().getRate()
                );
    }
}
