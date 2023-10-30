package org.kadirov.servlet;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.mapper.model.CurrencyResponseMapper;
import org.kadirov.mapper.model.ExchangeRateResponseMapper;
import org.kadirov.model.ErrorResponse;
import org.kadirov.model.ExchangeRateResponse;
import org.kadirov.model.ExchangeResponse;
import org.kadirov.entity.ExchangeRateEntity;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.util.DBExceptionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeServlet.class);

    private ObjectMapper objectMapper;
    private ExchangeRateService exchangeRateService;
    private ExchangeRateResponseMapper exchangeRateResponseMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
        exchangeRateResponseMapper = new ExchangeRateResponseMapper(new CurrencyResponseMapper());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fromCurrencyCode = req.getParameter("from");
        String toCurrencyCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (fromCurrencyCode == null || fromCurrencyCode.isBlank()) {
            logger.warn("The request url parameter 'from' is null or blank");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: from"));
            return;
        }

        if (toCurrencyCode == null || toCurrencyCode.isBlank()) {
            logger.warn("The request url parameter 'to' is null or blank");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: to"));
            return;
        }

        if (amount == null || amount.isBlank()) {
            logger.warn("The request url parameter 'amount' is null or blank");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: amount"));
            return;
        }

        BigDecimal parsedAmount;

        try {
            parsedAmount = BigDecimalParser.parse(amount);
        } catch (Exception e) {
            logger.error("Failed to parse parameter 'amount' to BigDecimal");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Parameter amount is not valid"));
            return;
        }

        Optional<ExchangeRateEntity> optionalExchangeRateModel;

        try {
            optionalExchangeRateModel = exchangeRateService.getExchangeRateByCode(fromCurrencyCode, toCurrencyCode);
        } catch (SQLException sqle) {
            logger.error("Error occurred during getExchangeRateByCode in getExchangeRateByCode", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if (optionalExchangeRateModel.isPresent()) {
            ExchangeRateEntity exchangeRateEntity = optionalExchangeRateModel.get();
            BigDecimal convertedAmount;
            convertedAmount = parsedAmount.multiply(exchangeRateEntity.getRate());

            ExchangeRateResponse optionalExchangeRateDTO = exchangeRateResponseMapper.map(exchangeRateEntity);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(),
                    new ExchangeResponse(optionalExchangeRateDTO.baseCurrency(), optionalExchangeRateDTO.targetCurrency(), exchangeRateEntity.getRate(), parsedAmount, convertedAmount));
        } else {
            logger.error("There is no way to make exchange for that from, to currency codes");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Couldn't find an exchange rate for that currencies"));
        }
    }
}
