import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;

interface ReservationAuditor {
    void auditReservation(TicketReservation reservation);
}

// Klasa Concert reprezentuje koncert i zawiera informacje takie jak nazwa, liczba dostępnych miejsc i cena.
class Concert{
    private String name;
    private int availableSeats;
    private double price;

    public Concert(String name, int availableSeats, double price) {
        this.name = name;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public void reserveSeat() {
        availableSeats--;
    }

/*    public void displayConcert() {
        System.out.println("Koncert: " + name + ", Ilosc biletow: " + availableSeats + ", Cena: " + price);
    }*/

}
//abstract Ticket - polimorfizm
// Klasa Ticket jest klasą abstrakcyjną i zawiera informacje o koncercie, cenie biletu i numerze biletu.
abstract class Ticket {
    protected Concert concert;
    protected double ticketPrice;
    protected int ticketNumber;

    public Ticket(Concert concert, int ticketNumber, double ticketPrice) {
        this.concert = concert;
        this.ticketNumber = ticketNumber;
        this.ticketPrice = ticketPrice;
    }

    public abstract double getPrice();

    /*  public abstract void displayTicket();*/

    public String getConcertName() {
        return concert.getName();
    }

    public int getConcertSeats() {
        return concert.getAvailableSeats();
    }

    public double getConcertPrice() {
        return concert.getPrice();
    }
}
//dziedziczenie extends
// Klasa NormalTicket dziedziczy po klasie Ticket i implementuje metody getPrice() i displayTicket().
class NormalTicket extends Ticket {
    public NormalTicket(Concert concert, int ticketNumber, double ticketPrice) {
        super(concert, ticketNumber, ticketPrice);
    }

    @Override
    public double getPrice() {
        return ticketPrice;
    }

}

// Klasa FFTicket dziedziczy po klasie Ticket i implementuje metody getPrice() i displayTicket().
// Dodatkowo zawiera prywatną zmienną vipDiscount, która reprezentuje zniżkę dla biletów For Friends.
class FFTicket extends Ticket {
    private double vipDiscount;

    public FFTicket(Concert concert, int ticketNumber, double ticketPrice, double vipCost) {
        super(concert, ticketNumber, ticketPrice);
        this.vipDiscount = vipCost;
    }

    @Override
    public double getPrice() {
        return ticketPrice - (ticketPrice * vipDiscount);
    }

}

// Klasa TicketReservation zawiera listę biletów i metody do dodawania, wyświetlania i tworzenia raportów.
// Reprezentuje kolekcję biletów na koncert.
class TicketReservation {
    private List<Ticket> tickets = new ArrayList<>();
    private static int nextTicketNumber = 1;
    private String customerEmail;

    public String getCustomerEmail() {
        return customerEmail;
    }

    // Klasa wewnętrzna ReservationReport
    private class ReservationReport {
        private String reportDetails;

        public ReservationReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("Raport Rezerwacji: \n");
            for (Ticket ticket : tickets) {
                sb.append("Bilet: ").append(ticket.getConcertName())
                        .append(", Cena: ").append(ticket.getPrice()).append("\n");
            }
            reportDetails = sb.toString();
        }

        public void displayReport() {
            System.out.println(reportDetails);
        }
    }

    public void setCustomerEmail(String email) {
        this.customerEmail = email;
    }

    public void addTicket(Ticket ticket) {
        ticket.ticketNumber = nextTicketNumber++;
        tickets.add(ticket);
        createReport(ticket);
    }

    public double getTotalPrice() {
        double totalPrice = 0;
        for (Ticket ticket : tickets) {
            totalPrice += ticket.getPrice();
        }
        return totalPrice;
    }

    public List<Ticket> getTickets() {
        return this.tickets;
    }

    // Metoda do wyświetlania raportu rezerwacji
    public void displayReservationReport() {
        ReservationReport report = new ReservationReport();
        System.out.println("Email klienta: " + customerEmail);
        report.displayReport();
    }

