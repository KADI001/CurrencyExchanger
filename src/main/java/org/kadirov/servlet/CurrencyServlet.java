package org.kadirov.servlet;

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
import org.kadirov.model.ErrorResponse;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServlet.class);

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
        String requestURI = req.getRequestURI();
        String[] splitURI = requestURI.split("/");

        if(!splitURI[splitURI.length - 2].equals("currency")){
            logger.warn("The request path is not valid");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "The request path is not valid"));
            return;
        }

        String currencyCode = splitURI[splitURI.length - 1];

        if(currencyCode.length() != 3 || !currencyCode.equals(currencyCode.toUpperCase())){
            logger.warn("The request parameter 'currencyCode' is not valid");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The currency code is not valid"));
            return;
        }

        if(!CurrencyCodeUtil.exists(currencyCode)){
            logger.warn("The assigned request parameter 'currency code' doesn't exist");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "The currency code, that you set to currencyCode, doesn't exist"));
            return;
        }

        Optional<CurrencyEntity> optionalCurrencyEntity;

        try {
            optionalCurrencyEntity = currencyRepository.selectByCode(currencyCode);
        } catch (SQLException sqle) {
            logger.error("Error occurred during selectByCode from currencies table", sqle);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse(HttpServletResponse.SC_CONFLICT, DBExceptionMessages.TROUBLE));
            return;
        }

        if(optionalCurrencyEntity.isPresent()){
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), currencyResponseMapper.map(optionalCurrencyEntity.get()));
        } else {
            logger.warn("Couldn't find the currency with that code");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "There is no any currency with that code"));
        }
    }
}
