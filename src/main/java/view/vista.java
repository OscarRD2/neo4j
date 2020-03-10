/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

/**
 *
 * @author arnauu
 */
import daoNeo.Dao;
import enums.TipoIncidencia;
import exceptions.NeoExceptions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Empleado;
import model.Incidencia;

public class vista {

    private static Dao daoNeo = Dao.initNeo();
    private static Empleado logueado = null;

    public static void main(String[] args) {
        boolean exit = false;
        do {
            int menu;
            try {
                if (logueado != null) {
                    System.out.println("< Welcome " + logueado.getFullName() + " >");
                    System.out.println("1. Put incidence");
                    System.out.println("2. Modify your account");
                    System.out.println("3. See your oirigin incidences");
                    System.out.println("4. See your destination incidences");
                    System.out.println("5. Delete My single node");
                    System.out.println("6. Delete My Relationships only ");
                    System.out.println("0. Log off");
                    menu = InputAsker.askInt("Choose option", 0, 6);
                    switch (menu) {
                        case 1:
                            incidencia();
                            break;
                        case 2:
                            modify();
                            break;
                        case 3:
                            originIncidence();
                            break;
                        case 4:
                            destinationIncidence();
                            break;
                        case 5:
                            deleteSingleNodo(logueado);
                            break;
                        case 6:
                            deleteRelationsEmpleado(logueado);
                            break;
                        case 0:
                            System.out.println("Good bye !");
                            logueado = null;
                            break;
                    }
                } else {
                    initMenu();
                    menu = InputAsker.askInt("Choose option", 0, 2);
                    switch (menu) {
                        case 1:
                            login();
                            break;
                        case 2:
                            registration();
                            break;
                        case 0:
                            daoNeo.close();
                            System.out.println("Good bye! ");
                            exit = true;
                            break;
                    }
                }
            } catch (NeoExceptions | ParseException ex) {
                System.out.println(ex.getMessage());
            }
        } while (!exit);
    }

    
    private static void registration() throws NeoExceptions {
        String userName = InputAsker.askString("Username: ");
        if (daoNeo.existeEmpleado(userName)) {
            throw new NeoExceptions(NeoExceptions.EMPLEADO_EXISTS);
        }
        String pass = InputAsker.askString("Password: ");
        String pass2 = InputAsker.askString("Confirm password: ");
        if (!pass.equals(pass2)) {
            throw new NeoExceptions(NeoExceptions.NO_COINCIDEN);
        }

        String fullName = InputAsker.askString("Full name: ");
        String phone = InputAsker.askString("Phone: ");

        if (phone.length() != 9) {
            throw new NeoExceptions(NeoExceptions.INCORRECT_TLF);
        }
        Empleado e = new Empleado(userName, pass, fullName, phone);
        daoNeo.insertEmpleado(e);
        System.out.println("Registration correct");
    }

    private static void deleteSingleNodo(Empleado e) throws NeoExceptions {

        String pass = InputAsker.askString("Password: ");

        if (!pass.equals(e.getPassword())) {
            throw new NeoExceptions(NeoExceptions.NO_COINCIDEN);
        }

        daoNeo.deleteSingleNodeEmpleado(logueado);

        logueado = null;
        System.out.println("Delete Single Node correct");
    }

    private static void deleteRelationsEmpleado(Empleado e) throws NeoExceptions {

        String pass = InputAsker.askString("Password: ");

        if (!pass.equals(e.getPassword())) {
            throw new NeoExceptions(NeoExceptions.NO_COINCIDEN);
        }

        daoNeo.deleteRelationshipsEmpleado(logueado);

        logueado = null;
        System.out.println("Delete Relationships this Empleado correct");
    }

