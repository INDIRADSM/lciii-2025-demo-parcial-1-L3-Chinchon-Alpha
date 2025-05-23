package tup.lciii.controlador;

import tup.lciii.modelo.*;
import tup.lciii.vista.VistaConsola;

import java.util.ArrayList;

public class Controlador {
    VistaConsola vista;
    Juego juego;

    public Controlador(VistaConsola vista, Juego juego) {
        this.vista = vista;
        this.juego = juego;
        this.cicloDeJuego();
    }

    public void cicloDeJuego() {
        boolean finDeJuego = false;

        while (!finDeJuego) {
            String opcion = vista.mostrarMenuJuegoOSalgo();
            while (!juego.validarMenuJuegoOSalgo(opcion)) {
                opcion = vista.mostrarMenuJuegoOSalgo();
                vista.opcionInvalida();
            }
            if (opcion.equals("1")) {
                this.partida();
            } else if (opcion.equals("2")) {
                finDeJuego = true;
            } else {
                vista.opcionInvalida();
            }
        }
    }

    /**
     * Este método simula una partida completa del juego. Inicializa el mazo, la mesa y los jugadores,
     * realiza varias rondas del juego y determina un ganador cuando un jugador alcanza 100 puntos.
     */
    public void partida() {
       Juego juego = new Juego();
       juego.iniciarMazo();

        //this.juego.iniciarMazo();
        Mazo mazo = juego.getMazo();

        Mesa mesa = juego.getMesa();

        ArrayList<String> nombresDeJugadores = vista.mostrarMensajeCantidadJugadores(juego.getCantidadJugadores());
        //modificacion en el control
        if (nombresDeJugadores.isEmpty()) {
            System.out.println("El juego no puede comenzar sin jugadores.");
            return;
        }
        //moficacion en el control

        juego.iniciarJugadores(nombresDeJugadores,juego.getCantidadJugadores());
        ArrayList<Jugador> jugadores = juego.getJugadores();
        //reinicia el puntaje a 0 en cada partida
        for (Jugador jugador : jugadores) {
            jugador.setPuntos(0);
        }
        vista.mostrarJugadores(jugadores);

        boolean ganador, partida = false;


        while (!partida) {
            //descombina todas las cartas de la ronda pasada
            mazo.descombinarTodasLasCartas();

            vista.mostrarMensajeNuevaRonda();

            this.ronda(juego, mesa, mazo, jugadores);

            //finalizada la ronda, se cuentan los puntos y se vacian las manos
            for (Jugador jugador : jugadores) {
                jugador.getMano().setearPuntaje(jugador);
                jugador.getMano().vaciarMano(mazo);
            }

            //se revisa si algun jugador llego a 100 puntos
            ganador = juego.revisarGanador(jugadores);
            if (ganador) {
                Jugador jugadorGanador = juego.obtenerGanador();
                vista.mostrarGanador(jugadorGanador);
                vista.mostrarPuntajes(jugadores);
                partida = true;
            }
        }
    }

