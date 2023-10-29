package org.kadirov.dao;

import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRatesRepository {
    List<ExchangeRateEntity> selectAll() throws SQLException;
    Optional<ExchangeRateEntity> selectByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    boolean existsByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
    ExchangeRateEntity insert(final ExchangeRateEntity exchangeRateEntity) throws SQLException;
    ExchangeRateEntity updateRateByBaseCurrencyCodeAndTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException;
    Optional<CrossExchangeRateDTO> selectByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException;
    Optional<CrossExchangeRateDTO> selectByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException;
    boolean existsDoubleByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException;
    boolean existsDoubleByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(String firstTargetCurrencyCode, String secondTargetCurrencyCode) throws SQLException;
}
