package org.kadirov.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.dao.entity.CurrencyEntity;
import org.kadirov.dao.entity.ExchangeRateEntity;
import org.kadirov.json.JSONObject;
import org.kadirov.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesServlet.class);

    private ExchangeRatesRepository exchangeRatesRepository;
    private CurrencyRepository currencyRepository;
    private JSONReader<?> jsonReader;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext servletContext = config.getServletContext();

        exchangeRatesRepository = (ExchangeRatesRepository) servletContext.getAttribute("exchangeRatesRepository");
        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        jsonReader = (JSONReader<?>) servletContext.getAttribute("jsonReader");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<ExchangeRateEntity> exchangeRateEntities = exchangeRatesRepository.selectAll();

            String jsonString = jsonReader.fromObjectToString(exchangeRateEntities);
            PrintWriter writer = resp.getWriter();
            writer.write(jsonString);
            writer.flush();
            writer.close();
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException sqle) {
            logger.error("Failed to selectAll for exchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder jsonData = new StringBuilder();
        BufferedReader reader = req.getReader();

        String temp;
        while ((temp = reader.readLine()) != null)
            jsonData.append(temp);

        JSONObject<?> targetJsonData = jsonReader.parse(jsonData.toString());

        String baseCurrencyCode = targetJsonData.getAsText("baseCurrencyCode");
        String targetCurrencyCode = targetJsonData.getAsText("targetCurrencyCode");
        BigDecimal rate = targetJsonData.getAsBigDecimal("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You've missed required field of the exchange rate");
            return;
        }

        try {
            if (!currencyRepository.existsByCode(baseCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no any currency with that baseCurrencyCode");
                return;
            }

            if (!currencyRepository.existsByCode(targetCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no any currency with that targetCurrencyCode");
                return;
            }

        } catch (SQLException sqle) {
            logger.error("Failed to existsByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {
            if (exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "There is already the exchange rate with that baseCurrencyCode and targetCurrencyCode");
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Failed to existsByBaseCurrencyCodeAndTargetCurrencyCode for ExchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        CurrencyEntity baseCurrencyEntity;
        CurrencyEntity targetCurrencyEntity;

        try {
            baseCurrencyEntity = currencyRepository.selectByCode(baseCurrencyCode);
            targetCurrencyEntity = currencyRepository.selectByCode(targetCurrencyCode);

        } catch (SQLException sqle) {
            logger.error("Failed to selectByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        ExchangeRateEntity savedExchangeRate;

        try {
            savedExchangeRate = exchangeRatesRepository.insert(new ExchangeRateEntity(baseCurrencyEntity, targetCurrencyEntity, rate));
        } catch (SQLException sqle) {
            logger.error("Failed to insert for ExchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String jsonString = jsonReader.fromObjectToString(savedExchangeRate);
        PrintWriter writer = resp.getWriter();
        writer.write(jsonString);
        writer.flush();
        writer.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
