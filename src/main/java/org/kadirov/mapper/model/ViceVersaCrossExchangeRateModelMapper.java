package org.kadirov.mapper.model;

import org.kadirov.entity.CrossExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.ViceVersaCrossExchangeRateModel;

public class ViceVersaCrossExchangeRateModelMapper implements Mapper<CrossExchangeRateEntity, ViceVersaCrossExchangeRateModel> {
    private final CurrencyModelMapper currencyModelMapper;

    public ViceVersaCrossExchangeRateModelMapper(CurrencyModelMapper currencyModelMapper) {
        this.currencyModelMapper = currencyModelMapper;
    }
    @Override
    public ViceVersaCrossExchangeRateModel map(CrossExchangeRateEntity crossExchangeRateEntity) throws MappingException {
        return new ViceVersaCrossExchangeRateModel(
                currencyModelMapper.map(crossExchangeRateEntity.firstExchangeRate().getTargetCurrency()),
                currencyModelMapper.map(crossExchangeRateEntity.firstExchangeRate().getBaseCurrency()),
                currencyModelMapper.map(crossExchangeRateEntity.secondExchangeRate().getBaseCurrency()),
                crossExchangeRateEntity.firstExchangeRate().getRate(),
                crossExchangeRateEntity.secondExchangeRate().getRate()
        );
    }
}
