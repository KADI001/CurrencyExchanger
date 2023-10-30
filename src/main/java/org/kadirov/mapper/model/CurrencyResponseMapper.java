package org.kadirov.mapper.model;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CurrencyResponse;

public class CurrencyResponseMapper implements Mapper<CurrencyEntity, CurrencyResponse> {
    @Override
    public CurrencyResponse map(CurrencyEntity currencyEntity) throws MappingException {
        return new CurrencyResponse(currencyEntity.getName(), currencyEntity.getCode(), currencyEntity.getSign());
    }
}
