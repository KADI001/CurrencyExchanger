package org.kadirov.service;

import org.kadirov.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    Optional<ExchangeRateEntity> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateEntity> getReverseExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateEntity> getDirectExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateEntity> getCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateEntity> getViceVersaCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
}
