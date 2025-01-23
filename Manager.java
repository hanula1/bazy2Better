import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {
    private Index index = new Index();
    private int b = 4;
    private double beta = 0.0;
    private double alfa = 0.5;
    private String overflow = "overflow.txt";
    private String output = "output.txt";

    private int diskReads = 0;
    private int diskWrites = 0;
    private int overflowRecords = 0;
    private int recordsMainArea = 0;
    private int address = 0;

    Page page;
    Page overflowPage;


    public Manager(int b, double alfa, double beta) throws IOException {
        this.b = b;
        this.beta = beta;
        this.alfa = alfa;

        Files.deleteIfExists(Paths.get(output));
        Files.createFile(Paths.get(output));
        Files.deleteIfExists(Paths.get(overflow));
        Files.createFile(Paths.get(overflow));

        page = new Page(0, b);
        Record guard = new Record(-1,new Pair<>(0.0,0.0));
        guard.setPosition(0);
        page.addRecord(guard);
        writePage(false);
        index.addIndexPair(page);
        overflowPage = new Page(0,b);
//        overflowPage = readPage(0, true);
//        overflowRecords=2;
    }

    private Page readPage(int id, boolean over) {
        Page p = new Page(id, b);

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader((over?overflow:output)))) {
            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                if (lineNumber > (b+1)*id && lineNumber < (b+1)*(id+1)) {
                    lines.add(line);
                }
                lineNumber++;
            }
            diskReads++;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(lines.isEmpty()) {
            return null;
        }

        String regex = "(\\d+)\\)\\s+K: (-?\\d+)\\s+V: \\[\\s*([\\d\\.]+),\\s*([\\d\\.]+)\\s*\\]\\s+D: (\\d+)\\s+O: (-|\\d+)\\s*";
        Pattern pattern = Pattern.compile(regex);

        String empty = "\\d+\\)\\t-\n";
        Pattern emptyPattern = Pattern.compile(empty);

        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = emptyPattern.matcher(lines.get(i));
            if (matcher.find()) {
                break;
            }
            matcher = pattern.matcher(lines.get(i));
            if (matcher.find()) {
                Integer position = Integer.parseInt(matcher.group(1));
                int key = Integer.parseInt(matcher.group(2));
                double data1 = Double.parseDouble(matcher.group(3));
                double data2 = Double.parseDouble(matcher.group(4));
                boolean deleted = matcher.group(5).equals("1");
                Integer overflowAddress = matcher.group(6).equals("-") ? null : Integer.parseInt(matcher.group(6));
                Record newRecord = new Record(key, new Pair<>(data1, data2), deleted, overflowAddress);
                newRecord.setPosition(position);
                p.addRecord(newRecord);
//                System.out.println(i + " record");
//                System.out.println("Key: " + key);
//                System.out.println("Data: [" + data1 + ", " + data2 + "]");
//                System.out.println("Deleted: " + deleted);
//                System.out.println("Overflow Address: " + overflowAddress + "\n");

            }
        }
        return p;
    }
    private void writePage(boolean over) {
        String tempFilePath = "temp.txt"; // Plik tymczasowy
        String filepath = (over?overflow:output);
        Page p = (over?overflowPage:page);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempFilePath), StandardOpenOption.CREATE)) {

            String line;
            int currentLine = 0;
            int min = (b+1)*p.getId();
            int recordNumber = 0;

            while ((line = reader.readLine()) != null) {
                if (currentLine == min) {
                    writer.write("Page " + p.getId());
                } else if (currentLine > min && currentLine <= min+b) {
                    int id = b * p.getId() + recordNumber;
                    if(p.getRecords().size()<=recordNumber) {
                        writer.write(id + ")\t-");
                    } else {
                        Record record = p.getRecords().get(recordNumber);
                        writer.write(id + ")\tK: " + record.getKey() +
                                "\tV: [ " + record.getData().getKey() +
                                ", " + record.getData().getValue() +
                                " ]\t\tD: " + (record.isDeleted() ? "1" : "0") +
                                "\tO: " + (record.getOverflow() == null ? "-" : record.getOverflow()));
                    }
                    recordNumber++;
                } else {
                    writer.write(line);
                }
                writer.newLine();
                currentLine++;
            }

            // Dodaj brakujące linie, jeśli plik był za krótki
            while (currentLine <= min+b) {
                if (currentLine == min) {
                    writer.write("Page " + p.getId());
                } else if (currentLine > min) {
                    int id = b * p.getId() + recordNumber;
                    if(p.getRecords().size()<=recordNumber) {
                        writer.write(id + ")\t-");
                    } else {
                        Record record = p.getRecords().get(recordNumber);
                        writer.write( id + ")\tK: " + record.getKey() +
                                "\tV: [ " + record.getData().getKey() +
                                ", " + record.getData().getValue() +
                                " ]\t\tD: " + (record.isDeleted() ? "1" : "0") +
                                "\tO: " + (record.getOverflow() == null ? "-" : record.getOverflow()));
                    }
                    recordNumber++;
                } else {
                    writer.write("");
                }
                writer.newLine();
                currentLine++;
            }
            diskWrites++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.move(Paths.get(tempFilePath), Paths.get(filepath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayFile(boolean over) {
        if (!over) {
            System.out.println("=== File ===");
        } else {
            System.out.println("=== Overflows ===");
        }
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(over?overflow:output))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("");
        diskReads++;
    }

    public void displayStats() {
        System.out.println("=== Stats ===");
        System.out.println("Disk reads: " + diskReads);
        System.out.println("Disk writes " + diskWrites);
        System.out.println("");
    }
    public void display() {
        System.out.println(index);
        displayFile(false);
        displayFile(true);
    }

    private Record findOverflownRecord(Record record, int key) {
        int overflowID = record.getOverflow()/b;
        if(overflowPage.getId()!=overflowID) {
            overflowPage = readPage(overflowID, true);
        }
        int position = record.getOverflow()%b;
        Record overflow = overflowPage.getRecords().get(position);
        if(overflow == null) {
            System.out.println("Record not found");
            return null;
        }
        if (overflow.getKey()==key) {
            System.out.println("Record found");
            return overflow;
        }
        if(overflow.getKey() > key ||
                overflow.getOverflow() == null) {
            System.out.println("Record not found");
            return null;
        }
        return findOverflownRecord(overflow, key);
    }
    private boolean writeOverflow(Record recordOnPage, Record record) {
        if(recordOnPage.getOverflow()==null) {
            recordOnPage.setOverflow(overflowRecords);
            if(!recordOnPage.isInOverflow()) {
                writePage(false);
            }
            int id = (overflowRecords)/b;
            if((overflowRecords)%b==0) {
                writePage(true);
                overflowPage = new Page(id, b);
            }
            else if (overflowPage.getId()!=id) {
                writePage(true);
                overflowPage=readPage(id, true);
            }
            record.setPosition(overflowRecords);
            record.setInOverflow(true);
            overflowPage.addRecord(record);
            writePage(true);
            System.out.println("Added");
            overflowRecords++;
            reorganizationIfNeeded();
            return true;
        }
        else {
            int id = (recordOnPage.getOverflow())/b;
            if (overflowPage.getId()!=id) {
                overflowPage=readPage(id, true);
            }
            int position = recordOnPage.getOverflow()%b;
            Record overflowRecord = overflowPage.getRecords().get(position);
            if(overflowRecord.getKey()==record.getKey()) {
                System.out.println("Already exist");
                return false;
            }
            if(overflowRecord.getKey()>record.getKey()) {
                record.setOverflow(recordOnPage.getOverflow());
                record.setPosition(overflowRecords);
                record.setInOverflow(true);
                id = (overflowRecords)/b;
                if((overflowRecords)%b==0) {
                    writePage(true);
                    overflowPage = new Page(id, b);
                }
                else if (overflowPage.getId()!=id) {
                    writePage(true);
                    overflowPage=readPage(id, true);
                }
                overflowPage.addRecord(record);
                writePage(true);
                if(recordOnPage.isInOverflow()) {
                    id = (recordOnPage.getPosition())/b;
                    if (overflowPage.getId()!=id) {
                        overflowPage=readPage(id, true);
                    }
                    position = recordOnPage.getPosition()%b;
                    overflowPage.getRecords().get(position).setOverflow(overflowRecords);
                    writePage(true);
                }
                else {
                    recordOnPage.setOverflow(overflowRecords);
                    writePage(false);
                }
                overflowRecords++;
                System.out.println("Added");
                reorganizationIfNeeded();
                return true;
            } else {
                return writeOverflow(overflowRecord, record);
            }
        }
    }

    public Record findRecord(int key) {
        if(key<0){
            System.out.println("Key cannot be less then 0");
            return null;
        }
        int pageID = index.findPage(key);
        if(pageID!=page.getId()) {
            page = readPage(pageID, false);
        }
        if(page == null) {
            System.out.println("Page is not existing");
            return null;
        }
        Record record = page.getRecord(key);
        if(record == null) {
            System.out.println("Record not found");
            return null;
        }
        if(record.getKey()==key) {
            System.out.println("Record found");
            return record;
        }
        if(record.getOverflow()==null) {
            System.out.println("Record not found");
            return null;
        }
        return findOverflownRecord(record, key);
    }

    private void reorganizationIfNeeded() {
        double ratio = (double)overflowRecords/recordsMainArea;
        if(ratio>beta)
            reorganize();
    }
    public boolean addRecord(Record record) {
        if(record.getKey()<0){
            System.out.println("Key cannot be less then 0");
            return false;
        }
        int pageID = index.findPage(record.getKey());
        if(pageID!=page.getId()) {
            page = readPage(pageID, false);
        }
        Record recordOnPage = page.getRecord(record.getKey());
        if(recordOnPage.getKey()==record.getKey()) {
            System.out.println("Already added");
            return false;
        }

        if(!page.isFull() && page.getSize() == recordOnPage.getPosition()%b + 1) {
            page.addRecord(record);
            writePage(false);
            System.out.println("Added " + record);
            recordsMainArea++;
            reorganizationIfNeeded();
            record.setPosition(recordOnPage.getPosition()+1);
            return true;
        }
        return writeOverflow(recordOnPage, record);
    }

    private void writeToNewFile(Page buffer, int records) {
        String newFile = "newFile.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, true))) {
            writer.write("Page " + buffer.getId());
            writer.newLine();
            int i = 0;
            for (; i < buffer.getSize(); i++) {
                Record record = buffer.getRecords().get(i);
                int id = b * buffer.getId() + i;
                writer.write( id + ")\tK: " + record.getKey() +
                        "\tV: [ " + record.getData().getKey() +
                        ", " + record.getData().getValue() +
                        " ]\t\tD: " + (record.isDeleted() ? "1" : "0") +
                        "\tO: " + (record.getOverflow() == null ? "-" : record.getOverflow()));
                writer.newLine();
            }
            for (; i < b; i++) {
                int id = b * buffer.getId() + i;
                writer.write(id + ")\t-");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        diskWrites++;
    }
    private Record getNextRecord(Integer a, boolean over) {
        Page newPage = over?overflowPage:page;
        int ID = a/b;
        int position = a%b;
        if(newPage.getId()!=ID) {
            newPage = readPage(ID,over);
        }
        return newPage.getRecords().get(position);
    }
    public boolean reorganize() {
        System.out.println("Reorganization");
        System.out.println("Before");
        display();
        int size = (int) Math.ceil(alfa * b);
        address = 0;
        int records = 1;
        int lastSavedPage = -1;
        boolean nextIsOverflow = false;
        int nextOverflowAddress = 0;
        Page buffer = new Page(0, 4);
        if (page.getId() != 0) {
            page = readPage(0, false);
        }
        Record record = page.getRecords().get(0);
        if(record.getOverflow()!=null) {
            nextIsOverflow = true;
            nextOverflowAddress = record.getOverflow();
            record.setOverflow(null);
        }
        buffer.addRecord(record);
        index.clearIndex();
        recordsMainArea += overflowRecords;
        overflowRecords = 0;

        while(recordsMainArea-records>=0) {
            if(nextIsOverflow) {
                record = getNextRecord(nextOverflowAddress, true);
            }
            else {
                address++;
                int id = address/b;
                if(page.getId()!=id)
                    page = readPage(id, false);
                if(address%b==page.getSize()) {
                    address += (b - page.getSize());
                }
                record = getNextRecord(address, false);
            }

            if(record.getOverflow()!=null) {
                nextIsOverflow = true;
                nextOverflowAddress = record.getOverflow();
                record.setOverflow(null);
            }
            else {
                nextIsOverflow = false;
            }
            buffer.addRecord(record);
            records++;

            if(buffer.getSize()==size) {
                writeToNewFile(buffer, records);
                lastSavedPage++;
                index.addIndexPair(buffer);
                buffer = new Page(lastSavedPage+1,b);
            }
        }
        if(buffer.getSize()!=0) {
            writeToNewFile(buffer, records);
            index.addIndexPair(buffer);
        }

        try {
            Files.move(Paths.get("newFile.txt"), Paths.get(output), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(Paths.get(overflow));
            Files.createFile(Paths.get(overflow));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("After");
        page = readPage(0,false);
        return true;
    }
}
