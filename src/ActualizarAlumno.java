import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ActualizarAlumno {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);
        
        String url = "jdbc:mysql://localhost:3306/dbformacion";
        String usuario = "root";
        String contrasena = "12345";

        System.out.print("Introduce el DNI del alumno: ");
        String dni = teclado.nextLine();

        String sql = "SELECT nombre FROM alumnos WHERE dni = ?";
        
        try (
                Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
                PreparedStatement sentencia = conexion.prepareStatement(sql)
            ) {
                sentencia.setString(1, dni);

                try (ResultSet resultado = sentencia.executeQuery()) {
                	
                	 if (resultado.next()) {
                         String nombreActual = resultado.getString("nombre");
                         System.out.println("Nombre actual del alumno: " + nombreActual);
                         
                         System.out.print("Introduce el nuevo nombre (Deja en blanco para no cambiar): ");
                         String nuevoNombre = teclado.nextLine();


					if (nuevoNombre.isBlank()) {
					    System.out.println("No se ha realizado ningún cambio.");
					} else {
					    String sqlActualizar = "UPDATE alumnos SET nombre = ? WHERE dni = ?";
					
					    try (PreparedStatement actualizar = conexion.prepareStatement(sqlActualizar)) {
					        actualizar.setString(1, nuevoNombre);
					        actualizar.setString(2, dni);
					
					        int filasModificadas = actualizar.executeUpdate();
					
					        if (filasModificadas > 0) {
					            System.out.println("Nombre actualizado correctamente.");
					        } else {
					            System.out.println("No se ha podido actualizar el nombre.");
					        }
					    }
					}

                         
                     } else {
                         System.out.println("No existe ningún alumno con ese DNI.");
                     }
                 }

             } catch (SQLException e) {
                 System.out.println("Error al acceder a la base de datos:");
                 e.printStackTrace();
             }

             teclado.close();
         }
}
