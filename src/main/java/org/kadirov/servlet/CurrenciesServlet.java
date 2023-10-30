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
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.mapper.model.CurrencyResponseMapper;
import org.kadirov.model.CurrencyResponse;
import org.kadirov.model.ErrorResponse;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.kadirov.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CurrenciesServlet.class);

    private CurrencyRepository currencyRepository;
    private ObjectMapper objectMapper;

    private CurrencyResponseMapper currencyResponseMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
        currencyResponseMapper = (CurrencyResponseMapper) servletContext.getAttribute("currencyResponseMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<CurrencyResponse> currencyViews = new ArrayList<>();

        try {
            List<CurrencyEntity> currencyEntities = currencyRepository.selectAll();

            if(!currencyEntities.isEmpty()){
                currencyEntities.forEach(currencyEntity -> currencyViews.add(currencyResponseMapper.map(currencyEntity)));
            }else {
                logger.warn("Couldn't find any currency in database");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        HttpServletResponse.SC_NOT_FOUND, "There's no any currency"));
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectAll from currencies table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), currencyViews);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(HttpRequestUtil.extractBodyAsString(req));
        } catch (JsonProcessingException jpe){
            logger.error("Failed to parse request body content into json node", jpe);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Not valid json body"));
            return;
        }

        JsonNode fullNameNode = rootNode.get("name");
        JsonNode codeNode = rootNode.get("code");
        JsonNode signNode = rootNode.get("sign");

        if(fullNameNode == null){
            logger.warn("Couldn't find required filed 'name' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: name"));
            return;
        }

        if(codeNode == null){
            logger.warn("Couldn't find required filed 'code' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: code"));
            return;
        }

        if(signNode == null){
            logger.warn("Couldn't find required filed 'sign' in json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Missing filed: sign"));
            return;
        }

        String fullName = fullNameNode.textValue();
        String currencyCode = codeNode.textValue();
        String sign = signNode.textValue();

        if(fullName == null){
            logger.warn("Couldn't convert parameter 'name' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field name is not a text type"));
            return;
        }

        if(currencyCode == null){
            logger.warn("Couldn't convert parameter 'code' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field code is not a text type"));
            return;
        }

        if(sign == null){
            logger.warn("Couldn't convert parameter 'sign' to text value");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Field sign is not a text type"));
            return;
        }

        if(!CurrencyCodeUtil.exists(currencyCode)){
            logger.warn("The assigned request parameter 'currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to currencyCode, doesn't exist"));
            return;
        }

        try {
            if(currencyRepository.existsByCode(currencyCode)){
                logger.warn("The currency with that code already exists in data base");
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_CONFLICT, "There's already the currency with that code"));
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Error occurred during existsByCode in currencies table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
            return;
        }

        CurrencyEntity currencyEntity = new CurrencyEntity(currencyCode, fullName, sign);

        try {
            CurrencyEntity addedCurrencyEntity = currencyRepository.insert(currencyEntity);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), currencyResponseMapper.map(addedCurrencyEntity));
        } catch (SQLException sqle) {
            logger.error("Error occurred during insert into currencies table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DBExceptionMessages.TROUBLE));
        }
    }
}
