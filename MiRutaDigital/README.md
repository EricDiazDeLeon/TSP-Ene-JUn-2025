# Mi Ruta Digital

Una aplicación Android para compartir ubicación de camiones en tiempo real usando Google Maps, Firebase y Room.

## Características

- **Mapa interactivo** con Google Maps API
- **Rutas de transporte** con polilíneas
- **Paradas de autobús** marcadas en el mapa
- **Camiones en tiempo real** con ubicación actualizada
- **Compartir ubicación** en segundo plano
- **Arquitectura MVVM** con Room y Firebase

## Configuración

### 1. Google Maps API
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de Google Maps para Android
4. Crea una clave de API
5. Reemplaza `YOUR_GOOGLE_MAPS_API_KEY` en `AndroidManifest.xml`

### 2. Firebase
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto
3. Agrega tu aplicación Android
4. Descarga el archivo `google-services.json`
5. Reemplaza el archivo `google-services.json` en la carpeta `app/`

### 3. Permisos
La aplicación requiere los siguientes permisos:
- `ACCESS_FINE_LOCATION` - Para ubicación precisa
- `ACCESS_COARSE_LOCATION` - Para ubicación aproximada
- `ACCESS_BACKGROUND_LOCATION` - Para ubicación en segundo plano
- `FOREGROUND_SERVICE` - Para servicios en primer plano
- `FOREGROUND_SERVICE_LOCATION` - Para servicios de ubicación

## Estructura del Proyecto

```
app/src/main/java/com/example/mirutadigital/
├── data/
│   ├── model/           # Modelos de datos
│   ├── local/           # Base de datos Room
│   ├── remote/          # Servicios de Firebase
│   └── repository/      # Repositorio principal
├── service/             # Servicios en segundo plano
├── ui/
│   ├── map/            # Fragmento del mapa
│   ├── routeslist/     # Lista de rutas
│   ├── sharing/        # Pantalla de compartir
│   └── viewmodel/      # ViewModels
└── view/               # Actividades
```

## Uso

1. **Mapa Principal**: Muestra las rutas, paradas y camiones en tiempo real
2. **Ver Rutas**: Lista todas las rutas disponibles
3. **Compartir Ubicación**: Inicia el servicio de ubicación en segundo plano

## Tecnologías Utilizadas

- **Android Studio** - IDE
- **Kotlin** - Lenguaje de programación
- **Google Maps API** - Mapas interactivos
- **Firebase Firestore** - Base de datos en la nube
- **Room** - Base de datos local
- **MVVM** - Arquitectura de la aplicación
- **Coroutines** - Programación asíncrona
- **LiveData** - Observación de datos

## Próximos Pasos

1. Configurar las claves de API
2. Implementar la lógica de navegación entre fragments
3. Agregar validación de permisos
4. Implementar la lógica de horarios de rutas
5. Agregar notificaciones push
6. Implementar autenticación de usuarios
