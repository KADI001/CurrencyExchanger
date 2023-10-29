package org.kadirov.servlet;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.model.ErrorModel;
import org.kadirov.model.ExchangeModel;
import org.kadirov.model.ExchangeRateModel;
import org.kadirov.service.ExchangeRateService;
import org.kadirov.util.DBExceptionMessages;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ObjectMapper jsonReader;
    private ExchangeRateService exchangeRateService;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        exchangeRateService = (ExchangeRateService) servletContext.getAttribute("exchangeRateService");
        jsonReader = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fromCurrencyCode = req.getParameter("from");
        String toCurrencyCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (fromCurrencyCode == null || fromCurrencyCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonReader.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: from"));
            return;
        }

        if (toCurrencyCode == null || toCurrencyCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonReader.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: to"));
            return;
        }

        if (amount == null || amount.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonReader.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: amount"));
            return;
        }

        BigDecimal parsedAmount;

        try {
            parsedAmount = BigDecimalParser.parse(amount);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonReader.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Parameter amount is not valid"));
            return;
        }

        Optional<ExchangeRateModel> optionalExchangeRateModel;

        try {
            optionalExchangeRateModel = exchangeRateService.getExchangeRateByCode(fromCurrencyCode, toCurrencyCode);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonReader.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        if (optionalExchangeRateModel.isPresent()) {
            ExchangeRateModel exchangeRate = optionalExchangeRateModel.get();
            BigDecimal convertedAmount;
            convertedAmount = parsedAmount.multiply(exchangeRate.rate());

            resp.setStatus(HttpServletResponse.SC_OK);
            jsonReader.writeValue(resp.getWriter(),
                    new ExchangeModel(exchangeRate.baseCurrency(), exchangeRate.targetCurrency(), exchangeRate.rate(), parsedAmount, convertedAmount));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonReader.writeValue(resp.getWriter(),
                    new ErrorModel(HttpServletResponse.SC_NOT_FOUND, "Couldn't find an exchange rate for that currencies"));
        }
    }
}
