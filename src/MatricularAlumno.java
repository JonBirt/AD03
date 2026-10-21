
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class MatricularAlumno {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/dbformacion";
        String usuario = "root";
        String contrasena = "12345";

        System.out.print("Introduce el DNI del alumno: ");
        String dni = teclado.nextLine().trim();

        // Comprobar que el DNI tiene 8 números y una letra.
        if (!dni.matches("\\d{8}[a-zA-Z]")) {
            System.out.println(
                "DNI no válido. Debe tener 8 números y una letra."
            );
            teclado.close();
            return;
        }

        // Aceptar la letra tanto en mayúscula como en minúscula.
        dni = dni.substring(0, 8) + dni.substring(8).toUpperCase();

        String nombreAlumno = "";
        boolean alumnoNuevo = false;

        try (Connection conexion =
                DriverManager.getConnection(url, usuario, contrasena)) {

            // 1. Comprobar si el alumno existe.
            String sqlAlumno =
                "SELECT nombre FROM alumnos WHERE dni = ?";

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sqlAlumno)) {

                sentencia.setString(1, dni);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        nombreAlumno = resultado.getString("nombre");
                        System.out.println(
                            "Alumno encontrado: " + nombreAlumno
                        );
                    } else {
                        System.out.println(
                            "El alumno no existe todavía."
                        );
                        System.out.print(
                            "Introduce el nombre del nuevo alumno: "
                        );
                        nombreAlumno = teclado.nextLine().trim();

                        if (nombreAlumno.isEmpty()) {
                            System.out.println(
                                "El nombre no puede estar vacío."
                            );
                            return;
                        }

                        alumnoNuevo = true;
                    }
                }
            }

            // 2. Mostrar los cursos y las plazas libres.
            String sqlCursos =
                "SELECT c.id_curso, c.titulo, " +
                "a.capacidad - COUNT(m.dni) AS plazas_libres " +
                "FROM cursos c " +
                "JOIN aulas a ON c.id_aula = a.id_aula " +
                "LEFT JOIN matriculas m ON c.id_curso = m.id_curso " +
                "GROUP BY c.id_curso, c.titulo, a.capacidad " +
                "ORDER BY c.id_curso";

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sqlCursos);
                 ResultSet resultado = sentencia.executeQuery()) {

                System.out.println("\nCursos disponibles:");

                while (resultado.next()) {
                    System.out.println(
                        resultado.getInt("id_curso") + " - " +
                        resultado.getString("titulo") +
                        " - Plazas libres: " +
                        resultado.getInt("plazas_libres")
                    );
                }
            }

            System.out.print("Introduce el ID del curso elegido: ");

            int idCursoElegido;

            try {
                idCursoElegido = Integer.parseInt(teclado.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("El ID del curso debe ser un número.");
                return;
            }

            // 3. Comprobar que el curso existe y tiene plazas.
            String sqlCurso =
                "SELECT a.capacidad - COUNT(m.dni) AS plazas_libres " +
                "FROM cursos c " +
                "JOIN aulas a ON c.id_aula = a.id_aula " +
                "LEFT JOIN matriculas m ON c.id_curso = m.id_curso " +
                "WHERE c.id_curso = ? " +
                "GROUP BY c.id_curso, a.capacidad";

            int plazasLibres;

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sqlCurso)) {

                sentencia.setInt(1, idCursoElegido);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        System.out.println("El curso no existe.");
                        return;
                    }

                    plazasLibres = resultado.getInt("plazas_libres");
                }
            }

            if (plazasLibres <= 0) {
                System.out.println("El curso está completo.");
                return;
            }

            System.out.println(
                "Quedan " + plazasLibres + " plazas libres."
            );

            // 4. Comprobar si ya está matriculado.
            String sqlMatriculaExistente =
                "SELECT dni FROM matriculas " +
                "WHERE dni = ? AND id_curso = ?";

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sqlMatriculaExistente)) {

                sentencia.setString(1, dni);
                sentencia.setInt(2, idCursoElegido);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        System.out.println(
                            "El alumno ya está matriculado en este curso."
                        );
                        return;
                    }
                }
            }

            // 5. Iniciar la transacción de las inserciones.
            conexion.setAutoCommit(false);

            try {
                // Si es nuevo, insertar primero al alumno.
                if (alumnoNuevo) {
                    String sqlInsertarAlumno =
                        "INSERT INTO alumnos (dni, nombre) VALUES (?, ?)";

                    try (PreparedStatement sentencia =
                            conexion.prepareStatement(sqlInsertarAlumno)) {

                        sentencia.setString(1, dni);
                        sentencia.setString(2, nombreAlumno);
                        sentencia.executeUpdate();
                    }
                }

                // Insertar la matrícula.
                String sqlInsertarMatricula =
                    "INSERT INTO matriculas " +
                    "(dni, id_curso, fecha_matricula) " +
                    "VALUES (?, ?, CURDATE())";

                try (PreparedStatement sentencia =
                        conexion.prepareStatement(sqlInsertarMatricula)) {

                    sentencia.setString(1, dni);
                    sentencia.setInt(2, idCursoElegido);
                    sentencia.executeUpdate();
                }

                // Confirmar ambas inserciones.
                conexion.commit();

                if (alumnoNuevo) {
                    System.out.println("Alumno creado correctamente.");
                }

                System.out.println("Matrícula registrada para " + nombreAlumno + " en Testing con Junit");

            } catch (SQLException e) {
                // Deshacer los cambios si falla una inserción.
                conexion.rollback();

                System.out.println(
                    "Error al guardar el alumno o la matrícula. " +
                    "Se han deshecho los cambios."
                );
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.out.println("Error al acceder a la base de datos:");
            e.printStackTrace();

        } finally {
            teclado.close();
        }
    }
}