    /**
     * Este método simula una ronda del juego. Inicializa la mesa y reparte cartas a los jugadores.
     * Luego, se ejecutan turnos de los jugadores, donde pueden levantar cartas del mazo o de la mesa,
     * combinar cartas en su mano y jugar una carta en la mesa. Se verifica si un jugador puede cortar la partida.
     * La ronda termina cuando un jugador corta.
     *
     * @param juego El objeto que controla el flujo del juego.
     * @param mesa La mesa donde se colocan las cartas durante la ronda.
     * @param mazo El mazo del juego del cual se reparten las cartas.
     * @param jugadores La lista de jugadores participantes en la ronda.
     */
    public void ronda(Juego juego, Mesa mesa, Mazo mazo,  ArrayList<Jugador> jugadores) {
        juego.iniciarMesa();
        //reparte las manos de los jugadores
        juego.mezclarMazoYrepartirCartas();
        vista.mostrarMensajeInfoPerder();
        vista.mostrarPuntajes(jugadores);

        boolean ronda = false;
        while (!ronda) {
            vista.mostrarMesa(mesa);

            //turnos
            Jugador jugadorActual = juego.revisarTurno(jugadores);
            Mano mano = jugadorActual.getMano();

            //Turno de actual
            vista.mostrarTurnoDe(jugadorActual);

            //ordenar mano
            mano.ordenarPorPaloYNumero(mano);

            //mostrar su mano inicial
            vista.mostrarMano(jugadorActual, mano, "Mano de");

            //opciones de levantado
            String opcionLevantado = "0";

            while (!juego.validarMenuOpcionesDeLevantado(opcionLevantado)) {
                opcionLevantado = vista.mostrarOpcionesDeLevantado();
                while (!juego.validarMenuOpcionesDeLevantado(opcionLevantado)) {
                    vista.opcionInvalida();
                    opcionLevantado = vista.mostrarOpcionesDeLevantado();
                }
                if (opcionLevantado.equals("1")) {
                    Boolean siguenHabiendoCartas = jugadorActual.agarrarCartaDelMazo(mazo);
                    if (!siguenHabiendoCartas) {
                        vista.mostrarMensajeNoHayCartasEnElMazo();
                        opcionLevantado = "0";
                    }
                } else if (opcionLevantado.equals("2")) {
                    jugadorActual.agarrarCartaDeLaMesa(mesa);
                } else {
                    vista.opcionInvalida();
                }
            }

            //mostrar su mano con carta levantada
            vista.mostrarMano(jugadorActual, mano, "Mano actualizada de");

            //revisar si tiene combinaciones
            String bucleCombinaciones = "0";

            while (!juego.validarMenuBucleDeCombinaciones(bucleCombinaciones)) {
                bucleCombinaciones = vista.mostrarMenuBucleDeCombinaciones();

                if (bucleCombinaciones.equals("1")) {
                    String combinaciones = vista.mostrarMenuCombinaciones(jugadorActual);

                    if (combinaciones.equals("1")) {
                        ArrayList<Carta> cartasPorCombinar = new ArrayList<>();

                        int cantidadDeCartasPorCombinar = validadInputCantidadDeCartas();

                        vista.mostrarMensajeCombinacionEscalera();

                        for (int i = 0; i < cantidadDeCartasPorCombinar; i++) {
                            Carta cartaValida = this.inputCarta(jugadorActual);
                            cartasPorCombinar.add(cartaValida);
                        }

                        Boolean escalera = mano.combinacionEscalera(cartasPorCombinar);

                        if (escalera) {
                            mano.setCombinacionesEscalera(cartasPorCombinar);
                        } else {
                            vista.mostrarMensajeNoEsEscalera();
                        }

                    } else if (combinaciones.equals("2")) {
                        ArrayList<Carta> cartasPorCombinar = new ArrayList<>();

                        int cantidadDeCartasPorCombinar = validadInputCantidadDeCartas();

                        vista.mostrarMensajeCombinacionNumerosIguales();

                        for (int i = 0; i < cantidadDeCartasPorCombinar; i++) {
                            Carta cartaValida = this.inputCarta(jugadorActual);
                            cartasPorCombinar.add(cartaValida);
                        }

                        Boolean numerosIguales = mano.combinacionNumerosIguales(cartasPorCombinar);

                        if (numerosIguales) {
                            mano.setCombinacionesNumerosIguales(cartasPorCombinar);
                        } else {
                            vista.mostrarMensajeNoEsNumerosIguales();
                        }
                    } else {
                        bucleCombinaciones = "2";
                    }
                } else if (bucleCombinaciones.equals("2")) {
                    vista.mostrarMensajeNoCombinaciones();
                    bucleCombinaciones = "2";
                } else {
                    vista.opcionInvalida();
                }
            }

            //ordenar mano
            mano.ordenarPorPaloYNumero(mano);

            //mostrar su mano para que vea cual tirar
            vista.mostrarMano(jugadorActual, mano, "Mano actual de");

            //dejar su carta extra para quedarse con 7
            vista.mostrarMensajeDejeUnaCarta();

            Carta cartaValida = this.inputCarta(jugadorActual);

            mano.jugarCarta(cartaValida);
            mesa.agregarCartaALaMesa(cartaValida);

            //revisa si al haber dejado la carta en la mesa, se deshizo una combinacion existente
            mano.revisarSiSeDescombino();

            //ordenar mano
            mano.ordenarPorPaloYNumero(mano);

            //mostrar como queda su mano al final del turno
            vista.mostrarMano(jugadorActual, mano, "Mano de final de ronda de");

            //revisa si tiene la opcion de cortar
            Boolean corta = juego.revisarSiCorta(jugadorActual);
            if (corta) {
                //pregunta si desea cortar
                String opcionCortar = vista.mostrarMenuCortar();
                if (juego.validarMenuOpcionCortar(opcionCortar)) {
                    mesa.vaciarMesa(mazo);
                    ronda = true;
                }
            }

            //cambio de turnos
            Jugador jugadorSiguiente = juego.revisarNoTurno(jugadores);
            jugadorActual.setEsTurno(false);
            jugadorSiguiente.setEsTurno(true);

        }
    }

    /**
     * Este método solicita al jugador la entrada de una carta válida y la valida.
     *
     * @param jugadorActual El jugador que debe ingresar la carta.
     * @return La carta válida ingresada por el jugador.
     */
    public Carta inputCarta(Jugador jugadorActual) {
        String cartaPorJugar = vista.inputCarta(jugadorActual);

        Carta cartaValida = juego.validarIngresoCarta(jugadorActual, cartaPorJugar);

        while (cartaValida == null) {
            cartaPorJugar = vista.inputCarta(jugadorActual);
            cartaValida = juego.validarIngresoCarta(jugadorActual, cartaPorJugar);
        }

        return cartaValida;
    }

    public int validadInputCantidadDeCartas() {
        int cantidadDeCartasPorCombinar = vista.inputNumeroDeCartas();

        while(!juego.validarMenuCantidadDeCartasPorCombinar(cantidadDeCartasPorCombinar)) {
            cantidadDeCartasPorCombinar = vista.inputNumeroDeCartas();
        }

        return cantidadDeCartasPorCombinar;
    }
}