package org.kadirov.mapper.model;

import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;
import org.kadirov.model.ViceVersaCrossExchangeRateModel;

public class ViceVersaCrossExchangeRateModelMapper implements Mapper<CrossExchangeRateDTO, ViceVersaCrossExchangeRateModel> {
    private final CurrencyModelMapper currencyModelMapper;

    public ViceVersaCrossExchangeRateModelMapper(CurrencyModelMapper currencyModelMapper) {
        this.currencyModelMapper = currencyModelMapper;
    }
    @Override
    public ViceVersaCrossExchangeRateModel map(CrossExchangeRateDTO crossExchangeRateDTO) throws MappingException {
        return new ViceVersaCrossExchangeRateModel(
                currencyModelMapper.map(crossExchangeRateDTO.firstExchangeRate().getTargetCurrency()),
                currencyModelMapper.map(crossExchangeRateDTO.firstExchangeRate().getBaseCurrency()),
                currencyModelMapper.map(crossExchangeRateDTO.secondExchangeRate().getBaseCurrency()),
                crossExchangeRateDTO.firstExchangeRate().getRate(),
                crossExchangeRateDTO.secondExchangeRate().getRate()
        );
    }
}
