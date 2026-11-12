package co.umariana.servlets;

import co.umariana.dao.EstudianteDAO;
import co.umariana.modelo.Estudiante;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/estudiantes")
public class EstudianteServlet extends HttpServlet {

    static final String VISTA_LISTAR = "/WEB-INF/vistas/listar.jsp";
    static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario.jsp";

    private final EstudianteDAO dao = new EstudianteDAO();

    static String validar(String nombre, String correo, String semestre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (correo == null || !correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo no es válido";
        }
        try {
            int s = Integer.parseInt(semestre.trim());
            if (s < 1 || s > 10) {
                return "El semestre debe ser un número entre 1 y 10";
            }
        } catch (NumberFormatException | NullPointerException e) {
            return "El semestre debe ser un número entre 1 y 10";
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if (accion == null || "listar".equals(accion)) {
            request.setAttribute("estudiantes", dao.listar());
            request.getRequestDispatcher(VISTA_LISTAR).forward(request, response);
            return;
        }

        if ("editar".equals(accion)) {
            try {
                int id = Integer.parseInt(request.getParameter("id").trim());
                Estudiante estudiante = dao.buscarPorId(id);
                if (estudiante == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("estudiante", estudiante);
                request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("crear".equals(accion)) {
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");

            String error = validar(nombre, correo, semestre);
            if (error == null) {
                Estudiante estudiante = new Estudiante(0, nombre.trim(), correo.trim(),
                                                       Integer.parseInt(semestre.trim()));
                if (dao.registrar(estudiante)) {
                    response.sendRedirect("estudiantes");
                    return;
                }
                error = "No se pudo guardar el estudiante. Verifique que el correo no esté repetido.";
            }
            request.setAttribute("error", error);
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if ("actualizar".equals(accion)) {
            int id;
            Estudiante existente;
            try {
                id = Integer.parseInt(request.getParameter("id").trim());
                existente = dao.buscarPorId(id);
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (existente == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");

            String error = validar(nombre, correo, semestre);
            if (error == null) {
                Estudiante estudiante = new Estudiante(id, nombre.trim(), correo.trim(),
                                                       Integer.parseInt(semestre.trim()));
                if (dao.actualizar(estudiante)) {
                    response.sendRedirect("estudiantes");
                    return;
                }
                error = "No se pudo actualizar el estudiante. Verifique que el correo no esté repetido.";
            }
            request.setAttribute("error", error);
            request.setAttribute("estudiante", existente);
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) {
            try {
                int id = Integer.parseInt(request.getParameter("id").trim());
                if (dao.eliminar(id)) {
                    response.sendRedirect("estudiantes");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}