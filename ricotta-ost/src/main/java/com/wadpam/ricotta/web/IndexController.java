package com.wadpam.ricotta.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.users.UserServiceFactory;
import com.wadpam.ricotta.velocity.Encoder;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
public class IndexController extends AbstractDaoController {
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String getIndex(Model model) {
        model.addAttribute("loginURL", UserServiceFactory.getUserService().createLoginURL("/index.html"));
        return "index";
    }

    @RequestMapping(value = "/logout.html", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        String logoutUrl = UserServiceFactory.getUserService().createLogoutURL("/loggedout.html");
        response.sendRedirect(logoutUrl);
    }

    @RequestMapping(value = "/loggedout.html", method = RequestMethod.GET)
    public String loggedout() {
        return "loggedout";
    }

    @RequestMapping(value = "/export.html", method = RequestMethod.GET)
    public String exportAll(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        model.put("uberDao", uberDao);

        TransController.renderTemplate("ricotta-export-all", model, response);
        return null;
    }

    @RequestMapping(value = "/exportOld.html", method = RequestMethod.GET)
    public String exportOld(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        model.put("uberDao", uberDao);

        TransController.renderTemplate("ricotta-export-old", model, response);
        return null;
    }

}
