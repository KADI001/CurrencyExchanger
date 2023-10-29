package org.kadirov.mapper.model;

import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.CrossExchangeRateModel;

public class CrossExchangeRateModelMapper implements Mapper<CrossExchangeRateDTO, CrossExchangeRateModel> {

    private final CurrencyModelMapper currencyModelMapper;

    public CrossExchangeRateModelMapper(CurrencyModelMapper currencyModelMapper) {
        this.currencyModelMapper = currencyModelMapper;
    }

    @Override
    public CrossExchangeRateModel map(CrossExchangeRateDTO crossExchangeRateDTO) throws MappingException {
        return new CrossExchangeRateModel(
                currencyModelMapper.map(crossExchangeRateDTO.firstExchangeRate().getBaseCurrency()),
                currencyModelMapper.map(crossExchangeRateDTO.firstExchangeRate().getTargetCurrency()),
                currencyModelMapper.map(crossExchangeRateDTO.secondExchangeRate().getTargetCurrency()),
                crossExchangeRateDTO.firstExchangeRate().getRate(),
                crossExchangeRateDTO.secondExchangeRate().getRate()
                );
    }
}
