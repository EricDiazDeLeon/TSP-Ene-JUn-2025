package com.example.mirutadigital.data.testsUi

import com.example.mirutadigital.data.testsUi.model.Stop
import com.example.mirutadigital.data.testsUi.model.route.Journey
import com.example.mirutadigital.data.testsUi.model.route.Route
import com.example.mirutadigital.data.testsUi.model.route.RouteDetail
import com.google.android.gms.maps.model.LatLng

// lista de paradas de ejemplo
val sampleStops = listOf(
    Stop("plaza_alesia", "Plaza Alesia", LatLng(22.76, -102.58)),
    Stop("miguel_aleman", "Miguel Alemán", LatLng(22.77, -102.57)),
    Stop("mina_patrocinio_140", "Mina del Patrocinio, 140", LatLng(22.78, -102.56)),
    Stop("sauceda", "Jardines de Sauceda", LatLng(22.75, -102.59)),
    Stop("villa_fontana", "Villa Fontana", LatLng(22.768423525502666, -102.4805096358822)),
    Stop("polideportivo", "Polideportivo", LatLng(22.76, -102.55)),
    Stop("primera", "Primera", LatLng(22.73143, -102.47472)),
    Stop("segunda", "Segunda", LatLng(22.73235, -102.47952)),
    Stop("tercera", "Tercera", LatLng(22.73143, -102.47472)),
    Stop("cuarta", "Cuarta", LatLng(22.73376, -102.48211)),
    Stop("quinta", "Quinta", LatLng(22.73493, -102.48590)),
    Stop("sexta", "Sexta", LatLng(22.73703, -102.49055)),
    Stop("septima", "Septima", LatLng(22.74022, -102.49686)),
    Stop("octava", "Octava", LatLng(22.75019, -102.49942)),
    Stop("novena", "Novena", LatLng(22.74901, -102.50292)),
    Stop("decima", "Decima", LatLng(22.74742, -102.51048)),
    Stop("primera_v", "Primera_V", LatLng(22.74724, -102.51041)),
    Stop("segunda_v", "Segunda_V", LatLng(22.74987, -102.49940)),
    Stop("tercera_v", "Tercera_V", LatLng(22.73934, -102.49637)),
    Stop("cuarta_v", "Cuarta_V", LatLng(22.73695, -102.49057)),
    Stop("quinta_v", "Quinta_V", LatLng(22.73203, -102.47890)),
    Stop("sexta_v", "Sexta_V", LatLng(22.73179, -102.47750))
)

