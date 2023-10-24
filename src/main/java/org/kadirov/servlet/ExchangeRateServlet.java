package org.kadirov.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.ExchangeRatesRepository;
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

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServlet.class);

    private ExchangeRatesRepository exchangeRatesRepository;
    private JSONReader<?> jsonReader;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext servletContext = config.getServletContext();

        exchangeRatesRepository = (ExchangeRatesRepository) servletContext.getAttribute("exchangeRatesRepository");
        jsonReader = (JSONReader<?>) servletContext.getAttribute("jsonReader");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if(!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no any code pair in the URL path");
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if(codePair.length() != 6){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong");
            return;
        }

        String code1 = codePair.substring(0, 3);
        String code2 = codePair.substring(3);

        ExchangeRateEntity exchangeRateEntity;

        try {
             exchangeRateEntity = exchangeRatesRepository.selectByBaseCurrencyCodeAndTargetCurrencyCode(code1, code2);
        } catch (SQLException sqle) {
            logger.error("Failed to selectAll for exchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if(exchangeRateEntity != null){
            String jsonString = jsonReader.fromObjectToString(exchangeRateEntity);
            PrintWriter writer = resp.getWriter();
            writer.write(jsonString);
            writer.flush();
            writer.close();
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder jsonData = new StringBuilder();
        BufferedReader reader = req.getReader();

        String temp;
        while ((temp = reader.readLine()) != null)
            jsonData.append(temp);

        JSONObject<?> targetJsonData = jsonReader.parse(jsonData.toString());

        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if(!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no any code pair in the URL path");
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if(codePair.length() != 6){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong");
            return;
        }

        String baseCurrencyCode = codePair.substring(0, 3);
        String targetCurrencyCode = codePair.substring(3);
        BigDecimal rate = targetJsonData.getAsBigDecimal("rate");

        try {
            if (!exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "There is no the exchange rate with that baseCurrencyCode and targetCurrencyCode");
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Failed to existsByBaseCurrencyCodeAndTargetCurrencyCode for exchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        ExchangeRateEntity updatedExchangeRateEntity;

        try {
             updatedExchangeRateEntity =
                    exchangeRatesRepository.updateRateByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode, rate);
        } catch (SQLException sqle) {
            logger.error("Failed to updateRateByBaseCurrencyCodeAndTargetCurrencyCode for exchangeRatesRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String jsonString = jsonReader.fromObjectToString(updatedExchangeRateEntity);
        PrintWriter writer = resp.getWriter();
        writer.write(jsonString);
        writer.flush();
        writer.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
