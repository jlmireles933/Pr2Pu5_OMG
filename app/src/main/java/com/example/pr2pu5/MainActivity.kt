package com.example.pr2pu5

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pr2pu5.ui.theme.Pr2Pu5Theme
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.sql.Date
import java.sql.Time

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton

val listaHabitacionGlobal = mutableStateListOf(
    HabitacionItem(idHabitacion = "Hab-01", numeroHabitacion = "101", piso = "Piso 1", estado = "Libre"),
    HabitacionItem(idHabitacion = "Hab-02", numeroHabitacion = "102", piso = "Piso 1", estado = "Ocupada")
)
val listaPacienteGlobal = mutableStateListOf(
    PacienteItem(curp = "ABCD123456", fechaIngreso = "01/10/2026", fechaSalida = "05/10/2026", nombre = "Juan Pérez", domicilio = "Calle 1"),
    PacienteItem(curp = "EFGH789012", fechaIngreso = "02/10/2026", fechaSalida = "06/10/2026", nombre = "María López", domicilio = "Calle 2")
)
val listaHistorialGlobal = mutableStateListOf(
    HistorialItem(cirugias = "Vesectomia", alergias = "Ninguna" , diagnosticos = "Calentura", fotoPaciente= "foto1.jpg" ,idHistorial = "" ),
    HistorialItem(cirugias = "Vesectomia", alergias = "Peanut Butter" , diagnosticos = "Gripa", fotoPaciente = "foto1.jpg" , idHistorial = "")
)

data class ModeloUsuario(val idusuario: Int, val usuario: String, val contrasena: String, val nombre_completo:String, val rol: String, val correo: String)
data class ModeloHistorialMedico(val idhistorial: Int, val fotopaciente: String, val alergias: String, val cirugias: String, val diagnosticosprevios: String, val cartillamedica: String, val curp: String, )

data class ModeloPaciente(val curp: String, val fechaingreso: Date, val fechasalida: Date, val nombre: String , val domicilio: String)
data class ModeloHabitacion(val idHabitacion: Int, val numero_Habitacion: Int, val piso: Int, val estado: Int, val curp: String )
data class ModeloDoctor(val cedula: String, val nombre: String, val telefono: String, val especialidad: String, val horaentrada: Time, val horasalida: Time, val idusuario: Int)
data class ModeloConsulta(val idconsulta: Int, val fechaConsulta: Date, val Descripcion: String, val Medicamento: String, val curp: String, val idusuario: Int)

data class HistorialItem(val idHistorial: String, val cirugias: String, val diagnosticos: String, val alergias: String, val fotoPaciente: String)
data class HabitacionItem(val idHabitacion: String, val numeroHabitacion: String, val piso: String, val estado: String)
data class PacienteItem(val curp: String, val fechaIngreso: String, val fechaSalida: String, val nombre: String, val domicilio: String)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pr2Pu5Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
class Preferencias(context: Context) {
    private val preferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    fun guardar(prop: String, value: String) {
        preferences.edit()
            .putString(prop, value)
            .apply()
    }
    fun obtener(prop: String, def: String): String? {
        return preferences.getString(prop, def)
    }


    fun remover(prop: String) {
        preferences.edit()
            .remove(prop)
            .apply()
    }
}
@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current //val es un constante
    val preferencias = Preferencias(context)
    var destination = "PacienteContent" //var es un variable


    if (preferencias.obtener("username", "") != "") { //if an id already exists in a session
        destination = "MenuContent"                         //takes you straight to you Menu
    }



    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = destination,
        modifier = modifier
    ) {
        composable("LoginContent") { LoginContent(navController) }
        composable("MenuContent") { MenuContent(navController) }
        composable("PacienteContent") { PacienteContent(navController) }
        composable("PacienteFormContent") { PacienteFormContent(navController) }
        composable("HistorialContent") { HistorialContent(navController) }
        composable("HistorialFormContent") { HistorialFormContent(navController) }
        composable("HabitacionContent") { HabitacionContent(navController) }
        composable("HabitacionFormContent") { HabitacionFormContent(navController) }
    }
}
interface ApiService {
    @FormUrlEncoded
    @POST("iniciarSesion.php")
    suspend fun iniciarSesion(
        @Field("username") username: String,
        @Field("contrasena") contrasena: String
    ): Response<String>

    // C VAN TODOS LOS CAMPOS MENOS EL ID
    @POST("manicomio.php?registrar")
    @FormUrlEncoded
    suspend fun registrarHistorialMedico(
    @Field("fotopaciente") fotopaciente: String,
    @Field("alergias") alergias: String,
    @Field("cirugias") cirugias: String,
    @Field("diagnosticosprevios") diagnosticosprevios: String,
    @Field("cartillamedica") cartillamedica: String,
    @Field("curp") curp: String,
    ): Response<Unit>

