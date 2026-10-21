
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;

public class ConsultarMatriculas {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/dbformacion";
        String usuario = "root";
        String contrasena = "12345";

        try (Connection conexion =
                DriverManager.getConnection(url, usuario, contrasena)) {

            // 1. Mostrar cursos.
            String sql = "SELECT id_curso, titulo FROM cursos ORDER BY id_curso";

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql);
                 ResultSet resultado = sentencia.executeQuery()) {

                System.out.println("Lista de cursos:");

                while (resultado.next()) {
                    System.out.println(
                        resultado.getInt("id_curso") + " - " +
                        resultado.getString("titulo")
                    );
                }
            }

            // 2. Pedir el ID
            System.out.print("Introduce el ID del curso: ");
            int idCurso = Integer.parseInt(teclado.nextLine());

            // 3. Llamar a la función almacenada de MySQL.
            String llamada = "{? = CALL obtener_numero_matriculados(?)}";

            try (CallableStatement sentencia =
                    conexion.prepareCall(llamada)) {

                // El primer parámetro recibirá el resultado.
                sentencia.registerOutParameter(1, Types.INTEGER);

                // El segundo parámetro es el ID del curso.
                sentencia.setInt(2, idCurso);

                sentencia.execute();

                int total = sentencia.getInt(1);

                System.out.println(
                    "Número de alumnos matriculados: " + total
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar las matrículas:");
            e.printStackTrace();

        } catch (NumberFormatException e) {
            System.out.println("Debes introducir un ID numérico.");

        } finally {
            teclado.close();
        }
    }
}

