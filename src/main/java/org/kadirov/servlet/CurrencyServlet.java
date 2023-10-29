package org.kadirov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.entity.CurrencyEntity;
import org.kadirov.model.CurrencyModel;
import org.kadirov.model.ErrorModel;
import org.kadirov.service.CurrencyService;
import org.kadirov.service.exception.CurrencyCodeValidationException;
import org.kadirov.util.CurrencyCodeUtil;
import org.kadirov.util.DBExceptionMessages;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();

        currencyService = (CurrencyService) servletContext.getAttribute("dbCurrencyService");
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String[] splitURI = requestURI.split("/");

        if(!splitURI[splitURI.length - 2].equals("currency")){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_BAD_REQUEST, "There's no currency code in the path"));
            return;
        }

        String currencyCode = splitURI[splitURI.length - 1];

        if(!CurrencyCodeUtil.exists(currencyCode)){
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_CONFLICT, "The currency code, that you set to currencyCode, doesn't exist"));
            return;
        }

        Optional<CurrencyModel> optionalCurrency;

        try {
            optionalCurrency = currencyService.getByCode(currencyCode);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorModel(HttpServletResponse.SC_CONFLICT, DBExceptionMessages.TROUBLE));
            return;
        }

        if(optionalCurrency.isPresent()){
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), optionalCurrency.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorModel(HttpServletResponse.SC_NOT_FOUND, "There is no any currency with that code"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
