package org.kadirov.dao.entity.mapper;

import org.kadirov.dao.entity.CurrencyEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMapper {
    public CurrencyEntity map(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String code = resultSet.getString("code");
        String fullName = resultSet.getString("full_name");
        String sign = resultSet.getString("sign");

        return new CurrencyEntity(id, code, fullName, sign);
    }
}
