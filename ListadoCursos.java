import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ListadoCursos {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/dbformacion";
        String usuario = "root";
        String contrasena = "12345";
        
        String sql = """
                SELECT
                    c.id_curso,
                    c.titulo,
                    c.fecha,
                    COUNT(m.dni) AS total_alumnos,
                    a.capacidad,
                    a.nombre AS aula,
                    a.edificio
                FROM cursos c
                INNER JOIN aulas a
                    ON c.id_aula = a.id_aula
                LEFT JOIN matriculas m
                    ON c.id_curso = m.id_curso
                GROUP BY
                    c.id_curso,
                    c.titulo,
                    c.fecha,
                    a.capacidad,
                    a.nombre,
                    a.edificio
                ORDER BY c.fecha
                """;


			try (
			    Connection conexion = DriverManager.getConnection(
			        url, usuario, contrasena
			    );
			    Statement sentencia = conexion.createStatement();
			    ResultSet resultado = sentencia.executeQuery(sql)
			) {

            	   while (resultado.next()) {
                       System.out.println(
                           resultado.getInt("id_curso") + " - "
                           + resultado.getString("titulo") + " - "
                           + resultado.getDate("fecha") + " - "
                           + resultado.getInt("total_alumnos") + " alumnos - "
                           + resultado.getInt("capacidad") + " plazas - "
                           + resultado.getString("aula") + " - "
                           + resultado.getString("edificio")
                       );
                   }

           

            

        } catch (SQLException e) {
            System.out.println("Error al conectar con MySQL.");
            e.printStackTrace();
        }
    }
    }

