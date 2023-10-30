package org.kadirov.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.mapper.model.ExchangeRateResponseMapper;
import org.kadirov.model.ErrorResponse;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.kadirov.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServlet.class);

    private ExchangeRateService exchangeRateService;
    private ExchangeRatesRepository exchangeRatesRepository;
    private ExchangeRateResponseMapper exchangeRateResponseMapper;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("dbExchangeRateService");
        exchangeRatesRepository = (ExchangeRatesRepository) servletContext.getAttribute("exchangeRatesRepository");
        exchangeRateResponseMapper = (ExchangeRateResponseMapper) servletContext.getAttribute("exchangeRateResponseMapper");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if (!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")) {
            logger.warn("The request path is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The request path is not valid"));
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if (codePair.length() != 6) {
            logger.warn("The currency pair is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong"));
            return;
        }

        String baseCurrencyCode = codePair.substring(0, 3);
        String targetCurrencyCode = codePair.substring(3);

        if(!CurrencyCodeUtil.exists(baseCurrencyCode)){
            logger.warn("The assigned request parameter 'base currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to baseCurrencyCode, doesn't exist"));
            return;
        }

        if(!CurrencyCodeUtil.exists(targetCurrencyCode)){
            logger.warn("The assigned request parameter 'target currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to targetCurrencyCode, doesn't exist"));
            return;
        }

        Optional<ExchangeRateEntity> exchangeRateModel;

        try {
            exchangeRateModel = exchangeRateService.getDirectExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Error occurred during getDirectExchangeRateByCode in exchangeRateService", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if (exchangeRateModel.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateResponseMapper.map(exchangeRateModel.get()));
        } else {
            logger.warn("There is no any exchange rate");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Failed to find the exchange rate for currencies with those codes"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String[] splittedRequestURI = requestURI.split("/");

        if (!splittedRequestURI[splittedRequestURI.length - 2].equals("exchangeRate")) {
            logger.warn("The request path is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "There is no any code pair in the URL path"));
            return;
        }

        String codePair = splittedRequestURI[splittedRequestURI.length - 1];

        if (codePair.length() != 6) {
            logger.warn("The currency pair is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The code pair in the URL path is wrong"));
            return;
        }

        String baseCurrencyCode = codePair.substring(0, 3);
        String targetCurrencyCode = codePair.substring(3);

        if(!CurrencyCodeUtil.exists(baseCurrencyCode)){
            logger.warn("The assigned request parameter 'base currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to baseCurrencyCode, doesn't exist"));
            return;
        }

        if(!CurrencyCodeUtil.exists(targetCurrencyCode)){
            logger.warn("The assigned request parameter 'target currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to targetCurrencyCode, doesn't exist"));
            return;
        }

        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(HttpRequestUtil.extractBodyAsString(req));
        } catch (JsonProcessingException jpe){
            logger.error("Failed to parse request body content into json node", jpe);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Not valid json body"));
            return;
        }

        JsonNode rateNode = rootNode.get("rate");

        if (rateNode == null) {
            logger.warn("Couldn't find required filed 'rate' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "You don't specify 'rate' field"));
            return;
        }

        BigDecimal rate = rateNode.decimalValue();

        if(rate.equals(BigDecimal.ZERO)){
            logger.warn("The filed 'rate' in json is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The 'rate' field in json body is not valid"));
            return;
        }

        try {
            if (!exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
                logger.warn("There is no one exchange rate with that base currency code, target currency code");
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(),
                        new ErrorResponse(HttpServletResponse.SC_CONFLICT, "There's no one the exchange rate for those currencies"));
                return;
            }

            ExchangeRateEntity exchangeRateEntity =
                    exchangeRatesRepository.updateRateByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode, rate);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateResponseMapper.map(exchangeRateEntity));
        } catch (SQLException sqle) {
            logger.error("Error occurred during updateRateByBaseCurrencyCodeAndTargetCurrencyCode in exchangerates table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
        }
    }
}
