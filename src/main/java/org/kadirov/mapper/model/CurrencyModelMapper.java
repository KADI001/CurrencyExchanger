package org.kadirov.mapper.model;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CurrencyModel;

public class CurrencyModelMapper implements Mapper<CurrencyEntity, CurrencyModel> {
    @Override
    public CurrencyModel map(CurrencyEntity currencyEntity) throws MappingException {
        return new CurrencyModel(currencyEntity.getFullName(), currencyEntity.getCode(), currencyEntity.getSign());
    }
}