    private static void incidencia() throws NeoExceptions, ParseException {
        List<Empleado> returnEmpleados = daoNeo.returnEmpleados();
        for (int i = 0; i < returnEmpleados.size(); i++) {
            if (returnEmpleados.get(i).getUserName().equals(logueado.getUserName())) {
                returnEmpleados.remove(i);
            }
        }
        if (returnEmpleados.isEmpty()) {
            throw new NeoExceptions(NeoExceptions.NO_EXISTS_EMPLEADOS);
        }
        int id = InputAsker.askInt("Id incidence: ", 0, Integer.MAX_VALUE);
        if (daoNeo.existeIncidencia(id)) {
            throw new NeoExceptions(NeoExceptions.INCIDENCIA_EXISTS);
        }
        Empleado origen = logueado;
        int cont = 0;
        for (int i = 0; i < returnEmpleados.size(); i++) {
            cont++;
            System.out.println(cont + ". " + returnEmpleados.get(i).getUserName());
        }
        int des = InputAsker.askInt("Destination: ", 1, cont);
        Empleado destino = returnEmpleados.get(des - 1);
        String descr = InputAsker.askString("Description: ");
        System.out.println("1. Normal");
        System.out.println("2. Urgent");
        int esc = InputAsker.askInt("Choose type of incidence", 1, 2);
        TipoIncidencia tipo = null;
        switch (esc) {
            case 1:
                tipo = TipoIncidencia.NORMAL;
                break;
            case 2:
                tipo = TipoIncidencia.URGENTE;
                break;
        }
        Date d = new Date();
        Incidencia inci = new Incidencia(id, d, origen, destino, descr, tipo);
        daoNeo.insertIncidencia(inci, logueado);
        System.out.println("Incidence registred correctly");
    }

    private static void login() throws NeoExceptions {
        String userName = InputAsker.askString("Username: ");
        String pass = InputAsker.askString("Password: ");
        logueado = daoNeo.loguinEmpleado(userName, pass);
    }

    private static void modify() throws NeoExceptions {
        System.out.println("1. Fullname (" + logueado.getFullName() + ")");
        System.out.println("2. Phone (" + logueado.getPhone() + ")");
        System.out.println("3. Password");
        System.out.println("0. Cancel");
        int m = InputAsker.askInt("Choose option: ", 0, 3);
        switch (m) {
            case 0:
                System.out.println("Canceling...");
                break;
            case 1:
                String fullanme = InputAsker.askString("New fullname: ");
                logueado.setFullName(fullanme);
                daoNeo.modifyEmpleado(logueado);
                System.out.println("Modify correctly");
                break;
            case 2:
                String phone = InputAsker.askString("New phone: ");
                if (phone.length() != 9) {
                    throw new NeoExceptions(NeoExceptions.INCORRECT_TLF);
                }
                logueado.setPhone(phone);
                daoNeo.modifyEmpleado(logueado);
                System.out.println("Modify correctly");
                break;
            case 3:
                String pass = InputAsker.askString("New password: ");
                String pass2 = InputAsker.askString("Confirm new password: ");
                if (!pass.equals(pass2)) {
                    throw new NeoExceptions(NeoExceptions.NO_COINCIDEN);
                }
                logueado.setPassword(pass);
                daoNeo.modifyEmpleado(logueado);
                System.out.println("Modify correctly");
                break;
        }
    }

    private static void originIncidence() throws ParseException, NeoExceptions {
        List<Incidencia> incidenciaByOrigen = daoNeo.getIncidenciaByOrigen(logueado);
        if (incidenciaByOrigen.isEmpty()) {
            throw new NeoExceptions(NeoExceptions.INCIDENCE_BY_ORIGIN);
        }
        for (Incidencia i : incidenciaByOrigen) {
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            String d = format1.format(i.getDatetime());
            System.out.println("Id: " + i.getId() + " | Tipe: " + i.getTipoIncidencia() + " | Description: " + i.getDescripcion() + " | Destination: " + i.getDestino().getUserName() + " | Date: " + d);
        }
    }

    private static void destinationIncidence() throws NeoExceptions, ParseException {
        List<Incidencia> incidenciaByDestino = daoNeo.getIncidenciaBydestination(logueado);
        if (incidenciaByDestino.isEmpty()) {
            throw new NeoExceptions(NeoExceptions.INCIDENCE_BY_DESTINATION);
        }
        for (Incidencia i : incidenciaByDestino) {
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            String d = format1.format(i.getDatetime());
            System.out.println("Id: " + i.getId() + " | Tipe: " + i.getTipoIncidencia() + " | Description: " + i.getDescripcion() + " | Origin: " + i.getOrigen().getUserName() + " | Date: " + d);
        }
    }

    private static void initMenu() {
        System.out.println("<< NEO4J >>");
        System.out.println("1. Log in");
        System.out.println("2. Registration");
        System.out.println("0. Exit");
    }

}
