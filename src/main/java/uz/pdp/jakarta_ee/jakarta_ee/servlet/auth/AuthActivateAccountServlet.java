package uz.pdp.jakarta_ee.jakarta_ee.servlet.auth;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import uz.pdp.jakarta_ee.jakarta_ee.dao.AuthUserDAO;
import uz.pdp.jakarta_ee.jakarta_ee.dao.AuthUserOTPDAO;
import uz.pdp.jakarta_ee.jakarta_ee.entity.AuthUser;
import uz.pdp.jakarta_ee.jakarta_ee.entity.AuthUserOTP;
import uz.pdp.jakarta_ee.jakarta_ee.utils.ConfirmationCodes;
import uz.pdp.jakarta_ee.jakarta_ee.utils.PasswordUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;

@WebServlet(name = "AuthActivateAccountServlet", value = "/activation")
public class AuthActivateAccountServlet extends HttpServlet {
    private final AuthUserDAO authUserDAO = new AuthUserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String token= request.getParameter("confirm");
            System.out.println(token);

        Optional<Cookie> first = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("user-id"))
                .findFirst();
        if (first.isPresent()) {
        }else {
            response.setStatus(400);
            return;
        }
        Hashtable<String, LocalDateTime> hashtable =
                ConfirmationCodes.confirmationCodes.get(first.get().getValue());
        System.out.println("receive confirm code");
        System.out.println(ConfirmationCodes.confirmationCodes);
        System.out.println(hashtable);
        LocalDateTime dateTime = hashtable.get(token);
        if (Objects.isNull(dateTime)) {
            response.setStatus(400);
            ConfirmationCodes.confirmationCodes.remove(first.get().getValue());
            System.out.println("when confirmation code incorrect");
            System.out.println(ConfirmationCodes.confirmationCodes);
            return;
        }
        if (dateTime.isAfter(LocalDateTime.now())) {
            response.setStatus(400);
            ConfirmationCodes.confirmationCodes.remove(first.get().getValue());
            System.out.println("after expired time");
            System.out.println(ConfirmationCodes.confirmationCodes);
            return;
        }
        AuthUser authUser = authUserDAO.findById(first.get().getValue());
        authUser.setStatus(AuthUser.Status.ACTIVE);
        authUserDAO.update(authUser);
        response.sendRedirect("/auth/login");
        ConfirmationCodes.confirmationCodes.remove(first.get().getValue());
        System.out.println("after confrimation");
        System.out.println(ConfirmationCodes.confirmationCodes);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        authUserDAO.findByEmail(email).ifPresentOrElse(authUser -> {
            if (!PasswordUtils.check(password, authUser.getPassword())) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/views/authuser/login.jsp");
                request.setAttribute("error_message", "Bad Credentials");
                try {
                    dispatcher.forward(request, response);
                } catch (ServletException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (!authUser.getStatus().equals(AuthUser.Status.ACTIVE)) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/views/authuser/login.jsp");
                request.setAttribute("error_message", "User not active");
                try {
                    dispatcher.forward(request, response);
                } catch (ServletException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String rememberMe = Objects.requireNonNullElse(request.getParameter("rememberMe"), "off");
                HttpSession session = request.getSession();
                session.setAttribute("email", authUser.getEmail());
                session.setAttribute("role", authUser.getRole());
                session.setAttribute("id", authUser.getId());
                if (rememberMe.equals("on")) {
                    Cookie cookie = new Cookie("rememberMe", authUser.getId());
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
                try {
                    String next = request.getParameter("next");
                    next = next == null || next.length() == 0 ? "/book/list" : next;
                    System.out.println("next = " + next);
                    response.sendRedirect(next);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, () -> {
            try {
                request.setAttribute("error_message", "Bad Credentials");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/views/authuser/login.jsp");
                dispatcher.forward(request, response);
            } catch (ServletException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
