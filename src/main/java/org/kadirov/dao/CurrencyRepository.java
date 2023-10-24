package org.kadirov.dao;

import org.kadirov.dao.entity.CurrencyEntity;

import java.sql.SQLException;
import java.util.List;

public interface CurrencyRepository {
    List<CurrencyEntity> selectAll() throws SQLException;
    CurrencyEntity selectById(int id) throws SQLException;
    CurrencyEntity selectByCode(String code) throws SQLException;
    CurrencyEntity insert(final CurrencyEntity currencyModel) throws SQLException;
    boolean existsByCode(String code) throws SQLException;
}
