package org.kadirov.dao;

import org.kadirov.entity.CurrencyEntity;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {
    List<CurrencyEntity> selectAll() throws SQLException;
    Optional<CurrencyEntity> selectById(int id) throws SQLException;
    Optional<CurrencyEntity> selectByCode(String code) throws SQLException;
    CurrencyEntity insert(final CurrencyEntity currencyModel) throws SQLException;
    boolean existsByCode(String code) throws SQLException;
}
