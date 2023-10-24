package org.kadirov.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.entity.CurrencyEntity;
import org.kadirov.json.JSONObject;
import org.kadirov.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CurrenciesServlet.class);

    private CurrencyRepository currencyRepository;
    private JSONReader<?> jsonReader;

    @Override
    public void init(ServletConfig config) {
        ServletContext servletContext = config.getServletContext();

        currencyRepository = (CurrencyRepository) servletContext.getAttribute("currencyRepository");
        jsonReader = (JSONReader<?>) servletContext.getAttribute("jsonReader");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<CurrencyEntity> all = currencyRepository.selectAll();

            String jsonString = jsonReader.fromObjectToString(all);
            PrintWriter writer = resp.getWriter();
            writer.write(jsonString);
            writer.flush();
            writer.close();
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException sqle) {
            logger.error("Failed to selectAll for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder jsonData = new StringBuilder();
        BufferedReader reader = req.getReader();

        String temp;

        while ((temp = reader.readLine()) != null)
            jsonData.append(temp);

        JSONObject<?> targetJsonData = jsonReader.parse(jsonData.toString());

        String code = targetJsonData.getAsText("code");
        String fullName = targetJsonData.getAsText("fullName");
        String sign = targetJsonData.getAsText("sign");

        if(code == null || fullName == null || sign == null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You've missed required field of the currency");
            return;
        }

        CurrencyEntity savedCurrencyModel;

        try {
            if(currencyRepository.existsByCode(code)){
                resp.sendError(HttpServletResponse.SC_CONFLICT, "There is the currency with that code.");
                return;
            }
        } catch (SQLException sqle) {
            logger.error("Couldn't execute existsByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error of data base access");
            return;
        }

        try {
            savedCurrencyModel = currencyRepository.insert(new CurrencyEntity(code, fullName, sign));
        } catch (SQLException sqle) {
            logger.error("Couldn't execute insert for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error of data base access");
            return;
        }

        String jsonString = jsonReader.fromObjectToString(savedCurrencyModel);
        PrintWriter writer = resp.getWriter();
        writer.write(jsonString);
        writer.flush();
        writer.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