    // wyjatek
    public void createReport(Ticket ticket) {
        try (PrintWriter reportFile = new PrintWriter(new FileWriter("raport.txt", true))) {
            reportFile.println("Raport rezerwacji biletu:");
            reportFile.println("Email klienta: " + customerEmail);
            reportFile.println("Nazwa koncertu: " + ticket.getConcertName() + "\t");
            reportFile.println("Typ biletu: " + ticket.getClass().getSimpleName());
            reportFile.println();
        } catch (IOException e) {      //exception, wyjatek
            System.out.println("Nie można utworzyć raportu: " + e.getMessage());
        }

        try (PrintWriter concertFile = new PrintWriter(new FileWriter("concerts.txt", true))) {
            double price = ticket.getConcertPrice();
            double seats = ticket.getConcertSeats()-1;
            String formattedPrice;
            String formattedSeats;
            formattedPrice = String.format("%d", (long) price);
            formattedSeats = String.format("%d", (long) seats);
            concertFile.println(ticket.getConcertName() + " " + formattedSeats + " " + formattedPrice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Main {

    // Metoda do aktualizacji listy koncertów
    private static void updateConcertsList(JTextArea concertsArea, Map<String, Concert> concerts) {
        StringBuilder concertsInfo = new StringBuilder();
        for (Concert concert : concerts.values()) {
            concertsInfo.append(concert.getName())
                    .append(" - Cena: ").append(concert.getPrice())
                    .append(", Miejsca: ").append(concert.getAvailableSeats())
                    .append("\n");
        }
        concertsArea.setText(concertsInfo.toString());
    }

    public static void main(String[] args) {

        // Wczytanie koncertów z pliku
        Map<String, Concert> concerts = new HashMap<>();
        try (Scanner fileScanner = new Scanner(new File("concerts.txt"))) {
            while (fileScanner.hasNext()) {
                String name = fileScanner.next();
                int availableSeats = fileScanner.nextInt();
                double price = fileScanner.nextDouble();
                concerts.put(name, new Concert(name, availableSeats, price));

            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Nie znaleziono pliku concerts.txt: " + e.getMessage());
            return;
        }

        // Utworzenie i konfiguracja okna
        JFrame frame = new JFrame("Kupno biletów na koncert");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        // Elementy GUI
        JComboBox<String> concertList = new JComboBox<>(concerts.keySet().toArray(new String[0]));
        JButton reserveButton = new JButton("Kup bilet");
        JTextField ticketTypeField = new JTextField(10);


        // Dodanie elementów do okna
        frame.add(new JLabel("Wybierz koncert:"));
        frame.add(concertList);
        frame.add(new JLabel("Typ biletu (zwykly/FF):"));
        frame.add(ticketTypeField);
        frame.add(reserveButton);

        JTextField emailField = new JTextField(20);
        frame.add(new JLabel("Email:"));
        frame.add(emailField);

        JTextArea concertsArea = new JTextArea(10, 30);
        concertsArea.setEditable(false);
        StringBuilder concertsInfo = new StringBuilder();
        for (Concert concert : concerts.values()) {
            concertsInfo.append(concert.getName())
                    .append(" - Cena: ").append(concert.getPrice())
                    .append(", Miejsca: ").append(concert.getAvailableSeats())
                    .append("\n");
        }
        concertsArea.setText(concertsInfo.toString());
        frame.add(new JScrollPane(concertsArea));

        // Tworzenie obiektu klasy anonimowej implementującej ReservationAuditor
        ReservationAuditor auditor = new ReservationAuditor() {
            @Override
            public void auditReservation(TicketReservation reservation) {
                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("reservation_audit.txt", true)))) {
                    out.println("Rezerwacja dla: " + reservation.getCustomerEmail());
                    for (Ticket ticket : reservation.getTickets()) {
                        out.println("  Bilet: " + ticket.getConcertName() + ", Cena: " + ticket.getPrice());
                    }
                    out.println("  Łączna cena: " + reservation.getTotalPrice());
                    out.println("----------------------------------------");
                } catch (IOException e) {
                    System.out.println("Błąd podczas zapisywania audytu rezerwacji: " + e.getMessage());
                }
            }
        };

        // Logika rezerwacji biletu
        //Klasa anonimowa implementująca interfejs ActionListener jest używana
        //do zdefiniowania akcji, która ma zostać wykonana po kliknięciu przycisku
        //"Rezerwuj bilet". W momencie kliknięcia, metoda actionPerformed zostanie wywołana.

        reserveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                if (email == null || email.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Proszę podać adres email.");
                    return; // Powstrzymuje dalsze działanie, jeśli email nie został podany
                }
                String concertName = (String) concertList.getSelectedItem();
                String ticketType = ticketTypeField.getText();
                Concert concert = concerts.get(concertName);

                // Wywołanie polimorficzne w main
                // Tutaj metoda addTicket klasy TicketReservation przyjmuje obiekty zarówno NormalTicket, jak i FFTicket,
                // pomimo że jej sygnatura wskazuje tylko na obiekty klasy bazowej Ticket.
                // Dzięki polimorfizmowi można wywoływać tę metodę, przekazując różne implementacje klasy Ticket.
                if (concert != null && concert.getAvailableSeats() > 0) {
                    TicketReservation reservation = new TicketReservation();
                    reservation.setCustomerEmail(email);
                    if ("zwykly".equals(ticketType)) {
                        reservation.addTicket(new NormalTicket(concert, 1, concert.getPrice())); //NormalTicket i FFT są dodawane do obiektu klasy TicketReservation
                        concert.reserveSeat();
                    } else if ("FF".equals(ticketType)) {
                        reservation.addTicket(new FFTicket(concert, 1, concert.getPrice(), 0.2));
                        concert.reserveSeat();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Zly typ biletu.");
                        return;
                    }
                    updateConcertsList(concertsArea, concerts);

                    auditor.auditReservation(reservation);
                    // Wyświetl raport rezerwacji
                    reservation.displayReservationReport();

                    JOptionPane.showMessageDialog(frame, "Bilet zakupiony.\nSuma: " + reservation.getTotalPrice());
                } else {
                    JOptionPane.showMessageDialog(frame, "Brak wolnych miejsc lub niepoprawna nazwa koncertu.");
                }
            }
        });

        // Wyświetlenie okna
        frame.setVisible(true);
    }
}


