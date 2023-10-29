package org.kadirov.service;

import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.dto.CrossExchangeRateDTO;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.model.*;
import org.kadirov.model.CrossExchangeRateModel;
import org.kadirov.model.ExchangeRateModel;
import org.kadirov.model.ViceVersaCrossExchangeRateModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRatesRepository exchangeRatesRepository;
    private final ExchangeRateModelMapper exchangeRateModelMapper;
    private final CrossExchangeRateModelMapper crossExchangeRateModelMapper;
    private final ViceVersaCrossExchangeRateModelMapper viceVersaCrossExchangeRateModelMapper;
    private final ExchangeRateModelMapperToEntity exchangeRateModelMapperToEntity;

    public ExchangeRateServiceImpl(ExchangeRatesRepository exchangeRatesRepository) {
        this.exchangeRatesRepository = exchangeRatesRepository;
        this.exchangeRateModelMapper = new ExchangeRateModelMapper(new CurrencyModelMapper());
        this.crossExchangeRateModelMapper = new CrossExchangeRateModelMapper(new CurrencyModelMapper());
        this.viceVersaCrossExchangeRateModelMapper = new ViceVersaCrossExchangeRateModelMapper(new CurrencyModelMapper());
        this.exchangeRateModelMapperToEntity = new ExchangeRateModelMapperToEntity(new CurrencyModelMapperToEntity());
    }

    @Override
    public List<ExchangeRateModel> getAll() throws SQLException {
        List<ExchangeRateModel> result = new ArrayList<>();

        List<ExchangeRateEntity> exchangeRateEntities = exchangeRatesRepository.selectAll();

        for (ExchangeRateEntity exchangeRateEntity : exchangeRateEntities) {
            result.add(exchangeRateModelMapper.map(exchangeRateEntity));
        }

        return result;
    }

    @Override
    public Optional<ExchangeRateModel> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRateModel> result = getDirectExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getReverseExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getCrossExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (result.isEmpty())
            result = getViceVersaCrossExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        return result;
    }

    @Override
    public Optional<ExchangeRateModel> getReverseExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateModel result = null;
        Optional<ExchangeRateModel> exchangeRateModel = getDirectExchangeRateByCode(targetCurrencyCode, baseCurrencyCode);

        if (exchangeRateModel.isPresent())
            result = new ExchangeRateModel(
                    exchangeRateModel.get().targetCurrency(),
                    exchangeRateModel.get().baseCurrency(),
                    BigDecimal.ONE.divide(exchangeRateModel.get().rate(), 2, RoundingMode.HALF_UP));

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ExchangeRateModel> getDirectExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateModel result = null;

        Optional<ExchangeRateEntity> optionalExchangeRateEntity =
                exchangeRatesRepository.selectByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);

        if (optionalExchangeRateEntity.isPresent())
            result = exchangeRateModelMapper.map(optionalExchangeRateEntity.orElseThrow());

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ExchangeRateModel> getCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateModel result = null;

        if (exchangeRatesRepository.existsDoubleByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
            Optional<CrossExchangeRateDTO> crossExchangeRateEntity =
                    exchangeRatesRepository.selectByFirstTargetCurrencyCodeAndSecondTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);

            if (crossExchangeRateEntity.isPresent()) {
                CrossExchangeRateModel crossExchangeRateModel = crossExchangeRateModelMapper.map(crossExchangeRateEntity.get());
                BigDecimal rate = crossExchangeRateModel.secondExchangeRate().divide(crossExchangeRateModel.firstExchangeRate(), 2, RoundingMode.HALF_UP);
                result = new ExchangeRateModel(crossExchangeRateModel.firstCurrency(), crossExchangeRateModel.secondCurrency(), rate);
            }
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ExchangeRateModel> getViceVersaCrossExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        ExchangeRateModel result = null;
        if (exchangeRatesRepository.existsDoubleByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
            Optional<CrossExchangeRateDTO> crossExchangeRateEntity =
                    exchangeRatesRepository.selectByFirstBaseCurrencyCodeAndSecondBaseCurrencyCode(baseCurrencyCode, targetCurrencyCode);

            if (crossExchangeRateEntity.isPresent()) {
                ViceVersaCrossExchangeRateModel viceVersaCrossExchangeRateModel = viceVersaCrossExchangeRateModelMapper.map(crossExchangeRateEntity.get());

                BigDecimal firstExchangeRate = viceVersaCrossExchangeRateModel.firstExchangeRate();
                BigDecimal secondExchangeRate = viceVersaCrossExchangeRateModel.secondExchangeRate();
                BigDecimal rate = firstExchangeRate.divide(secondExchangeRate, 2, RoundingMode.HALF_UP);
                result = new ExchangeRateModel(viceVersaCrossExchangeRateModel.firstCurrency(), viceVersaCrossExchangeRateModel.secondCurrency(), rate);
            }
        }

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsExchangeRateByBaseCodeAndTargetCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode);
    }

    @Override
    public void add(ExchangeRateModel exchangeRateModel) throws SQLException {
        exchangeRatesRepository.insert(exchangeRateModelMapperToEntity.map(exchangeRateModel));
    }

    @Override
    public ExchangeRateModel update(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException {
        ExchangeRateEntity updatedExchangeRateEntity = exchangeRatesRepository.updateRateByBaseCurrencyCodeAndTargetCurrencyCode(
                baseCurrencyCode,
                targetCurrencyCode,
                rate);

        return exchangeRateModelMapper.map(updatedExchangeRateEntity);
    }
}
