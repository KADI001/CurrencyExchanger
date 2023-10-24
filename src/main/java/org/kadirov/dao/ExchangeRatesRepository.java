package org.kadirov.dao;

import org.kadirov.dao.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface ExchangeRatesRepository {
    List<ExchangeRateEntity> selectAll() throws SQLException;
    ExchangeRateEntity selectByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    boolean existsByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    ExchangeRateEntity insert(final ExchangeRateEntity exchangeRateEntity) throws SQLException;
    ExchangeRateEntity updateRateByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException;
}