// lista de rutas de ejemplo
val sampleRoutes = listOf(
    Route(
        id = "PERICO",
        name = "Perico",
        departureInterval = 35,
        outboundJourney = Journey(
            stops = listOf(
                sampleStops[6],
                sampleStops[7],
                sampleStops[8],
                sampleStops[9],
                sampleStops[10],
                sampleStops[11],
                sampleStops[12],
                sampleStops[13],
                sampleStops[14],
                sampleStops[15]
            ),
            firstDeparture = "06:30",
            lastDeparture = "19:30",
            encodedPolyline = "mvviC~qmpRGj@QnBQzBK`B[vD[tDEh@Eh@Mn@M^OXOX]`@]Za@\\[X]Z[TSTMPKVIXG^Ih@Ef@Cd@Ir@Gv@Gt@E`@E`@CXEVCTCXEVGd@G\\I\\I^IZITELENGTGTOd@I^Wz@Wt@M^Qj@Qf@Up@Sn@Od@MZK\\Qh@EJKXQh@KZQf@KVKTOb@O`@M\\M`@Q`@Of@Qf@Qj@Ob@Yr@Qd@Sh@Qf@Sj@Yv@_@fAk@~Ag@tAk@~Am@hBg@~Au@lBs@dB_@dAUbAMfAG~ASTq@JkAPoAN??yAPk@FeAJ{@JKFKFQHSDcAJaAHu@F[Bi@F]Di@Fg@Fa@Fm@Hw@LSDOHQFi@Hg@Fi@Hq@Hg@Fc@Fg@Ho@Hq@Jg@Fu@Hw@Jo@Hc@Dm@He@Fu@Ju@Ls@J{@Jo@Hy@FiAN[?QCUMOQIQAU@YFWHMHKJEJAR?P@JHHLDNBJHRDNDLDNDNJv@BZDNHJDLBLDJDVFRFXFRH\\H^Lf@Lf@Nl@H\\H^VjAHd@Px@J`@J`@H\\BTNl@JRNj@TbAH^H\\J^HXDRH\\H^HVPv@H\\D\\J^LTJ^Jh@J^Lj@Ld@J`@FZLj@Pn@Rx@Jd@F`@D^Bd@DhAC\\Dj@Cj@Gl@Kx@Md@Mb@GX"
        ),
        inboundJourney = Journey(
            stops = listOf(
                sampleStops[16],
                sampleStops[17],
                sampleStops[18],
                sampleStops[19],
                sampleStops[20],
                sampleStops[21]
            ),
            firstDeparture = "07:00",
            lastDeparture = "20:30",
            encodedPolyline = "gyyiC`qtpR`@iBJyADwAEyAMwA[gBm@eCy@_Dy@gDcAcEcA{Da@mBgAoE@_@CYOq@W}A_@cBk@mCG[@UJMPIj@G|@K~@Oz@Mj@Iv@Ip@GpDq@\\Kl@MtAQbBS`AKhAE`AI`AOl@Cl@J\\Lj@P`@ZvAzAh@`@f@Fv@WTc@l@aDPg@\\g@n@_@|@S`AM`AM`ASbAOPGTKLa@Hu@NkBZmAp@sBr@_C|@oCx@yBf@wAhAcD`BsEfBcFzAgEtAwDxAkEVs@x@oClB}Gf@qCh@uFTaCFcAPaATg@h@s@ZYv@k@t@q@`@k@\\q@ViALqAP_BNeB"
        ),
        detail = RouteDetail(
            windshieldLabel = "LA LAGUNA D.A. - GUADALUPE",
            colors = "Amarillo con Verde",
            price = 11.0
        )
    ),
    Route(
        id = "R_3",
        name = "Ruta 3",
        departureInterval = 15,
        outboundJourney = Journey(
            stops = listOf(
                sampleStops[0],
                sampleStops[1],
                sampleStops[5]
            ), // ida: Alesia -> Alemán -> Polideportivo
            firstDeparture = "06:00",
            lastDeparture = "22:00",
            encodedPolyline = null
        ),
        inboundJourney = Journey(
            stops = listOf(
                sampleStops[5],
                sampleStops[1],
                sampleStops[0]
            ), // vuelta: Polideportivo -> Alemán -> Alesia
            firstDeparture = "06:30",
            lastDeparture = "22:30",
            encodedPolyline = null
        ),
        detail = RouteDetail(
            windshieldLabel = "ZACATECAS - CEREZO",
            colors = "Blanco con Azul",
            price = 10.5
        )
    ),
    Route(
        id = "R_16",
        name = "Ruta 16",
        departureInterval = 20,
        outboundJourney = Journey(
            stops = listOf(sampleStops[0], sampleStops[3]), // Ida: Alesia -> Sauceda
            firstDeparture = "05:30",
            lastDeparture = "21:30",
            encodedPolyline = null
        ),
        inboundJourney = Journey(
            stops = listOf(sampleStops[3], sampleStops[0]), // Vuelta: Sauceda -> Alesia
            firstDeparture = "06:00",
            lastDeparture = "22:00",
            encodedPolyline = null
        ),
        detail = RouteDetail(
            windshieldLabel = "VILLA GPE - CD. GOBIERNO",
            colors = "Amarillo con Morado",
            price = 10.5
        )
    ),
    Route(
        id = "R_17",
        name = "Ruta 17",
        departureInterval = 25,
        outboundJourney = Journey(
            stops = listOf(sampleStops[0], sampleStops[4]), // ida: Alesia -> Villa Fontana
            firstDeparture = "06:20",
            lastDeparture = "21:00",
            encodedPolyline = null
        ),
        inboundJourney = Journey(
            stops = listOf(sampleStops[4], sampleStops[0]), // vuelta: Villa Fontana -> Alesia
            firstDeparture = "06:05",
            lastDeparture = "21:30",
            encodedPolyline = null
        ),
        detail = RouteDetail(
            windshieldLabel = "VILLA GPE - SIGLO XXI",
            colors = "Amarillo con Verde",
            price = 10.5
        )
    ),
    Route(
        id = "R_4",
        name = "Ruta 4",
        departureInterval = 15,
        outboundJourney = Journey(
            stops = listOf(
                sampleStops[0],
                sampleStops[1],
                sampleStops[5],
                sampleStops[3]
            ), // ida: Alesia -> Alemán -> Polideportivo
            firstDeparture = "06:00",
            lastDeparture = "22:00",
            encodedPolyline = null
        ),
        inboundJourney = Journey(
            stops = listOf(
                sampleStops[5],
                sampleStops[1],
                sampleStops[0]
            ), // vuelta: Polideportivo -> Alemán -> Alesia
            firstDeparture = "06:30",
            lastDeparture = "22:30",
            encodedPolyline = null
        ),
        detail = RouteDetail(
            windshieldLabel = "ZACATECAS - BUFA",
            colors = "Blanco con Gris",
            price = 9.5
        )
    )
)


