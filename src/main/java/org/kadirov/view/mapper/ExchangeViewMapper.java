package org.kadirov.view.mapper;

import org.kadirov.model.ExchangeModel;
import org.kadirov.view.ExchangeView;

public class ExchangeViewMapper {
    public ExchangeView map(ExchangeModel exchangeModel){
        return new ExchangeView(exchangeModel.getBaseCurrency(), exchangeModel.getTargetCurrency(),
                exchangeModel.getRate(), exchangeModel.getAmount(), exchangeModel.getConvertedAmount());
    }
}
