package org.kadirov.servlet;

import com.fasterxml.jackson.core.io.BigDecimalParser;
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
import org.kadirov.json.JSONReader;
import org.kadirov.model.ExchangeModel;
import org.kadirov.view.mapper.ExchangeViewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeServlet.class);

    private ExchangeRatesRepository exchangeRatesRepository;
    private CurrencyRepository currencyRepository;
    private JSONReader<?> jsonReader;
    private ExchangeViewMapper exchangeViewMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        exchangeRatesRepository = (ExchangeRatesRepository) servletContext.getAttribute("exchangeRatesRepository");
        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        jsonReader = (JSONReader<?>) servletContext.getAttribute("jsonReader");
        exchangeViewMapper = new ExchangeViewMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String fromCurrencyCode = req.getParameter("from");
        String toCurrencyCode = req.getParameter("to");
        BigDecimal amount;

        try {
            amount = BigDecimalParser.parse(req.getParameter("amount"));
        } catch (Exception e) {
            logger.error("Failed to parse the amount param to BigDecimal", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {
            if (!currencyRepository.existsByCode(fromCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no currency for from");
                return;
            }

            if (!currencyRepository.existsByCode(toCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no currency for to");
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Failed to existsByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {
            if (!exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(fromCurrencyCode, toCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no any exchange rate with that baseCurrencyCode and targetCurrencyCode");
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Failed to existsByBaseCurrencyCodeAndTargetCurrencyCode for ExchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        CurrencyEntity baseCurrency;
        CurrencyEntity targetCurrency;

        try {
            baseCurrency = currencyRepository.selectByCode(fromCurrencyCode);
            targetCurrency = currencyRepository.selectByCode(toCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Failed to selectByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        ExchangeRateEntity exchangeRateEntity;

        try {
            exchangeRateEntity = exchangeRatesRepository.selectByBaseCurrencyCodeAndTargetCurrencyCode(fromCurrencyCode, toCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Failed to selectByBaseCurrencyCodeAndTargetCurrencyCode for ExchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        BigDecimal convertedAmount = amount.multiply(exchangeRateEntity.getRate());

        ExchangeModel exchangeModel = new ExchangeModel(baseCurrency, targetCurrency, exchangeRateEntity.getRate(), amount, convertedAmount);

        String jsonString = jsonReader.fromObjectToString(exchangeViewMapper.map(exchangeModel));
        PrintWriter writer = resp.getWriter();
        writer.write(jsonString);
        writer.flush();
        writer.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
