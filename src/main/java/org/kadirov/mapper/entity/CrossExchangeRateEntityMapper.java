package org.kadirov.mapper.entity;

import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrossExchangeRateEntityMapper implements Mapper<ResultSet, CrossExchangeRateDTO> {
    @Override
    public CrossExchangeRateDTO map(ResultSet resultSet) throws MappingException {
        try {
            int firstId = resultSet.getInt("first_id");

            int secondId = resultSet.getInt("second_id");
            int baseCurrencyId = resultSet.getInt("base_currency_id");
            int firstTargetCurrencyId = resultSet.getInt("first_target_currency_id");
            BigDecimal firstRate = resultSet.getBigDecimal("first_rate");
            int secondTargetCurrencyId = resultSet.getInt("second_target_currency_id");
            BigDecimal secondRate = resultSet.getBigDecimal("second_rate");
            String baseCurrencyCode = resultSet.getString("b_code");
            String baseCurrencyFullName = resultSet.getString("b_full_name");
            String baseCurrencySign = resultSet.getString("b_sign");
            String firstTargetCurrencyCode = resultSet.getString("ftc_code");
            String firstTargetCurrencyFullName = resultSet.getString("ftc_full_name");
            String firstTargetCurrencySign = resultSet.getString("ftc_sign");
            String secondTargetCurrencyCode = resultSet.getString("stc_code");
            String secondTargetCurrencyFullName = resultSet.getString("stc_full_name");
            String secondTargetCurrencySign = resultSet.getString("stc_sign");

            CurrencyEntity baseCurrency = new
                    CurrencyEntity(baseCurrencyId, baseCurrencyCode, baseCurrencyFullName, baseCurrencySign);
            CurrencyEntity firstTargetCurrency =
                    new CurrencyEntity(firstTargetCurrencyId, firstTargetCurrencyCode, firstTargetCurrencyFullName, firstTargetCurrencySign);
            CurrencyEntity secondTargetCurrency =
                    new CurrencyEntity(secondTargetCurrencyId, secondTargetCurrencyCode, secondTargetCurrencyFullName, secondTargetCurrencySign);

            ExchangeRateEntity firstExchangeRateEntity = new ExchangeRateEntity(firstId, baseCurrency, firstTargetCurrency, firstRate);
            ExchangeRateEntity secondExchangeRateEntity = new ExchangeRateEntity(secondId, baseCurrency, secondTargetCurrency, secondRate);

            return new CrossExchangeRateDTO(firstExchangeRateEntity, secondExchangeRateEntity);
        } catch (SQLException sqle) {
            throw new MappingException(sqle);
        }
    }
}
