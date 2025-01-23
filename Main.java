import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String input = "input.txt";
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        boolean file = true;
        boolean show = false;
        Manager manager = new Manager(4, 0.5, 0.5);

        System.out.println("Odczyt z pliku?\n1)Tak\n2)Nie");
        int choice = scanner.nextInt();
        if (choice == 2)
            file = false;
        System.out.println("Pokaż plik po każdej operacji?\n1)Tak\n2)Nie");
        choice = scanner.nextInt();
        if (choice == 1)
            show = true;

        int key;
        Record record;
        Double data1, data2;


        if (!file) {
            while (running) {
                System.out.println("Wybierz opcję: ");
                System.out.println("1. Dodaj rekord");
                System.out.println("2. Odczytaj record");
                System.out.println("3. Wyświetl plik");
                System.out.println("4. Reorganizuj");
                System.out.println("5. Zakończ");

                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        System.out.print("Podaj klucz: ");
                        key = scanner.nextInt();
                        System.out.print("Podaj dane: ");
                        data1 = scanner.nextDouble();
                        data2 = scanner.nextDouble();
                        record = new Record(key, new Pair<>(data1, data2));
                        manager.addRecord(record);
                        if(show)
                            manager.display();
                        break;
                    case 2:
                        System.out.print("Podaj klucz: ");
                        key = scanner.nextInt();
                        record = manager.findRecord(key);
                        if(record != null)
                            System.out.println("Rekord: " +  record );
                        break;
                    case 3:
                        manager.display();
                        break;
                    case 4:
                        manager.reorganize();
                        if(show)
                            manager.display();
                        break;
                    case 5:
                        running = false;
                        break;
                    default:
                        System.out.println("Nieznana komenda.");
                        break;
                }
                manager.displayStats();
            }
        }
        else{
            try (BufferedReader br = new BufferedReader(new FileReader(input))) {
                String line;
                while ((line = br.readLine()) != null) {
                    switch (line.trim()) {
                        case "Add":
                            line = br.readLine();
                            key = Integer.parseInt(line);
                            line = br.readLine();
                            data1 = Double.parseDouble(line);
                            line = br.readLine();
                            data2 = Double.parseDouble(line);
                            System.out.println("* Dodaj " + key + ", ( " + data1 + ", "+ data2 + " )");
                            record = new Record(key, new Pair<>(data1, data2));
                            manager.addRecord(record);
                            if(show)
                                manager.display();
                            break;
                        case "Find":
                            line = br.readLine();
                            key = Integer.parseInt(line);
                            System.out.println("* Odczytaj " + key);
                            record = manager.findRecord(key);
                            if(record != null)
                                System.out.println("Rekord: " +  record );
                            break;
                        case "Display":
                            System.out.println("* Wyświetl");
                            manager.display();
                            break;
                        case "Reorganize":
                            System.out.println("* Reorganizuj");
                            manager.reorganize();
                            if(show)
                                manager.display();
                            break;
                        default:
                            System.out.println("* Nieznana komenda.");
                            break;
                    }
                    manager.displayStats();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        scanner.close();
    }
}