    // R BUSCAR
    @GET("manicomio.php?buscar")
    suspend fun buscarHistorialMedico(@Query("buscar") busqueda: String): List<ModeloHistorialMedico>

    // U SOLO EN MODIFICAR, INGRESAMOS EL ID
    @POST("manicomio.php?modificar")
    @FormUrlEncoded
    suspend fun modificarHistorialMedico(
        @Field("idhistorial") idhistorial: Int,
        @Field("fotopaciente") fotopaciente: String,
        @Field("alergias") alergias: String,
        @Field("cirugias") cirugias: String,
        @Field("diagnosticosprevios") diagnosticosprevios: String,
        @Field("cartillamedica") cartillamedica: String,
        @Field("curp") curp: String,
    ): Response<Unit>

    // D elmiinar
    @POST("manicomio.php?eliminar")
    @FormUrlEncoded
    suspend fun eliminarHistorialMedico(
        @Field("idhistorial") idhistorial: Int
    ): Response<Unit>
}


val retrofit = Retrofit.Builder()
    .baseUrl("https://borders-annotation-customized-witness.trycloudflare.com/AW/app/api/")
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build()


val api = retrofit.create(ApiService::class.java)





@Composable
fun LoginContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferencias = Preferencias(context)
    val scope = rememberCoroutineScope ()

    var username by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.perfil),
            contentDescription = "Fondo de inicio de sesión",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Inicio de Sesión",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Usuario:",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Ingresa tu Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Contraseña:",
                color = Color.White
            )
            TextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                scope.launch {
                    try {
                        val respuesta = api.iniciarSesion(username, contrasena)
                        val id : String = respuesta.body().toString()
                        Toast.makeText(context, "Respuesta: '$id'", Toast.LENGTH_LONG).show()
                        username = id
                        if (id.toDoubleOrNull() != null) {
                            preferencias.guardar("username", username)
                            navController.navigate("MenuContent")
                        }
                        else {
                            Toast.makeText(context, "Usuario y/o contraseña incorrecto(s).", Toast.LENGTH_SHORT).show()
                            }
                        }
                        catch (e: Exception) {
                            Log.e("API", "Error al iniciar sesión: ${e.message}")
                        }
                }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Iniciar sesión")
            }
        }
    }
}

