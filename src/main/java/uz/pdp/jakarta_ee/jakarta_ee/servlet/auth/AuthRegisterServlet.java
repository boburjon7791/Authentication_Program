package uz.pdp.jakarta_ee.jakarta_ee.servlet.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.pdp.jakarta_ee.jakarta_ee.dao.AuthUserDAO;
import uz.pdp.jakarta_ee.jakarta_ee.dao.AuthUserOTPDAO;
import uz.pdp.jakarta_ee.jakarta_ee.dao.BaseDAO;
import uz.pdp.jakarta_ee.jakarta_ee.entity.AuthUser;
import uz.pdp.jakarta_ee.jakarta_ee.entity.AuthUserOTP;
import uz.pdp.jakarta_ee.jakarta_ee.services.MailtrapService;
import uz.pdp.jakarta_ee.jakarta_ee.utils.PasswordUtils;
import uz.pdp.jakarta_ee.jakarta_ee.utils.StringUtils;

import javax.mail.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(name = "AuthRegisterServlet", value = "/auth/register")
public class AuthRegisterServlet extends HttpServlet {

    private final AuthUserDAO authUserDAO = new AuthUserDAO();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/authuser/register.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirm_password = request.getParameter("confirm_password");

        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.validEmail(email)) {
//            errors.put("email_error", "Invalid email");
        }else {
            authUserDAO.findByEmail(email).ifPresent(
                    (authUser -> errors.put("email_error", "Email Already Taken")));
        }

        if (password == null) {
            errors.put("password_error", "Password is invalid");
            return;
        }
        if (!Objects.equals(password, confirm_password))
            errors.put("password_error", "Password is invalid");

        if (!errors.isEmpty()) {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/authuser/login.jsp");
            errors.forEach(request::setAttribute);
            dispatcher.forward(request, response);
        }

        AuthUser authUser = AuthUser
                .childBuilder()
                .email(email)
                .role("USER")
                .status(AuthUser.Status.IN_ACTIVE)
                .password(PasswordUtils.encode(password))
                .build();

        CompletableFuture.runAsync(() -> {
            AuthUserOTPDAO authUserOTPDAO = AuthUserOTPDAO.getInstance();
            AuthUserOTP authUserOTP = AuthUserOTP
                    .childBuilder()
                    .userID(authUser.getId())
                    .build();
            authUserOTPDAO.save(authUserOTP);
        });
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("library");
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(authUser);
        System.out.println(authUser.getId());
        authUser.getId();
        Cookie cookie = new Cookie("user-id",authUser.getId());
        MailtrapService.sendActivationEmail(authUser.getId(), authUser.getEmail());
        entityManager.getTransaction().commit();
        cookie.setMaxAge(120);
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
        response.sendRedirect("http://localhost:8080/views/authuser/confirm.jsp");
        emf.close();
    }
}
