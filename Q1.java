import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableColumn;

@SuppressWarnings("unused")

class ButtonRenderer extends DefaultTableCellRenderer {
    private JButton button;

    public ButtonRenderer(JButton b) {
        button = b;
        button.setText("Read");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (isSelected) {
            button.setBackground(Color.DARK_GRAY);
            button.setForeground(Color.blue);
        } else {
            button.setBackground(table.getBackground());
            button.setForeground(table.getForeground());
        }

        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        int columnWidth = tableColumn.getPreferredWidth();
        int lastColumn = table.getColumnCount();
        button.setPreferredSize(new Dimension(columnWidth, button.getPreferredSize().height));
        return button;
    }
}

public class Q1 extends JFrame {
    private JFrame contentFrame;
    private Library library;
    private JPanel mainHeadingPanel;
    private JPanel tablePanel;
    JTable availableItemsTable;
    JScrollPane pane;
    static Q1 Q = new Q1();
    boolean check = false;

    public static Q1 GetInstance() {
        return Q;
    }

    public static void main(String[] args) {

    }

    private Q1() {
        this.displayFrame();
    }

    public void displayFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (contentFrame == null) {
            contentFrame = new JFrame("Library Management System");
        }

        mainHeadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel mainHeadingLabel = new JLabel("Welcome to library management system");
        mainHeadingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
        mainHeadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainHeadingPanel.add(mainHeadingLabel);
        contentFrame.add(mainHeadingPanel, BorderLayout.NORTH);
        if (check == false) {
            library = new Library();
            check = true;
            if (!library.loadFile()) {
                JLabel errorMessageLabel = new JLabel("Unable to read data");
                errorMessageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                errorMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                contentFrame.add(errorMessageLabel, BorderLayout.CENTER);
            }
        }
        // Displaying all the items
        tablePanel = new JPanel(new BorderLayout());
        JPanel tableHeadingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel tableHeadingLabel = new JLabel("Available Items");
        tableHeadingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        tableHeadingLabel.setHorizontalAlignment(SwingConstants.LEFT);
        tableHeadingPanel.add(tableHeadingLabel);
        tablePanel.add(tableHeadingPanel, BorderLayout.NORTH);
        displayItemsInTable(tablePanel, mainHeadingPanel);

        // adding buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Item");
        JButton editButton = new JButton("Edit Item");
        JButton deleteButton = new JButton("Delete Item");
        JButton viewpopularityButton = new JButton("View Popularity");
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(viewpopularityButton);
        contentFrame.add(buttonsPanel, BorderLayout.SOUTH);
        ButtonEventHandler actionHandler = new ButtonEventHandler(library, this);
        addButton.addActionListener(actionHandler);
        editButton.addActionListener(actionHandler);
        deleteButton.addActionListener(actionHandler);
        viewpopularityButton.addActionListener(actionHandler);

