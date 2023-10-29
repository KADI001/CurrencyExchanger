package org.kadirov.mapper.model;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CurrencyModel;

public class CurrencyModelMapperToEntity implements Mapper<CurrencyModel, CurrencyEntity> {
    @Override
    public CurrencyEntity map(CurrencyModel currencyModel) throws MappingException {
        return new CurrencyEntity(currencyModel.fullName(), currencyModel.code(), currencyModel.sign());
    }
}
