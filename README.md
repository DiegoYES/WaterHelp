💧 WaterHelp

WaterHelp es una aplicación móvil desarrollada en Android (Kotlin) diseñada para fomentar el uso responsable y consciente del agua. Permite a los usuarios monitorear su consumo diario, establecer límites personalizados y visualizar su progreso histórico para crear hábitos más sostenibles.

Este proyecto fue desarrollado como parte de la asignatura Desarrollo de Aplicaciones en Android para el Proyecto 2: Cuidado Responsable del Agua.

🚀 Características Principales

Registro de Consumo: Permite registrar la cantidad de litros consumidos por actividad (ej. bañarse, lavar trastes) en una fecha específica.

Límite Personalizado: El usuario puede establecer un límite diario de litros (almacenado localmente).

Monitoreo en Tiempo Real: Comparación inmediata entre el consumo acumulado del día y el límite establecido.

Feedback Visual:

Mensajes de Alerta: Avisos en color rojo si se excede el límite (con porcentaje de exceso).

Mensajes de Felicitación: Avisos en color verde si se está por debajo del límite (con porcentaje de ahorro).

Historial Gráfico: Gráfica de barras personalizada (Canvas) que muestra el consumo de los últimos 7 días.

Consejos Ecológicos: Sección con recomendaciones prácticas para ahorrar agua.

Persistencia de Datos: Todos los registros y configuraciones se guardan localmente para no perder información al cerrar la app.

🛠️ Tecnologías Utilizadas

Lenguaje: Kotlin

UI Toolkit: Jetpack Compose (Interfaz de usuario moderna y declarativa).

Arquitectura: MVVM (Model-View-ViewModel).

Base de Datos: Room (SQLite) para almacenar el historial de registros.

Almacenamiento de Preferencias: Jetpack DataStore para guardar el límite diario del usuario.

Concurrencia: Kotlin Coroutines & Flow para operaciones asíncronas y reactivas.

📂 Estructura del Proyecto

El código está organizado de manera modular para facilitar su mantenimiento y escalabilidad:

com.example.waterhelp
│
├── data/                  # Capa de Datos
│   ├── AppDatabase.kt     # Configuración de Room (Base de Datos)
│   ├── WaterDao.kt        # Interfaces de acceso a datos (Queries SQL)
│   ├── WaterRecord.kt     # Entidad (Tabla de registros)
│   └── PreferencesManager.kt # Gestión de DataStore (Límite diario)
│
├── ui2/                   # Capa de Interfaz de Usuario
│   ├── MainActivity.kt    # Punto de entrada de la aplicación
│   └── WaterHelpApp.kt    # Lógica principal de UI, ViewModel y Navegación
│
└── ui.theme/              # Tema y Estilos
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt


🔧 Configuración e Instalación

Clonar el repositorio:

git clone [https://github.com/TU_USUARIO/WaterHelp.git](https://github.com/TU_USUARIO/WaterHelp.git)


Abrir en Android Studio:

Abre Android Studio y selecciona "Open".

Navega a la carpeta donde clonaste el repositorio y selecciona la carpeta raíz.

Sincronizar Gradle:

Espera a que Android Studio descargue las dependencias y configure el proyecto.

Ejecutar:

Conecta un dispositivo Android o inicia un emulador (API 26 o superior recomendada).

Haz clic en el botón de Run (Play) en Android Studio.

📸 Capturas de Pantalla

(Espacio reservado para agregar capturas de la aplicación)

Dashboard (Ahorro)

Dashboard (Exceso)

Registro de Consumo

👥 Equipo de Desarrollo

Este proyecto fue creado por:

[Diego Jesús Hernández Aguilar] - [21130596]

[Sharon Michelle Mejía Cruz] - [21130612]

[Aixa Viviana Tovar Vazquez] - [21130606]

Instituto Tecnológico de La Laguna Ingeniería en Sistemas Computacionales Fecha: Noviembre 2025

📄 Licencia

Este proyecto es de uso académico y educativo.
