package org.kadirov.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.model.CurrencyModel;
import org.kadirov.model.ErrorModel;
import org.kadirov.service.CurrencyService;
import org.kadirov.service.exception.CurrencyCodeValidationException;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.kadirov.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        currencyService = (CurrencyService) servletContext.getAttribute("dbCurrencyService");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<CurrencyModel> allCurrencies;

        try {
            allCurrencies = currencyService.getAll();
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), allCurrencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(HttpRequestUtil.extractBodyAsString(req));

        JsonNode fullNameNode = jsonNode.get("fullName");
        JsonNode codeNode = jsonNode.get("code");
        JsonNode signNode = jsonNode.get("sign");

        if(fullNameNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: fullName"));
            return;
        }

        if(codeNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: code"));
            return;
        }

        if(signNode.isNull()){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: sign"));
            return;
        }

        String fullName = fullNameNode.textValue();
        String currencyCode = codeNode.textValue();
        String sign = signNode.textValue();

        if(fullName == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field fullName is not a text type"));
            return;
        }

        if(currencyCode == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field code is not a text type"));
            return;
        }

        if(sign == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "Field sign is not a text type"));
            return;
        }

        if(!CurrencyCodeUtil.exists(currencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to currencyCode, doesn't exist"));
            return;
        }

        try {
            if(currencyService.existsByCode(currencyCode)){
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "There's already the currency with that code"));
                return;
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        CurrencyModel currencyModel = new CurrencyModel(fullName, currencyCode, sign);
        CurrencyModel addedCurrencyModel;

        try {
            addedCurrencyModel = currencyService.add(currencyModel);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), addedCurrencyModel);
    }
}
