package org.kadirov.service;

import org.kadirov.model.CurrencyModel;
import org.kadirov.service.exception.CurrencyCodeValidationException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CurrencyService {
    Optional<CurrencyModel> getByCode(String code) throws SQLException;
    List<CurrencyModel> getAll() throws SQLException;
    CurrencyModel add(final CurrencyModel currencyModel) throws SQLException;
    boolean existsByCode(String code) throws SQLException;
}
