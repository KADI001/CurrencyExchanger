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
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.ExchangeRatesRepository;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.model.ExchangeRateResponseMapper;
import org.kadirov.model.ErrorResponse;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.model.ExchangeRateResponse;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.kadirov.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesServlet.class);

    private CurrencyRepository currencyRepository;
    private ExchangeRatesRepository exchangeRatesRepository;
    private ExchangeRateResponseMapper exchangeRateResponseMapper;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        exchangeRatesRepository = (ExchangeRatesRepository) servletContext.getAttribute("exchangeRatesRepository");
        exchangeRateResponseMapper = (ExchangeRateResponseMapper) servletContext.getAttribute("exchangeRateResponseMapper");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateResponse> exchangeRateResponses;

        try {
            exchangeRateResponses = exchangeRatesRepository.selectAll().stream()
                    .map(exchangeRateResponseMapper::map)
                    .toList();
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectAll in exchangerates table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if(!exchangeRateResponses.isEmpty()){
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateResponses);
        }else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "There's no any exchange rate"));
        }
    }

    @Override
    @SuppressWarnings("all")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(HttpRequestUtil.extractBodyAsString(req));
        } catch (JsonProcessingException jpe){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Not valid json body"));
            return;
        }

        JsonNode baseCurrencyCodeNode = rootNode.get("baseCurrencyCode");
        JsonNode targetCurrencyCodeNode = rootNode.get("targetCurrencyCode");
        JsonNode rateNode = rootNode.get("rate");

        if(baseCurrencyCodeNode == null){
            logger.warn("Couldn't find required filed 'baseCurrencyCode' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: baseCurrencyCode"));
            return;
        }

        if(targetCurrencyCodeNode == null){
            logger.warn("Couldn't find required filed 'targetCurrencyCode' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: targetCurrencyCode"));
            return;
        }

        if(rateNode == null){
            logger.warn("Couldn't find required filed 'rate' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: rate"));
            return;
        }

        String baseCurrencyCode = baseCurrencyCodeNode.textValue();
        String targetCurrencyCode = targetCurrencyCodeNode.textValue();
        BigDecimal rate = rateNode.decimalValue();

        if(baseCurrencyCode == null){
            logger.warn("Couldn't convert parameter 'baseCurrencyCode' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field baseCurrencyCode in json is not a text type"));
            return;
        }

        if(targetCurrencyCode == null){
            logger.warn("Couldn't convert parameter 'targetCurrencyCode' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field targetCurrencyCode in json is not a text type"));
            return;
        }

        if(rate == null){
            logger.warn("Couldn't convert parameter 'rate' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field rate in json is not a text type"));
            return;
        }

        if(!CurrencyCodeUtil.exists(baseCurrencyCode)){
            logger.warn("The assigned request parameter 'baseCurrencyCode' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to baseCurrencyCode, doesn't exist"));
            return;
        }

        if(!CurrencyCodeUtil.exists(targetCurrencyCode)){
            logger.warn("The assigned request parameter 'targetCurrencyCode' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to targetCurrencyCode, doesn't exist"));
            return;
        }

        Optional<CurrencyEntity> optionalBaseCurrency;
        Optional<CurrencyEntity> optionalTargetCurrency;

        try {
            if (exchangeRatesRepository.existsByBaseCurrencyCodeAndTargetCurrencyCode(baseCurrencyCode, targetCurrencyCode)) {
                logger.warn("There is already one exchange rate with that baseCurrencyCode, targetCurrencyCode");
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(),
                        new ErrorResponse(HttpServletResponse.SC_CONFLICT, "There's already the exchange rate for those codes"));
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Error occurred during existsByBaseCurrencyCodeAndTargetCurrencyCode in exchangerates table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        try{
            optionalBaseCurrency = currencyRepository.selectByCode(baseCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectByCode from currencies table by baseCurrencyCode", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        try{
            optionalTargetCurrency = currencyRepository.selectByCode(targetCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectByCode from currencies table by targetCurrencyCode", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if(optionalBaseCurrency.isEmpty()){
            logger.warn("Couldn't find a currency with that baseCurrencyCode");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "There's no currency with that baseCurrencyCode"));
            return;
        }

        if(optionalTargetCurrency.isEmpty()){
            logger.warn("Couldn't find a currency with that targetCurrencyCode");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "There's no currency with that targetCurrencyCode"));
            return;
        }

        Optional<ExchangeRateEntity> addedExchangeRateEntity;

        //TODO: Сделать операцию добавления и получения exchange rate атамарнной
        //Transaction: start
        try{
            exchangeRatesRepository.insert(optionalBaseCurrency.get().getId(), optionalTargetCurrency.get().getId(), rate);

        } catch (SQLException sqle) {
            logger.error("Error occurred during insert into exchangerates table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        try{
            addedExchangeRateEntity = exchangeRatesRepository.selectByBaseCurrencyCodeAndTargetCurrencyCode(optionalBaseCurrency.get().getCode(),
                    optionalTargetCurrency.get().getCode());
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectByBaseCurrencyCodeAndTargetCurrencyCode from exchangerates table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }
        //Transaction: end

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), exchangeRateResponseMapper.map(addedExchangeRateEntity.get()));
    }
}