@Composable
fun MenuContent(navController: NavHostController, modifier: Modifier = Modifier) {
   val preferencias = Preferencias(LocalContext.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel de Consulta - Menú",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { navController.navigate("PacienteContent") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Registro de Pacientes",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { navController.navigate("HistorialContent") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Historial Médico")
        }

        Button(
            onClick = { navController.navigate("HabitacionContent") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Habitación")
        }

        Button(
            onClick = {
                preferencias.remover("id")



                navController.navigate("LoginContent")
                      },

            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun HistorialContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // AI code added: Context for showing Toasts
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // AI code added: Vertical scroll state for multi-row database lists
    val verticalScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var historialAEditar by remember { mutableStateOf<ModeloHistorialMedico?>(null) } //AI
    // State to hold the list fetched from the database
    var listaHistorial by remember { mutableStateOf<List<ModeloHistorialMedico>>(emptyList()) }

    // Fetch data automatically when the screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                listaHistorial = api.buscarHistorialMedico("")
            } catch (e: Exception) {
                // AI code added: Error logging matching your pattern
                Log.e("API", "Error al cargar el historial: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.derrickroseimage),
            contentDescription = "Fondo de historial médico",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Tabla: Historial Médico",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("HistorialFormContent") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Nuevo Historial")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI code added: Translucent background container box for table readability over background image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0x88000000))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState) // AI code added: enable vertical scrolling
                        .horizontalScroll(scrollState)
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ID", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Foto", modifier = Modifier.width(110.dp), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Alergias", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Cirugias", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Diagnósticos", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Acción", modifier = Modifier.width(196.dp), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Divider(color = Color.White)

                    // Table Rows mapped dynamically from phpMyAdmin database
                    listaHistorial.forEachIndexed { index, item ->
                        val bgColor = if (index % 2 == 0) Color(0xEEFFFFFF) else Color(0xDDFFFFFF)
                        Row(
                            modifier = Modifier
                                .background(bgColor)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.idhistorial.toString(), modifier = Modifier.width(60.dp), color = Color.DarkGray)
                            Text(item.fotopaciente, modifier = Modifier.width(110.dp), color = Color.DarkGray)
                            Text(item.alergias, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                            Text(item.cirugias, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                            Text(item.diagnosticosprevios, modifier = Modifier.width(150.dp), color = Color.DarkGray)

                            Button(
                                onClick = {
                                    historialAEditar = item
                                },
                                modifier = Modifier.width(90.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                                ){
                                Text("Editar", color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    // AI code added: Coroutine delete action using your Toast and Logcat structure
                                    coroutineScope.launch {
                                        try {
                                            val response = api.eliminarHistorialMedico(item.idhistorial)
                                            if (response.isSuccessful) {
                                                Toast.makeText(context, "Historial eliminado con éxito.", Toast.LENGTH_SHORT).show()
                                                // Refresh list from database
                                                listaHistorial = api.buscarHistorialMedico("")
                                            } else {
                                                Toast.makeText(context, "Error al eliminar de la base de datos.", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("API", "Error al eliminar el historial: ${e.message}")
                                            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.width(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Borrar", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("MenuContent") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regresar al Menú")
            }
        }
        historialAEditar?.let { registro ->
            var fotopaciente by remember { mutableStateOf(registro.fotopaciente) }
            var alergias by remember { mutableStateOf(registro.alergias) }
            var cirugias by remember { mutableStateOf(registro.cirugias) }
            var diagnosticosprevios by remember { mutableStateOf(registro.diagnosticosprevios) }
            var cartillamedica by remember { mutableStateOf(registro.cartillamedica) }
            var curp by remember { mutableStateOf(registro.curp) }

            AlertDialog(
                onDismissRequest = { historialAEditar = null },
                title = { Text("Editar Historial #${registro.idhistorial}") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(value = fotopaciente, onValueChange = { fotopaciente = it }, label = { Text("Foto Paciente") })
                        OutlinedTextField(value = alergias, onValueChange = { alergias = it }, label = { Text("Alergias") })
                        OutlinedTextField(value = cirugias, onValueChange = { cirugias = it }, label = { Text("Cirugías") })
                        OutlinedTextField(value = diagnosticosprevios, onValueChange = { diagnosticosprevios = it }, label = { Text("Diagnósticos Previos") })
                        OutlinedTextField(value = cartillamedica, onValueChange = { cartillamedica = it }, label = { Text("Cartilla Médica") })
                        OutlinedTextField(value = curp, onValueChange = { curp = it }, label = { Text("CURP") })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val response = api.modificarHistorialMedico(
                                        idhistorial = registro.idhistorial,
                                        fotopaciente = fotopaciente,
                                        alergias = alergias,
                                        cirugias = cirugias,
                                        diagnosticosprevios = diagnosticosprevios,
                                        cartillamedica = cartillamedica,
                                        curp = curp
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Historial actualizado.", Toast.LENGTH_SHORT).show()
                                        listaHistorial = api.buscarHistorialMedico("")
                                        historialAEditar = null
                                    } else {
                                        Toast.makeText(context, "Error al actualizar.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("API", "Error al editar: ${e.message}")
                                    Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { historialAEditar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialFormContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    var idHistorial by remember { mutableStateOf("") }
    var alergias by remember { mutableStateOf("") }
    var cirugias by remember { mutableStateOf("") }
    var diagnosticos by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        // TODO: Assign the uri string to your state/model variable if needed
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Formulario de Historial",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            Text(text = "CURP:")
            TextField(
                value = idHistorial,
                onValueChange = { idHistorial = it },
                placeholder = { Text("Ej. H-1234") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(text = "Alergias:")
            TextField(
                value = alergias,
                onValueChange = { alergias = it },
                placeholder = { Text("Ej. Pollen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text(text = "Cirugias:")
            TextField(
                value = cirugias,
                onValueChange = { cirugias = it },
                placeholder = { Text("Ej. Vesectomia") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(text = "Diagnosticos:")
            TextField(
                value = diagnosticos,
                onValueChange = { diagnosticos = it },
                placeholder = { Text("Ej. Daños en las cavidades oculares") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )



            // Below your Text(text = "Imagen Nueva:")
            Button(
                onClick = {
                    // Filter for images; you can use "*/*" for any file type
                    launcher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = if (imageUri == null) "Seleccionar Archivo" else "Archivo Seleccionado ✓")
            }

// Optional: Display the file path or a small preview if selected
            if (imageUri != null) {
                Text(
                    text = "URI: $imageUri",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (idHistorial.isNotBlank() && alergias.isNotBlank()) {
                        coroutineScope.launch {
                            try {
                                // Call your API to insert into phpMyAdmin
                                val response = api.registrarHistorialMedico(
                                    fotopaciente = if (imageUri != null) imageUri.toString() else "foto1.jpg",
                                    alergias = alergias,
                                    cirugias = cirugias,
                                    diagnosticosprevios = diagnosticos,
                                    cartillamedica = "Completa", // or map to a form field if you have one
                                    curp = idHistorial // using your idHistorial field for curp based on your form text
                                )

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Guardado con éxito en la Base de Datos", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Error al guardar en el servidor", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Falta uno o mas campos para llenar", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
            ) {
                Text("Guardar Paciente")
            }

            Button(
                onClick = { navController.navigate("MenuContent") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regresar al Menú")
            }
        }
    }
}

@Composable
fun PacienteContent(navController: NavHostController, modifier: Modifier = Modifier) {

    val preferencias = Preferencias(LocalContext.current)

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.backgroundimage),
            contentDescription = "Fondo de tabla de pacientes",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Registro de Pacientes",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("PacienteFormContent") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Nuevo Paciente")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                Row {
                    Text("CURP", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold)
                    Text("Fecha de Ingreso", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold)
                    Text("Fecha de Salida", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold)
                    Text("Nombre", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold)
                    Text("Domicilio", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold)
                    Text("Acción", modifier = Modifier.width(1.dp), fontWeight = FontWeight.Bold)
                }
                Divider()

                listaPacienteGlobal.forEachIndexed { index, item ->
                    val bgColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color.White
                    Row(
                        modifier = Modifier
                            .background(bgColor)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.curp, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.fechaIngreso, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.fechaSalida, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.nombre, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.domicilio, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Button(
                            onClick = { listaPacienteGlobal.removeAt(index) },
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Borrar")
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("MenuContent") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regresar al Menú")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacienteFormContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var curp by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf("") }
    var fechaSalida by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var domicilio by remember { mutableStateOf("") }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Formulario: Paciente",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        Text(text = "CURP:")
        TextField(
            value = curp,
            onValueChange = { curp = it },
            placeholder = { Text("Ej. ABCD123456") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(text = "Fecha de Ingreso:")
        TextField(
            value = fechaIngreso,
            onValueChange = { fechaIngreso = it },
            placeholder = { Text("Ej. 01/10/2026") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(text = "Fecha de Salida:")
        TextField(
            value = fechaSalida,
            onValueChange = { fechaSalida = it },
            placeholder = { Text("Ej. 05/10/2026") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(text = "Nombre:")
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = { Text("Ej. Juan Pérez") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(text = "Domicilio:")
        TextField(
            value = domicilio,
            onValueChange = { domicilio = it },
            placeholder = { Text("Ej. Calle 1") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if  (curp.isNotBlank() && nombre.isNotBlank()) {
                    listaPacienteGlobal.add(
                        PacienteItem(
                            curp = curp,
                            fechaIngreso = fechaIngreso,
                            fechaSalida = fechaSalida,
                            nombre = nombre,
                            domicilio = domicilio
                        )
                    )
                    Toast.makeText(context, "Paciente guardado con éxito", Toast.LENGTH_SHORT)
                        .show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Paciente")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun HabitacionContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.juanjo),
            contentDescription = "Fondo de tabla de habitaciones",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Tabla: Habitación",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("HabitacionFormContent") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Nueva Habitación")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                Row {
                    Text("ID Habitacion", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Numero Habitacion", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Piso", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Estado", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Acción", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold, color = Color.White)
                }
                Divider(color = Color.White)

                listaHabitacionGlobal.forEachIndexed { index, item ->
                    val bgColor = if (index % 2 == 0) Color(0xAAFFFFFF) else Color(0xCCFFFFFF)
                    Row(
                        modifier = Modifier
                            .background(bgColor)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.idHabitacion, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.numeroHabitacion, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.piso, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Text(item.estado, modifier = Modifier.width(130.dp), color = Color.DarkGray)
                        Button(
                            onClick = { listaHabitacionGlobal.removeAt(index) },
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Borrar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("MenuContent") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regresar al Menú")
            }
        }
    }
}

annotation class composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitacionFormContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var idHabitacion by remember { mutableStateOf("") }
    var numeroHabitacion by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Formulario: Habitación",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            Text(text = "ID Habitación:", color = Color.Black)
            TextField(
                value = idHabitacion,
                onValueChange = { idHabitacion = it },
                placeholder = { Text("Ej. Hab-03") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(text = "Número de Habitación:", color = Color.Black)
            TextField(
                value = numeroHabitacion,
                onValueChange = { numeroHabitacion = it },
                placeholder = { Text("Ej. 103") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(text = "Piso:", color = Color.Black)
            TextField(
                value = piso,
                onValueChange = { piso = it },
                placeholder = { Text("Ej. Piso 2") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(text = "Estado:", color = Color.Black)
            TextField(
                value = estado,
                onValueChange = { estado = it },
                placeholder = { Text("Ej. Libre u Ocupada") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)

            )

            Button(
                onClick = {
                    if (idHabitacion.isNotBlank() && numeroHabitacion.isNotBlank()) {
                        listaHabitacionGlobal.add(
                            HabitacionItem(
                                idHabitacion = idHabitacion,
                                numeroHabitacion = numeroHabitacion,
                                piso = piso,
                                estado = estado
                            )
                        )
                        Toast.makeText(context, "Habitación guardada con éxito", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Habitación")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    Pr2Pu5Theme {
        AppContent()
    }
}