package org.kadirov.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.model.CurrencyModel;
import org.kadirov.model.ErrorModel;
import org.kadirov.model.ExchangeRateModel;
import org.kadirov.service.CurrencyService;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.kadirov.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesServlet.class);

    private ExchangeRateService exchangeRateService;
    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");
        currencyService = (CurrencyService) servletContext.getAttribute("currencyService");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateModel> allExchangeRates;

        try {
            allExchangeRates = exchangeRateService.getAll();
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if (allExchangeRates.size() != 0) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), allExchangeRates);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_NOT_FOUND, "There's no one exchange rate"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode rootNode = objectMapper.readTree(HttpRequestUtil.extractBodyAsString(req));

        JsonNode baseCurrencyCodeNode = rootNode.get("baseCurrencyCode");
        JsonNode targetCurrencyCodeNode = rootNode.get("targetCurrencyCode");
        JsonNode rateNode = rootNode.get("rate");

        if(baseCurrencyCodeNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: baseCurrencyCode"));
            return;
        }

        if(targetCurrencyCodeNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: targetCurrencyCode"));
            return;
        }

        if(rateNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing field in json: rate"));
            return;
        }

        String baseCurrencyCode = baseCurrencyCodeNode.textValue();
        String targetCurrencyCode = targetCurrencyCodeNode.textValue();
        BigDecimal rate = rateNode.decimalValue();

        if(baseCurrencyCode == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field baseCurrencyCode in json is not a text type"));
            return;
        }

        if(targetCurrencyCode == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field targetCurrencyCode in json is not a text type"));
            return;
        }

        if(rate == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field rate in json is not a text type"));
            return;
        }

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

        Optional<CurrencyModel> optionalBaseCurrency;
        Optional<CurrencyModel> optionalTargetCurrency;

        try {
            if (exchangeRateService.existsExchangeRateByBaseCodeAndTargetCode(baseCurrencyCode, targetCurrencyCode)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(),
                        new ErrorModel(HttpServletResponse.SC_CONFLICT, "There's already the exchange rate for those codes"));
                return;
            }

            optionalBaseCurrency = currencyService.getByCode(baseCurrencyCode);
            optionalTargetCurrency = currencyService.getByCode(targetCurrencyCode);

            if(optionalBaseCurrency.isEmpty()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "There's no currency with that baseCurrencyCode"));
                return;
            }

            if(optionalTargetCurrency.isEmpty()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "There's no currency with that targetCurrencyCode"));
                return;
            }

            ExchangeRateModel exchangeRateModel = new ExchangeRateModel(optionalBaseCurrency.get(), optionalTargetCurrency.get(), rate);
            exchangeRateService.add(exchangeRateModel);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), exchangeRateModel);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
        }
    }
}
