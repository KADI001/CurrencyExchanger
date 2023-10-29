package org.kadirov.service;

import org.kadirov.model.ExchangeRateModel;
import org.kadirov.service.exception.CurrencyCodeValidationException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRateModel> getAll() throws SQLException;
    Optional<ExchangeRateModel> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateModel> getReverseExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateModel> getDirectExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateModel> getCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    Optional<ExchangeRateModel> getViceVersaCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    boolean existsExchangeRateByBaseCodeAndTargetCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    void add(ExchangeRateModel exchangeRateModel) throws SQLException;
    ExchangeRateModel update(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException;
}
