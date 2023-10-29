package org.kadirov.mapper.model;

import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.ExchangeRateModel;

public class ExchangeRateModelMapperToEntity implements Mapper<ExchangeRateModel, ExchangeRateEntity> {

    private final CurrencyModelMapperToEntity currencyModelMapperToEntity;

    public ExchangeRateModelMapperToEntity(CurrencyModelMapperToEntity currencyModelMapperToEntity) {
        this.currencyModelMapperToEntity = currencyModelMapperToEntity;
    }

    @Override
    public ExchangeRateEntity map(ExchangeRateModel exchangeRateModel) throws MappingException {
        return new ExchangeRateEntity(
                currencyModelMapperToEntity.map(exchangeRateModel.baseCurrency()),
                currencyModelMapperToEntity.map(exchangeRateModel.targetCurrency()),
                exchangeRateModel.rate());
    }
}