        contentFrame.setResizable(true);
        contentFrame.setSize(700, 500);
        contentFrame.setVisible(true);
    }

    public void displayOutsideclass() {
        contentFrame.getContentPane().removeAll();
        displayFrame();
    }

    public void displayItemsInTable(JPanel tablePanel, JPanel mainHeadingPanel) {
        ArrayList<Item> itemList = library.getItemsList();
        if (itemList == null || itemList.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No Items available in the library");
            noItemsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
            noItemsLabel.setHorizontalAlignment(SwingConstants.CENTER);

            contentFrame.getContentPane().removeAll();
            contentFrame.add(mainHeadingPanel, BorderLayout.NORTH);
            contentFrame.add(noItemsLabel, BorderLayout.CENTER);

            contentFrame.revalidate();
            contentFrame.repaint();
            return;
        }

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Title");
        tableModel.addColumn("Author");
        tableModel.addColumn("Publisher");
        tableModel.addColumn("Year/Date");
        tableModel.addColumn("Popularity");
        tableModel.addColumn("Cost");
        tableModel.addColumn("Read");

        if (availableItemsTable != null) {
            contentFrame.getContentPane().remove(availableItemsTable);
            contentFrame.revalidate();
            contentFrame.repaint();
        }
        availableItemsTable = new JTable(tableModel);
        availableItemsTable.getTableHeader().setReorderingAllowed(false);
        if (pane != null) {
            contentFrame.remove(pane);
            contentFrame.revalidate();
            contentFrame.repaint();
        }
        pane = new JScrollPane(availableItemsTable);
        tablePanel.add(pane, BorderLayout.CENTER);
        contentFrame.add(tablePanel, BorderLayout.CENTER);

        availableItemsTable.addMouseMotionListener(new MouseMotionAdapter() {
            private int lastHoveredRow = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = availableItemsTable.rowAtPoint(e.getPoint());
                if (row != lastHoveredRow) {
                    if (lastHoveredRow >= 0) {
                        availableItemsTable.removeRowSelectionInterval(lastHoveredRow, lastHoveredRow);
                    }
                    if (row >= 0) {
                        availableItemsTable.addRowSelectionInterval(row, row);
                    }
                    lastHoveredRow = row;
                } else if (row == -1) {
                    if (lastHoveredRow >= 0) {
                        availableItemsTable.removeRowSelectionInterval(lastHoveredRow, lastHoveredRow);
                        lastHoveredRow = -1;
                    }
                }
            }
        });

        availableItemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = availableItemsTable.rowAtPoint(e.getPoint());
                int column = availableItemsTable.columnAtPoint(e.getPoint());

                if (row >= 0 && column >= 7) {
                    System.out.println("Clicked on Row: " + row + ", Column: " + column);
                    System.out.println("Button clicked!");

                    int selectedRow = availableItemsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        Item selectedItem = itemList.get(selectedRow);
                        if (selectedItem != null) {
                            JFrame contentFrame = new JFrame("Content");
                            contentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                            JTextArea contentTextArea = new JTextArea(selectedItem.getContent());
                            contentTextArea.setWrapStyleWord(true);
                            contentTextArea.setLineWrap(true);
                            contentTextArea.setEditable(false);

                            contentFrame.add(new JScrollPane(contentTextArea));
                            contentFrame.setSize(400, 300);
                            contentFrame.setVisible(true);
                            contentFrame.addWindowListener(new WindowAdapter() {

                                @Override
                                public void windowClosing(WindowEvent e) {
                                    int result = JOptionPane.showConfirmDialog(contentFrame,
                                            "Are you sure you want to close this window?", "Confirm Close",
                                            JOptionPane.YES_NO_OPTION);
                                    if (result == JOptionPane.YES_OPTION) {
                                        contentFrame.dispose();
                                    }
                                }
                            });
                        }
                    }
                }
            }

        });

        availableItemsTable.getDefaultRenderer(JButton.class);

        ActionListener customActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button clicked!");
            }
        };
        print_values_in_the_table(itemList, availableItemsTable, tableModel);
        contentFrame.revalidate();
        contentFrame.repaint();
    }

    void print_values_in_the_table(ArrayList<Item> itemList, JTable availableItemsTable, DefaultTableModel tableModel) {

        for (Item item : itemList) {
            JButton button = new JButton();
            if (item instanceof Book) {
                Book book = (Book) item;
                Object[] row = new Object[] {
                        book.getID(),
                        book.getTitle(),
                        book.getAuthor(),
                        "-",
                        book.getYear(),
                        book.getPopularityCount(),
                        book.getCost(),
                        button
                };

                ButtonRenderer buttonRenderer = new ButtonRenderer(button);
                int lastColumn = availableItemsTable.getColumnCount() - 1;
                TableColumn column = availableItemsTable.getColumnModel().getColumn(lastColumn);
                column.setCellRenderer(buttonRenderer);
                tableModel.addRow(row);

            } else if (item instanceof Magazine) {
                Magazine magazine = (Magazine) item;
                tableModel.addRow(new Object[] {
                        magazine.getID(),
                        magazine.getTitle(),
                        magazine.getAuthors(),
                        magazine.getPublisher(),
                        "-",
                        magazine.getPopularityCount(),
                        magazine.getCost(),
                        button
                });

            } else if (item instanceof NewsPaper) {
                NewsPaper newspaper = (NewsPaper) item;
                tableModel.addRow(new Object[] {
                        newspaper.getID(),
                        newspaper.getTitle(),
                        "-",
                        newspaper.getPublisher(),
                        newspaper.getDate(),
                        newspaper.getPopularityCount(),
                        newspaper.getCost(),
                        button
                });
            }
        }

    }

    void refreshTable(JPanel tablePanel, JPanel mainHeadingPanel) {
        contentFrame.getContentPane().removeAll();
        displayItemsInTable(tablePanel, mainHeadingPanel);
        contentFrame.add(mainHeadingPanel, BorderLayout.NORTH);
        contentFrame.add(tablePanel, BorderLayout.CENTER);
        contentFrame.revalidate();
        contentFrame.repaint();
    }

    public JPanel getTablePanel() {
        return tablePanel;
    }

    public JPanel getMainHeadingPanel() {
        return mainHeadingPanel;
    }
}

