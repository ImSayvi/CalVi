package com.calvi.ui;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MonthView extends GridPane {
    private YearMonth currentMonth = YearMonth.now();

    public MonthView(){
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        ColumnConstraints fullWidth = new ColumnConstraints();
        fullWidth.setPercentWidth(100);
        getColumnConstraints().add(fullWidth);

        //tu buduję samo miejsce na wiersza (nawigacja, nazwy dni, klocki mieisąca)
        RowConstraints headerRow = new RowConstraints();
        RowConstraints daysRow = new RowConstraints();
        RowConstraints navRow = new RowConstraints();
        daysRow.setVgrow(Priority.ALWAYS);
        getRowConstraints().addAll(navRow, headerRow, daysRow);

        //a tu wypełniam  to miejsce treścią
        refresh();
    }

    private GridPane buildHeader(){
        String[] days = {"Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nd"};

        GridPane header = new GridPane();
        addEqualColumns(header);
        header.setPadding(new Insets(8));
        header.setStyle("-fx-background-color: #eeeeee;");

        for(int i=0; i<=6; i++){
            Label dayName = new Label(days[i]);
            dayName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            GridPane.setHalignment(dayName, HPos.CENTER);
            header.add(dayName, i, 0);
        }

        return header;
    }


    private GridPane buildDays(){
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startCol = firstOfMonth.getDayOfWeek().getValue() - 1; //na której kol wypada pierwszy dzień mieisąca; odejmuję 1 bo getvalue liczy od 1 a grid pane od 0
        int daysInMonth = currentMonth.lengthOfMonth();
        int prevMonthLength = currentMonth.minusMonths(1).lengthOfMonth();

        LocalDate today = LocalDate.now();
        int todaysDay = today.getDayOfMonth();
        boolean isCurrentMonthShown = currentMonth.equals(YearMonth.from(today));

        GridPane monthGrid = new GridPane();
        addEqualColumns(monthGrid);
        addEqualRows(monthGrid, 6);
        monthGrid.setHgap(2);
        monthGrid.setVgap(2);

        int totalCells = 7 * 6; // zawsze 6 pełnych tygodni, żeby siatka miała stały, przewidywalny rozmiar
        for (int index = 0; index < totalCells; index++){
            int col = index % 7;
            int row = index / 7;

            int dayNumber;
            boolean inCurrentMonth;

            if (index < startCol) { // dni z końca poprzedniego miesiąca, wypełniające puste miejsce przed 1. dniem
                dayNumber = prevMonthLength - startCol + 1 + index;
                inCurrentMonth = false;
            } else if (index < startCol + daysInMonth) { // prawdziwe dni aktualnego miesiąca
                dayNumber = index - startCol + 1;
                inCurrentMonth = true;
            } else { // dni z początku następnego miesiąca, dopełniające ostatni tydzień
                dayNumber = index - startCol - daysInMonth + 1;
                inCurrentMonth = false;
            }

            VBox dayBox = new VBox();
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new Insets(6));
            dayBox.setStyle("-fx-border-color: lightgray;");

            Label dayNum = new Label(String.valueOf(dayNumber));
            dayNum.setFont(Font.font("Arial", 14));
            if (!inCurrentMonth) {
                dayNum.setStyle("-fx-text-fill: #bbbbbb;");
            }



            if(inCurrentMonth && isCurrentMonthShown && dayNumber == todaysDay){
                dayBox.setStyle("-fx-background-color: #f5eeef;");
            }
            dayBox.getChildren().add(dayNum);

            monthGrid.add(dayBox, col, row);
        }

        return monthGrid;
    }

    private void addEqualColumns(GridPane grid){
        for (int i = 0; i < 7; i++){
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7);
            grid.getColumnConstraints().add(column);
        }
    }

    private void addEqualRows(GridPane grid, int rowCount){
        for (int i = 0; i < rowCount; i++){
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / rowCount);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }
    }

    private void refresh(){
        getChildren().clear();
        add(buildNavBar(), 0, 0);
        add(buildHeader(), 0, 1);
        add(buildDays(), 0, 2);
    }

    private BorderPane buildNavBar(){
        BorderPane navBar = new BorderPane();

        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        Button actualMonth = new Button(formatMonthLabel());

        navBar.setLeft(prevButton);
        navBar.setRight(nextButton);
        navBar.setCenter(actualMonth);

        BorderPane.setAlignment(actualMonth, Pos.CENTER);

        prevButton.setOnAction(event-> {
            currentMonth = currentMonth.minusMonths(1);
            refresh();
        });

        nextButton.setOnAction(event -> {
            currentMonth = currentMonth.plusMonths(1);
            refresh();
        });

        actualMonth.setOnAction(event->{
            this.currentMonth = YearMonth.now();
            refresh();
        });

        return navBar;
    }

    private String formatMonthLabel(){
        Month month = currentMonth.getMonth();
        String formatedMonth = month.getDisplayName(TextStyle.FULL, new Locale("pl"));

        String firstLetter = formatedMonth.substring(0,1);
        String prettyName = firstLetter.toUpperCase() + formatedMonth.substring(1, formatedMonth.length());

        return prettyName + " " + currentMonth.getYear();
    }
}
