package org.kadirov.service;

import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.dto.ViceVersaCrossExchangeRateDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRatesRepository exchangeRatesRepository;

    public ExchangeRateServiceImpl(ExchangeRatesRepository exchangeRatesRepository) {
        this.exchangeRatesRepository = exchangeRatesRepository;
    }

    @Override
    public Optional<ExchangeRateEntity> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRateEntity> result = getDirectExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getReverseExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getCrossExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getViceVersaCrossExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        return result;
    }

    @Override
    public Optional<ExchangeRateEntity> getReverseExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateEntity result = null;
        Optional<ExchangeRateEntity> exchangeRateModel = getDirectExchangeRateByCode(targetCurrencyCode, baseCurrencyCode);

        if (exchangeRateModel.isPresent())
            result = new ExchangeRateEntity(
                    exchangeRateModel.get().getTargetCurrency(),
                    exchangeRateModel.get().getBaseCurrency(),
                    BigDecimal.ONE.divide(exchangeRateModel.get().getRate(), 2, RoundingMode.HALF_UP));

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ExchangeRateEntity> getDirectExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRatesRepository.selectByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);
    }

    @Override
    public Optional<ExchangeRateEntity> getCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateEntity result = null;

        if (exchangeRatesRepository.existsDoubleByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
            Optional<CrossExchangeRateDTO> crossExchangeRateEntity =
                    exchangeRatesRepository.selectByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);

            if (crossExchangeRateEntity.isPresent()) {
                CrossExchangeRateDTO crossExchangeRateDTO = crossExchangeRateEntity.get();
                BigDecimal rate = crossExchangeRateDTO.secondExchangeRate().divide(crossExchangeRateDTO.firstExchangeRate(), 2, RoundingMode.HALF_UP);
                result = new ExchangeRateEntity(crossExchangeRateDTO.firstCurrency(), crossExchangeRateDTO.secondCurrency(), rate);
            }
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ExchangeRateEntity> getViceVersaCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateEntity result = null;
        if (exchangeRatesRepository.existsDoubleByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
            Optional<ViceVersaCrossExchangeRateDTO> optionalViceVersaCrossExchangeRateDTO =
                    exchangeRatesRepository.selectByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(baseCurrencyCode, targetCurrencyCode);

            if (optionalViceVersaCrossExchangeRateDTO.isPresent()) {
                ViceVersaCrossExchangeRateDTO viceVersaCrossExchangeRateDTO = optionalViceVersaCrossExchangeRateDTO.get();

                BigDecimal firstExchangeRate = viceVersaCrossExchangeRateDTO.firstExchangeRate();
                BigDecimal secondExchangeRate = viceVersaCrossExchangeRateDTO.secondExchangeRate();
                BigDecimal rate = firstExchangeRate.divide(secondExchangeRate, 2, RoundingMode.HALF_UP);
                result = new ExchangeRateEntity(viceVersaCrossExchangeRateDTO.firstCurrency(), viceVersaCrossExchangeRateDTO.secondCurrency(), rate);
            }
        }

        return Optional.ofNullable(result);
    }
}