interface Configuration {
    void display();

    int calculateCost();
}

abstract class Item implements Configuration {
    protected static int nextId = 0;
    protected int id;
    protected String title;
    protected boolean borrowed;
    protected int popularityCount;
    protected int cost;
    protected String content;

    Item(String content) {
        if (content.length() == 0) {
            System.out.println("Unable to create item");
            return;
        }
        id = nextId;
        nextId++;
        System.out.println("ITEM ID: " + id + " NEXT ID: " + nextId);
    }

    protected boolean verifyTitle(String title) {
        if (title != null && !title.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setTitle(String title) {
        if (verifyTitle(title)) {
            this.title = title;
        } else {
            this.title = "";
            System.out.println("Invalid title. Please provide a non-empty title.");
        }
    }

    public String getTitle() {
        return title;
    }

    public int getID() {
        return this.id;
    }

    public void setPopularityCount(int pop) {
        popularityCount = pop;
    }

    public int getPopularityCount() {
        return popularityCount;
    }

    public void setCost(int cost) {
        if (cost <= 0) {
            System.out.println("Invalid cost");
            return;
        }
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public abstract void display();

    public abstract int calculateCost();

    public void setContent(String str) {
        this.content = str;
    }

    public String getContent() {
        return content;
    }

    public void increasePopularityCount() {
        popularityCount++;
    }

    public void deleteItem() {
        nextId--;
    }
}

class Library {
    ArrayList<Item> itemList;
    File filedata;

    public Library() {
        itemList = new ArrayList<Item>();
    }

    public ArrayList<Item> getItemsList() {
        return itemList;
    }

    public void HotPicks() {
        int n = itemList.size();
        boolean swapped;
        do {
            swapped = false;
            for (int i = 1; i < n; i++) {
                if (itemList.get(i - 1).getPopularityCount() < itemList.get(i).getPopularityCount()) {
                    Item temp = itemList.get(i - 1);
                    itemList.set(i - 1, itemList.get(i));
                    itemList.set(i, temp);
                    swapped = true;
                }
            }
        } while (swapped);

        System.out.println("Hot Picks:");
        this.viewItems();

    }

    public boolean loadFile() {
        try {
            filedata = new File("data.txt");
            if (!filedata.exists()) {
                System.out.print("FILE NOT FOUND");
                return false;
            }
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Scanner myReader = new Scanner(filedata);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] values = data.split(" : ");

                if (values.length == 2) {
                    String[] itemData = values[0].split(", ");
                    String content = values[1];
                    int type = Integer.parseInt(itemData[0].trim());

                    if (type == 1) {
                        // Book
                        if (itemData.length == 6) {
                            String title = itemData[1].trim();
                            String author = itemData[2].trim();
                            int year = Integer.parseInt(itemData[3].trim());
                            int popularityCount = Integer.parseInt(itemData[4].trim());
                            int cost = Integer.parseInt(itemData[5].trim());
                            itemList.add(new Book(title, author, year, popularityCount, cost, content));
                        } else {
                            System.out.println("Incomplete data for a book");
                        }

                    } else if (type == 2) {
                        // Magazine
                        if (itemData.length >= 6) {
                            String title = itemData[1].trim();
                            String[] authors = new String[itemData.length - 5];
                            for (int i = 2; i < itemData.length - 3; i++) {
                                authors[i - 2] = itemData[i].trim();
                            }
                            String publisher = itemData[itemData.length - 3].trim();
                            int popularityCount = Integer.parseInt(itemData[itemData.length - 2].trim());
                            int cost = Integer.parseInt(itemData[itemData.length - 1].trim());

                            itemList.add(new Magazine(title, publisher, authors, popularityCount, cost, content));
                        } else {
                            System.out.println("Incomplete data for a magazine");
                        }
                    } else if (type == 3) {
                        // NewsPaper
                        if (itemData.length == 5) {
                            String title = itemData[1].trim();
                            String publisher = itemData[2].trim();
                            String pattern = "dd-MM-yyyy";
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                            int popularityCount = Integer.parseInt(itemData[3].trim());
                            String date = itemData[4].trim();
                            LocalDate localDate = LocalDate.parse(date, formatter);
                            itemList.add(new NewsPaper(title, publisher, popularityCount, localDate, 1, content));
                        } else {
                            System.out.println("Incomplete data for a newspaper");
                        }

                    } else {
                        System.out.println("Invalid item type");
                    }
                }

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return false;
        }

        if (itemList.size() == 0) {
            return false;
        }
        return true;
    }

    public void addItem(Item item) {
        itemList.add(item);
    }

    public boolean deleteItem(int ID) {
        for (Item items : itemList) {
            if (ID == items.getID()) {
                itemList.remove(items);
                items.deleteItem();
                System.out.println("The Item has been deleted");
                return true;
            }
        }
        System.out.println("Error: Book does not exist");
        return false;
    }

    public void viewItems() {
        if (itemList.isEmpty()) {
            System.out.println("No Items availabe in library");
            return;
        }
        for (Item items : itemList) {
            items.display();
        }
    }

    public void viewItemByID(int ID) {

        for (Item items : itemList) {
            if (ID == items.getID()) {
                items.display();
                return;
            }
        }
        System.out.println("Item with ID " + ID + " not found");
    }

    public void displayItem(Item I) {
        I.display();
    }

    public Item getItemByID(int ID) {
        for (Item items : itemList) {
            if (ID == items.getID()) {
                return items;
            }
        }
        System.out.println("Item with ID " + ID + " not found");
        return null;
    }

    public boolean save() {
        this.viewItems();
        try {
            FileWriter writer = new FileWriter(filedata);

            for (Item item : itemList) {
                if (item instanceof Book) {
                    Book book = (Book) item;
                    String data = "1, " + book.getTitle() + ", " + book.getAuthor() + ", " + book.getYear() + ", "
                            + book.getPopularityCount() + ", " + book.getCost() + " : " + book.getContent();
                    writer.write(data + "\n");
                } else if (item instanceof Magazine) {
                    Magazine magazine = (Magazine) item;
                    String authors = String.join(", ", magazine.getAuthors());
                    String data = "2, " + magazine.getTitle() + ", " + authors + ", " + magazine.getPublisher() + ", "
                            + magazine.getPopularityCount() + ", " + magazine.getCost() + " : " + magazine.getContent();
                    writer.write(data + "\n");
                } else if (item instanceof NewsPaper) {
                    NewsPaper newspaper = (NewsPaper) item;
                    String formattedDate = newspaper.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    String data = "3, " + newspaper.getTitle() + ", " + newspaper.getPublisher() + ", "
                            + newspaper.getPopularityCount() + ", " + formattedDate + " : " + newspaper.getContent();
                    writer.write(data + "\n");
                }
            }

            writer.close();
            System.out.println("\nData saved to data.txt.");
            return true;
        } catch (IOException e) {
            System.out.println("An error occurred while saving data.");
            e.printStackTrace();
            return false;
        }
    }

    public void exportToCSV(String filepath) {
        this.HotPicks();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("Item Name,Popularity Count\n");
            for (Item item : itemList) {
                writer.write(item.getTitle() + "," + item.getPopularityCount() + "\n");
            }
            System.out.println("\nData exported to " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Book extends Item {
    private String author;
    private int year;

    public Book(String t, String a, int y, int pop, int cos, String content) {
        super(content);
        setTitle(t);
        setAuthor(a);
        setYear(y);
        setPopularityCount(pop);
        setCost(cos);
        setContent(content);
    }

    private boolean verifyAuthor(String author) {
        if (author != null && !author.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setAuthor(String author) {
        if (verifyAuthor(author)) {
            this.author = author;
        } else {
            this.author = "";
            System.out.println("Invalid author name. Please provide a non-empty author name.");
        }
    }

    private boolean verifyYear(int year) {
        if (year >= 0 && (year <= (Year.now().getValue()))) {
            return true;
        } else {
            return false;
        }
    }

    public void setYear(int year) {
        if (verifyYear(year)) {
            this.year = year;
        } else {
            System.out.println("Invalid year. Please provide a valid year that is not in the future.");
        }
    }

    public void display() {
        System.out.print("\nID: " + id + " Title: " + title + " Author: " + author + " (" + year
                + ") Popularity: " + popularityCount + " Cost: " + cost);
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    @Override
    public int calculateCost() {
        int bookCost = (int) (10 + 0.2 * (getCost()) + 200);
        return bookCost;
    }
}

class Magazine extends Item {
    private String publisher;
    private ArrayList<String> authorList;

    public Magazine(String t, String p, String[] authorlist, int pop, int cos, String content) {
        super(content);
        authorList = new ArrayList<String>();
        setTitle(t);
        setPublisher(p);
        setAuthors(authorlist);
        setPopularityCount(pop);
        setCost(cos);
        setContent(content);
    }

    private boolean verifyPublisher(String publisher) {
        if (publisher != null && !publisher.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setPublisher(String publisher) {
        if (verifyPublisher(publisher)) {
            this.publisher = publisher;
        } else {
            this.publisher = "";
            System.out.println("Invalid publisher name. Please provide a non-empty publisher name.");
        }
    }

    public void setAuthors(String[] authors) {
        if (authors == null || authors.length == 0) {
            System.out.println("No authors found");
            return;
        } else {
            for (String author : authors) {
                if (author != null && !author.isEmpty()) {
                    authorList.add(author);
                } else {
                    System.out.println("Invalid Author");
                }
            }
            if (authorList.isEmpty()) {
                System.out.println("No valid authors found");
            }
        }
    }

    public void display() {
        System.out.print("\nID: " + id + " Title: " + title + " Publisher: " + publisher + " Authors: ");
        for (int i = 0; i < authorList.size(); i++) {
            System.out.print(authorList.get(i) + " ");
        }
        System.out.print(" Popularity: " + popularityCount + " Cost: " + cost);
    }

    public String getPublisher() {
        return publisher;
    }

    public String getAuthors() {
        String authorsString = String.join(", ", authorList);
        return authorsString;
    }

    @Override
    public int calculateCost() {
        int magazineCost = getCost() * getPopularityCount();
        return magazineCost;
    }
}

class NewsPaper extends Item {
    private String publisher;
    private LocalDate date;

    public NewsPaper(String t, String p, int pop, LocalDate y, int cost, String content) {
        super(content);
        setTitle(t);
        setPublisher(p);
        setPopularityCount(pop);
        setDate(y);
        setCost(cost);
        setContent(content);
    }

    private boolean verifyPublisher(String publisher) {
        if (publisher != null && !publisher.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setPublisher(String publisher) {
        if (verifyPublisher(publisher)) {
            this.publisher = publisher;
        } else {
            this.publisher = "";
            System.out.println("Invalid publisher name. Please provide a non-empty publisher name.");
        }
    }

    private boolean verifyDate(LocalDate date) {
        LocalDate now = LocalDate.now();
        if (!date.isAfter(now)) {
            return true;
        } else {
            return false;
        }
    }

    public void setDate(LocalDate date) {
        if (verifyDate(date)) {
            this.date = date;
        } else {
            System.out.println("Invalid date. Please provide a valid date that is not in the future.");
        }
    }

    public void display() {
        System.out.print("\nID: " + id + " Title: " + title + " Publisher: " + publisher + " (" + date
                + ") Popularity: " + popularityCount + " Cost: " + cost);
    }

    public String getPublisher() {
        return publisher;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public int calculateCost() {
        int newspaperCost = getCost();
        return newspaperCost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}

class AddItemGUI extends JFrame {
    private Library library;
    private Q1 mainFrame;

    private JPanel inputPanel;
    private JComboBox<String> itemTypeComboBox;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField authorsField;
    private JTextField yearField;
    private JTextField popularityField;
    private JTextField dateField;
    private JTextField costField;
    private JTextArea contentArea;
    private JButton saveButton;

    public AddItemGUI(Library library, Q1 mainFrame) {
        this.library = library;
        this.mainFrame = mainFrame;

        setTitle("Add Item");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        inputPanel = new JPanel(new GridLayout(10, 2));
        inputPanel.add(new JLabel("Select Item Type:"));
        String[] itemTypes = { "Book", "Magazine", "Newspaper" };
        itemTypeComboBox = new JComboBox<>(itemTypes);
        inputPanel.add(itemTypeComboBox);

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        inputPanel.add(authorField);

        inputPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField();
        inputPanel.add(publisherField);

        inputPanel.add(new JLabel("Authors (comma-separated):"));
        authorsField = new JTextField();
        inputPanel.add(authorsField);

        inputPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        inputPanel.add(yearField);

        inputPanel.add(new JLabel("Popularity Count:"));
        popularityField = new JTextField();
        inputPanel.add(popularityField);

        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        inputPanel.add(new JLabel("Cost:"));
        costField = new JTextField();
        inputPanel.add(costField);

        inputPanel.add(new JLabel("Content:"));
        contentArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(contentArea);
        inputPanel.add(scrollPane);

        hideAllFields();

        itemTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItemType = (String) itemTypeComboBox.getSelectedItem();
                hideAllFields();
                showRelevantFields(selectedItemType);
            }
        });

        add(inputPanel, BorderLayout.CENTER);

        saveButton = new JButton("Save");
        add(saveButton, BorderLayout.SOUTH);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItemType = (String) itemTypeComboBox.getSelectedItem();
                String title = titleField.getText();
                String author = authorField.getText();
                String publisher = publisherField.getText();
                String authorsInput = authorsField.getText();
                String[] authors = authorsInput.split(",");
                int year = 0;
                if (!yearField.getText().isEmpty()) {
                    year = Integer.parseInt(yearField.getText());
                }
                int popularityCount = Integer.parseInt(popularityField.getText());
                String dateText = dateField.getText();
                LocalDate date = null;
                if (!dateText.isEmpty()) {
                    date = LocalDate.parse(dateText);
                }
                int cost = Integer.parseInt(costField.getText());
                String content = contentArea.getText();

                if (selectedItemType.equals("Book")) {
                    if (titleField.getText().isEmpty() || authorField.getText().isEmpty()
                            || yearField.getText().isEmpty() || popularityField.getText().isEmpty()
                            || costField.getText().isEmpty() || content.isEmpty()) {
                        showErrorMessage("Please fill in all required fields.");
                        return;
                    } else {
                        Book book = new Book(title, author, year, popularityCount, cost, content);
                        library.addItem(book);
                    }
                } else if (selectedItemType.equals("Magazine")) {
                    if (titleField.getText().isEmpty() || publisherField.getText().isEmpty()
                            || authorsField.getText().isEmpty() || popularityField.getText().isEmpty()
                            || costField.getText().isEmpty() || content.isEmpty()) {
                        showErrorMessage("Please fill in all required fields.");
                        return;
                    } else {
                        Magazine magazine = new Magazine(title, publisher, authors, popularityCount, cost, content);
                        library.addItem(magazine);
                    }
                } else if (selectedItemType.equals("Newspaper")) {
                    if (titleField.getText().isEmpty() || publisherField.getText().isEmpty()
                            || dateField.getText().isEmpty() || popularityField.getText().isEmpty()
                            || costField.getText().isEmpty() || content.isEmpty()) {
                        showErrorMessage("Please fill in all required fields.");
                        return;
                    } else {
                        NewsPaper newspaper = new NewsPaper(title, publisher, popularityCount, date, cost, content);
                        library.addItem(newspaper);
                    }
                }

                library.save();
                dispose();
                mainFrame.displayOutsideclass();
            }
        });
    }

    private void hideAllFields() {
        titleField.setVisible(false);
        authorField.setVisible(false);
        publisherField.setVisible(false);
        authorsField.setVisible(false);
        yearField.setVisible(false);
        popularityField.setVisible(false);
        dateField.setVisible(false);
        costField.setVisible(false);
    }

    private void showRelevantFields(String itemType) {
        if ("Book".equals(itemType)) {
            titleField.setVisible(true);
            authorField.setVisible(true);
            yearField.setVisible(true);
            popularityField.setVisible(true);
            costField.setVisible(true);
        } else if ("Magazine".equals(itemType)) {
            titleField.setVisible(true);
            publisherField.setVisible(true);
            authorsField.setVisible(true);
            popularityField.setVisible(true);
            costField.setVisible(true);
        } else if ("Newspaper".equals(itemType)) {
            titleField.setVisible(true);
            publisherField.setVisible(true);
            dateField.setVisible(true);
            popularityField.setVisible(true);
            costField.setVisible(true);
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

class DeleteItemGUI extends JFrame {
    private Library library;
    private Q1 mainFrame;

    private JPanel inputPanel;

    public DeleteItemGUI(Library library, Q1 mainFrame) {
        this.library = library;
        this.mainFrame = mainFrame;

        setTitle("Edit Item");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        inputPanel = new JPanel(new GridLayout(9, 2));
        inputPanel.add(new JLabel("Enter ID:"));
        JTextField IDField = new JTextField();
        inputPanel.add(IDField);

        add(inputPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save");
        add(saveButton, BorderLayout.SOUTH);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ID = Integer.parseInt(IDField.getText());
                Item item = library.getItemByID(ID);
                if (item == null) {
                    showErrorMessage("ID not Found!");
                } else {
                    library.deleteItem(ID);
                    library.save();
                }
                dispose();
                mainFrame.displayOutsideclass(); // new
            }

        });
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

class EditItemGUI extends JFrame {
    private Library library;
    private Q1 mainFrame;
    private Item item;

    private JPanel inputPanel;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField authorsField;
    private JTextField yearField;
    private JTextField popularityField;
    private JTextField dateField;
    private JTextField costField;
    private JButton saveButton;

    public EditItemGUI(Library library, Q1 mainFrame) {
        this.library = library;
        this.mainFrame = mainFrame;

        setTitle("Edit Item");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        inputPanel = new JPanel(new GridLayout(10, 1));

        inputPanel.add(new JLabel("Enter ID:"));
        JTextField IDField = new JTextField();
        inputPanel.add(IDField);

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        inputPanel.add(authorField);

        inputPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField();
        inputPanel.add(publisherField);

        inputPanel.add(new JLabel("Authors (comma-separated):"));
        authorsField = new JTextField();
        inputPanel.add(authorsField);

        inputPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        inputPanel.add(yearField);

        inputPanel.add(new JLabel("Popularity Count:"));
        popularityField = new JTextField();
        inputPanel.add(popularityField);

        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        inputPanel.add(new JLabel("Cost:"));
        costField = new JTextField();
        inputPanel.add(costField);

        hideAllFields();

        add(inputPanel, BorderLayout.CENTER);
        JButton loadIDButton = new JButton("Load Item");
        inputPanel.add(loadIDButton);
        saveButton = new JButton("Save");
        add(saveButton, BorderLayout.SOUTH);

        loadIDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int ID = Integer.parseInt(IDField.getText());
                    item = library.getItemByID(ID);
                    if (item == null) {
                        showErrorMessage("ID not Found!");
                    } else {
                        if (item instanceof Book) {
                            titleField.setVisible(true);
                            authorField.setVisible(true);
                            yearField.setVisible(true);
                            popularityField.setVisible(true);
                            costField.setVisible(true);
                        } else if (item instanceof Magazine) {
                            titleField.setVisible(true);
                            publisherField.setVisible(true);
                            authorsField.setVisible(true);
                            popularityField.setVisible(true);
                            costField.setVisible(true);
                        } else if (item instanceof NewsPaper) {
                            titleField.setVisible(true);
                            publisherField.setVisible(true);
                            dateField.setVisible(true);
                            popularityField.setVisible(true);
                            costField.setVisible(true);
                        }
                    }
                } catch (NumberFormatException ex) {
                    showErrorMessage("Invalid ID format. Please enter a valid integer ID.");
                }
            }
        });

        add(inputPanel, BorderLayout.CENTER);

        saveButton = new JButton("Save");
        add(saveButton, BorderLayout.SOUTH);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String title = titleField.getText();
                    String author = authorField.getText();
                    String publisher = publisherField.getText();
                    String authorsInput = authorsField.getText();
                    String[] authors = authorsInput.split(",");
                    int year = 0;
                    if (!yearField.getText().isEmpty()) {
                        year = Integer.parseInt(yearField.getText());
                    }
                    int popularityCount = -1;
                    if (!popularityField.getText().isEmpty()) {
                        popularityCount = Integer.parseInt(popularityField.getText());
                    }
                    LocalDate date = null;
                    if (!dateField.getText().isEmpty()) {
                        date = LocalDate.parse(dateField.getText());
                    }
                    int cost = -1;
                    if (!costField.getText().isEmpty()) {
                        cost = Integer.parseInt(costField.getText());
                    }

                    if (item instanceof Book) {
                        if (!titleField.getText().isEmpty()) {
                            item.setTitle(title);
                        }
                        if (!authorField.getText().isEmpty()) {
                            ((Book) item).setAuthor(author);
                        }
                        if (!yearField.getText().isEmpty() || year != 0) {
                            ((Book) item).setYear(year);
                        }
                        if (!popularityField.getText().isEmpty() || popularityCount != -1) {
                            item.setPopularityCount(popularityCount);
                        }
                        if (!costField.getText().isEmpty() || cost != -1) {
                            item.setCost(cost);
                        }

                    } else if (item instanceof Magazine) {
                        if (!titleField.getText().isEmpty()) {
                            item.setTitle(title);
                        }
                        if (!publisherField.getText().isEmpty()) {
                            ((Magazine) item).setPublisher(publisher);
                        }
                        if (!authorsField.getText().isEmpty()) {
                            ((Magazine) item).setAuthors(authors);
                        }

                        if (!popularityField.getText().isEmpty() || popularityCount != -1) {
                            item.setPopularityCount(popularityCount);
                        }
                        if (!costField.getText().isEmpty() || cost != -1) {
                            item.setCost(cost);
                        }
                    } else if (item instanceof NewsPaper) {
                        if (!titleField.getText().isEmpty()) {
                            item.setTitle(title);
                        }
                        if (!publisherField.getText().isEmpty()) {
                            ((Magazine) item).setPublisher(publisher);
                        }
                        if (!dateField.getText().isEmpty() || date != null) {
                            ((NewsPaper) item).setDate(date);
                        }
                        if (!popularityField.getText().isEmpty() || popularityCount != -1) {
                            item.setPopularityCount(popularityCount);
                        }
                        if (!costField.getText().isEmpty() || cost != -1) {
                            item.setCost(cost);
                        }
                    }

                    library.save();
                    dispose();
                    mainFrame.displayOutsideclass();

                } catch (NumberFormatException ex) {
                    showErrorMessage("Invalid input format. Please check your inputs.");
                }
            }
        });
    }

    private void hideAllFields() {
        titleField.setVisible(false);
        authorField.setVisible(false);
        publisherField.setVisible(false);
        authorsField.setVisible(false);
        yearField.setVisible(false);
        popularityField.setVisible(false);
        dateField.setVisible(false);
        costField.setVisible(false);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

class ButtonEventHandler implements ActionListener {
    private Library library;
    private Q1 mainFrame;

    public ButtonEventHandler(Library library2, Q1 mainFrame) {
        this.library = library2;
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();

        if (source.getText().equals("Add Item")) {
            AddItemGUI addItemGUI = new AddItemGUI(library, mainFrame);
            addItemGUI.setVisible(true);
        } else if (source.getText().equals("Edit Item")) {
            EditItemGUI editItemGUI = new EditItemGUI(library, mainFrame);
            editItemGUI.setVisible(true);
        } else if (source.getText().equals("Delete Item")) {
            DeleteItemGUI deleteItemGUI = new DeleteItemGUI(library, mainFrame);
            deleteItemGUI.setVisible(true);
        } else if (source.getText().equals("View Popularity")) {
            library.exportToCSV("viewpopularitycount.csv");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", "display_barchart.py");
                Process process = processBuilder.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}