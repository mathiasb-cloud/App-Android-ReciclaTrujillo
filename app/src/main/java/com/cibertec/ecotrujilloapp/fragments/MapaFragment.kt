package com.cibertec.ecotrujilloapp.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.cibertec.ecotrujilloapp.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class MapaFragment : Fragment(), OnMapReadyCallback, SensorEventListener {


    private lateinit var mapView: MapView
    private lateinit var mapa: GoogleMap
    private val mapScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var locationCallback: LocationCallback? = null


    private var latitudModulo: Double = 0.0
    private var longitudModulo: Double = 0.0
    private var marcadorUsuario: Marker? = null
    private var conoUsuario: Marker? = null
    private var ultimaRotacion: Float = 0f

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)


        latitudModulo = activity?.intent?.getDoubleExtra("latitud", 0.0) ?: 0.0
        longitudModulo = activity?.intent?.getDoubleExtra("longitud", 0.0) ?: 0.0

        // Servicios
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // MapView + lifecycle
        var mapBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(mapBundle)
        mapView.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        mapa.mapType = GoogleMap.MAP_TYPE_NORMAL
        mapa.uiSettings.isZoomControlsEnabled = true
        mapa.uiSettings.isCompassEnabled = true
        mapa.uiSettings.isMyLocationButtonEnabled = false
        mapa.isTrafficEnabled = false

        try { mapa.isMyLocationEnabled = false } catch (_: SecurityException) {}

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        // FABs del overlay
        val fabTraffic  = view?.findViewById<FloatingActionButton>(R.id.fabTraffic)
        val fabCentrar  = view?.findViewById<FloatingActionButton>(R.id.fabCentrar)
        val fabTipoMapa = view?.findViewById<FloatingActionButton>(R.id.fabTipoMapa)
        val fabVista    = view?.findViewById<FloatingActionButton>(R.id.fabVista)

        fabTraffic?.setOnClickListener {

            mapa.isTrafficEnabled = !mapa.isTrafficEnabled
            Toast.makeText(requireContext(),
                if (mapa.isTrafficEnabled) "Tráfico activado" else "Tráfico desactivado",
                Toast.LENGTH_SHORT
            ).show()

        }

        fabCentrar?.setOnClickListener {
            marcadorUsuario?.let {
                mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 17f)) }
        }

        fabTipoMapa?.setOnClickListener {
            mapa.mapType = when (mapa.mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
                GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_HYBRID
                else -> GoogleMap.MAP_TYPE_NORMAL
            }
        }

        fabVista?.setOnClickListener {
            mapa.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder(mapa.cameraPosition).tilt(45f).zoom(17f).build()
                )
            )
        }

        if (latitudModulo == 0.0 || longitudModulo == 0.0) {
            Toast.makeText(requireContext(), "⚠ Coordenadas del módulo inválidas", Toast.LENGTH_SHORT).show()
            return
        }

        val destino = LatLng(latitudModulo, longitudModulo)
        mapa.addMarker(
            MarkerOptions()
                .position(destino)
                .title("Módulo de reciclaje")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15f))
        iniciarUbicacionTiempoReal(destino)





        mapView.setOnTouchListener { _, _ -> false }
        mapView.isClickable = false
        mapView.isFocusable = false
        mapView.translationZ = -2f


        requireActivity().findViewById<View>(R.id.btnMenu)?.bringToFront()
        requireActivity().findViewById<View>(R.id.bottomNavigation)?.bringToFront()



    }

    @Suppress("MissingPermission")
    private fun iniciarUbicacionTiempoReal(destino: LatLng) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val posicion = LatLng(location.latitude, location.longitude)

                if (marcadorUsuario == null) {
                    marcadorUsuario = mapa.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .flat(true)
                            .anchor(0.5f, 0.5f)
                            .icon(crearPuntoVerde())
                    )
                } else {
                    marcadorUsuario!!.position = posicion
                }

                if (conoUsuario == null) {
                    conoUsuario = mapa.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .flat(true)
                            .anchor(0.5f, 0.5f)
                            .icon(crearConoDifuminado())
                    )
                } else {
                    conoUsuario!!.position = posicion
                }

                mapa.animateCamera(CameraUpdateFactory.newLatLng(posicion))
                cargarRutaDesdeGoogleDirections(posicion, destino)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }


    private fun crearPuntoVerde(): BitmapDescriptor {
        val size = 60
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val c = size / 2f
        val rOut = c
        val rIn = c - 6f

        val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4CAF50")
            style = Paint.Style.FILL
        }

        canvas.drawCircle(c, c, rOut, paintBorder)
        canvas.drawCircle(c, c, rIn, paintCenter)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun crearConoDifuminado(): BitmapDescriptor {
        val w = 320
        val h = 230
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val path = Path().apply {
            moveTo(w / 2f, h * 0.39f)
            lineTo(w * 0.28f, h.toFloat())
            lineTo(w * 0.72f, h.toFloat())
            close()
        }

        val gradient = LinearGradient(
            w / 2f, h * 0.15f, w / 2f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#B34CAF50"),
                Color.parseColor("#604CAF50"),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
            style = Paint.Style.FILL
        }

        canvas.drawPath(path, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun cargarRutaDesdeGoogleDirections(origen: LatLng, destino: LatLng) {
        val apiKey = getString(R.string.google_maps_key)
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${origen.latitude},${origen.longitude}" +
                    "&destination=${destino.latitude},${destino.longitude}&mode=walking&key=$apiKey"

        mapScope.launch {
            try {
                val data = URL(url).readText()
                val json = JSONObject(data)
                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    val path = decodePoly(points)
                    withContext(Dispatchers.Main) {
                        mapa.addPolyline(
                            PolylineOptions()
                                .addAll(path)
                                .width(6f)
                                .color(Color.parseColor("#4CAF50"))
                                .geodesic(true)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MAPA", "Error cargando ruta: ${e.message}")
            }
        }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapScope.cancel()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapBundle == null) {
            mapBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle)
        }
        mapView.onSaveInstanceState(mapBundle)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

            if (::mapa.isInitialized && conoUsuario != null) {
                if (kotlin.math.abs(azimuth - ultimaRotacion) > 2) {
                    ultimaRotacion = azimuth
                    conoUsuario!!.rotation = azimuth
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */ }
}
