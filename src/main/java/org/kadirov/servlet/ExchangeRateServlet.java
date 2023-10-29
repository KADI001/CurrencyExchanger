package org.kadirov.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.model.ErrorModel;
import org.kadirov.model.ExchangeRateModel;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.service.exception.CurrencyCodeValidationException;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServlet.class);

    private ExchangeRateService exchangeRateService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();

        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("dbExchangeRateService");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if (!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "There is no any code pair in the URL path"));
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if (codePair.length() != 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong"));
            return;
        }

        String baseCurrencyCode = codePair.substring(0, 3);
        String targetCurrencyCode = codePair.substring(3);

        if(!CurrencyCodeUtil.exists(baseCurrencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to baseCurrencyCode, doesn't exist"));
            return;
        }

        if(!CurrencyCodeUtil.exists(targetCurrencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to targetCurrencyCode, doesn't exist"));
            return;
        }

        Optional<ExchangeRateModel> exchangeRateModel;

        try {
            exchangeRateModel = exchangeRateService.getDirectExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if (exchangeRateModel.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateModel.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorModel(HttpServletResponse.SC_NOT_FOUND, "Failed to find the exchange rate for currencies with those codes"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if (!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "There is no any code pair in the URL path"));
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if (codePair.length() != 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong"));
            return;
        }

        String baseCurrencyCode = codePair.substring(0, 3);
        String targetCurrencyCode = codePair.substring(3);

        if(!CurrencyCodeUtil.exists(baseCurrencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to baseCurrencyCode, doesn't exist"));
            return;
        }

        if(!CurrencyCodeUtil.exists(targetCurrencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to targetCurrencyCode, doesn't exist"));
            return;
        }

        StringBuilder requestJsonBodyContent = new StringBuilder();
        BufferedReader reader = req.getReader();

        String temp;
        while ((temp = reader.readLine()) != null)
            requestJsonBodyContent.append(temp);

        JsonNode rootNode = objectMapper.readTree(requestJsonBodyContent.toString());

        JsonNode rateNode = rootNode.get("rate");

        if (rateNode.isNull()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "You don't specify 'rate' field"));
            return;
        }

        BigDecimal rate = rateNode.decimalValue();
        ExchangeRateModel updatedExchangeRateModel;

        try {
            if (!exchangeRateService.existsExchangeRateByBaseCodeAndTargetCode(baseCurrencyCode, targetCurrencyCode)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(),
                        new ErrorModel(HttpServletResponse.SC_CONFLICT, "There's no one the exchange rate for those currencies"));
                return;
            }

            updatedExchangeRateModel = exchangeRateService.update(baseCurrencyCode, targetCurrencyCode, rate);

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), updatedExchangeRateModel);
    }
}
