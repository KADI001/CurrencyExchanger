package org.kadirov.service;

import org.kadirov.dao.CurrencyRepository;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.model.CurrencyModelMapper;
import org.kadirov.model.CurrencyModel;
import org.kadirov.service.exception.CurrencyCodeValidationException;
import org.kadirov.util.CurrencyCodeUtil;

import java.sql.SQLException;
import java.util.*;

public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyModelMapper currencyModelMapper;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
        this.currencyModelMapper = new CurrencyModelMapper();
    }

    @Override
    public Optional<CurrencyModel> getByCode(String code) throws SQLException {
        CurrencyModel result = null;

        Optional<CurrencyEntity> optionalCurrencyEntity = currencyRepository.selectByCode(code);

        if(optionalCurrencyEntity.isPresent())
            result = currencyModelMapper.map(optionalCurrencyEntity.get());

        return Optional.ofNullable(result);
    }

    @Override
    public List<CurrencyModel> getAll() throws SQLException {
        List<CurrencyModel> currencyModels = new ArrayList<>();

        List<CurrencyEntity> currencyEntities = currencyRepository.selectAll();

        for (CurrencyEntity currencyEntity : currencyEntities) {
            currencyModels.add(currencyModelMapper.map(currencyEntity));
        }

        return currencyModels;
    }

    @Override
    public CurrencyModel add(final CurrencyModel currencyModel) throws SQLException {
        return  currencyModelMapper.map(currencyRepository.insert(new CurrencyEntity(currencyModel.code(), currencyModel.fullName(), currencyModel.sign())));
    }

    @Override
    public boolean existsByCode(String code) throws SQLException {
        return currencyRepository.existsByCode(code);
    }

    private static boolean validate(String code) {
        return CurrencyCodeUtil.exists(code);
    }
}
