package org.kadirov.mapper.entity;

import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.Mapper;
import org.kadirov.mapper.exception.MappingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMapper implements Mapper<ResultSet, CurrencyEntity> {
    @Override
    public CurrencyEntity map(ResultSet resultSet) throws MappingException {
        CurrencyEntity currencyEntity;
        int id;

        try {
            id = resultSet.getInt("id");
            String code = resultSet.getString("code");
            String fullName = resultSet.getString("full_name");
            String sign = resultSet.getString("sign");

            currencyEntity = new CurrencyEntity(id, code, fullName, sign);
        } catch (SQLException sqle) {
            throw new MappingException(sqle);
        }

        return currencyEntity;
    }
}
