package org.kadirov.mapper.entity;

import org.kadirov.dto.ViceVersaCrossExchangeRateDTO;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViceVersaCrossExchangeEntityRateMapper implements Mapper<ResultSet, ViceVersaCrossExchangeRateDTO> {
    @Override
    public ViceVersaCrossExchangeRateDTO map(ResultSet resultSet) throws MappingException {
        try {
            int targetCurrencyId = resultSet.getInt("target_currency_id");
            int firstBaseCurrencyId = resultSet.getInt("first_base_currency_id");
            BigDecimal firstRate = resultSet.getBigDecimal("first_rate");
            int secondBaseCurrencyId = resultSet.getInt("second_base_currency_id");
            BigDecimal secondRate = resultSet.getBigDecimal("second_rate");
            String targetCurrencyCode = resultSet.getString("t_code");
            String targetCurrencyFullName = resultSet.getString("t_full_name");
            String targetCurrencySign = resultSet.getString("t_sign");
            String firstBaseCurrencyCode = resultSet.getString("fbc_code");
            String firstBaseCurrencyFullName = resultSet.getString("fbc_full_name");
            String firstBaseCurrencySign = resultSet.getString("fbc_sign");
            String secondBaseCurrencyCode = resultSet.getString("sbc_code");
            String secondBaseCurrencyFullName = resultSet.getString("sbc_full_name");
            String secondBaseCurrencySign = resultSet.getString("sbc_sign");

            CurrencyEntity targetCurrency =
                    new CurrencyEntity(targetCurrencyId, targetCurrencyCode, targetCurrencyFullName, targetCurrencySign);
            CurrencyEntity firstBaseCurrency =
                    new CurrencyEntity(firstBaseCurrencyId, firstBaseCurrencyCode, firstBaseCurrencyFullName, firstBaseCurrencySign);
            CurrencyEntity secondBaseCurrency =
                    new CurrencyEntity(secondBaseCurrencyId, secondBaseCurrencyCode, secondBaseCurrencyFullName, secondBaseCurrencySign);

            return new ViceVersaCrossExchangeRateDTO(targetCurrency, firstBaseCurrency, secondBaseCurrency, firstRate, secondRate);
        } catch (SQLException sqle) {
            throw new MappingException(sqle);
        }
    }
}
