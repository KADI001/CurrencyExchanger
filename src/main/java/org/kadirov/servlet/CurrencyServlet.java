package org.kadirov.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kadirov.dao.CurrencyRepository;
import org.kadirov.dao.entity.CurrencyEntity;
import org.kadirov.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext servletContext = getServletContext();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        CurrencyRepository currencyRepository =
                (CurrencyRepository) servletContext.getAttribute("currencyRepository");

        JSONReader<?> jsonReader =
                (JSONReader<?>) servletContext.getAttribute("jsonReader");

        String requestURI = req.getRequestURI();
        String[] splitURI = requestURI.split("/");

        if(!splitURI[splitURI.length - 2].equals("currency")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is no a currency code in the URL path");
            return;
        }

        String currencyCode = splitURI[splitURI.length - 1];
        CurrencyEntity target;

        try {
            target = currencyRepository.selectByCode(currencyCode);
        } catch (SQLException sqle) {
            logger.error("Couldn't execute findByCode for CurrencyRepository", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error of data base access");
            return;
        }

        if(target != null){
            PrintWriter writer = resp.getWriter();
            writer.write(jsonReader.fromObjectToString(target));
            writer.flush();
            writer.close();

            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            logger.warn("Couldn't find currency with code {}", currencyCode);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no any currency with that code");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
