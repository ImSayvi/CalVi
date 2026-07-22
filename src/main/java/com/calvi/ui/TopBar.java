package com.calvi.ui;

import java.util.function.Consumer;

import com.calvi.model.WeatherLocation;
import com.calvi.weather.GeocodeResult;
import com.calvi.weather.WeatherService;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class TopBar extends HBox {
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean locked = false;
    private Consumer<Boolean> onLockChanged;
    private Runnable onDataChanged;
    private Runnable onWeatherLocationChanged;
    private final WeatherLocation weatherLocation;
    private Popup settingsPopup;

    public TopBar(Stage stage, WeatherLocation weatherLocation) {
        this.weatherLocation = weatherLocation;

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setStyle("-fx-background-color: #2c2c2c; -fx-padding: 8 10 8 12;");

        Label title = new Label("CalVi");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // pushnie resztę przycisków na prawą stronę paska
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button("⚙");
        settingsButton.setOnAction(event -> toggleSettingsPopup(settingsButton));
        Button closeButton = new Button("✕");
        closeButton.setOnAction(event -> stage.close());

        Button lockButton = new Button("🔓");
        lockButton.setOnAction(event -> {
            locked = !locked;
            lockButton.setText(locked ? "🔒" : "🔓");
            // w zablokowanym stanie ma działać wyłącznie sama kłódka - reszta paska (w tym drag,
            // liczony niżej ręcznie na mysz) ma być nieaktywna
            settingsButton.setDisable(locked);
            closeButton.setDisable(locked);
            // całe okno robi się półprzezroczyste - widać przez nie tło, ale treść wciąż da się przeczytać
            stage.setOpacity(locked ? 0.75 : 1.0);
            if (onLockChanged != null) {
                onLockChanged.accept(locked);
            }
        });

        getChildren().addAll(title, spacer, lockButton, settingsButton, closeButton);

        // okno bez systemowej ramki nie da się już przeciągać za domyślny pasek tytułu,
        // więc sami liczymy przesunięcie myszy i przesuwamy nim Stage
        setOnMousePressed(event -> {
            if (locked) {
                return;
            }
            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });
        setOnMouseDragged(event -> {
            if (locked) {
                return;
            }
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });
    }

    public void setOnLockChanged(Consumer<Boolean> onLockChanged) {
        this.onLockChanged = onLockChanged;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void setOnWeatherLocationChanged(Runnable onWeatherLocationChanged) {
        this.onWeatherLocationChanged = onWeatherLocationChanged;
    }

    private void toggleSettingsPopup(Button anchor){
        if (settingsPopup != null && settingsPopup.isShowing()) {
            settingsPopup.hide();
            return;
        }

        TextField cityField = new TextField(weatherLocation.getCity());
        cityField.setPrefWidth(140);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

        Button saveButton = new Button("Zapisz");
        saveButton.setOnAction(event -> {
            String newCity = cityField.getText().trim();
            if (!newCity.isEmpty()) {
                statusLabel.setText("Szukam...");
                applyNewCity(newCity, statusLabel);
            }
        });

        Label header = new Label("Miasto (pogoda w widoku tygodnia):");
        header.setStyle("-fx-font-weight: bold;");

        VBox popupContent = new VBox(6, header, cityField, saveButton, statusLabel);
        popupContent.setPadding(new Insets(10));
        popupContent.setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        settingsPopup = new Popup();
        settingsPopup.getContent().add(popupContent);
        settingsPopup.setAutoHide(true);

        Bounds anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        settingsPopup.show(anchor, anchorBounds.getMinX(), anchorBounds.getMaxY() + 4);
    }

    // szuka współrzędnych podanego miasta i - jeśli się uda - podmienia lokalizację, zapisuje i odświeża prognozę
    private void applyNewCity(String cityName, Label statusLabel){
        new Thread(() -> {
            try {
                GeocodeResult result = WeatherService.geocodeCity(cityName);

                Platform.runLater(() -> {
                    if (result == null) {
                        statusLabel.setText("Nie znaleziono miasta");
                        return;
                    }

                    weatherLocation.setCity(result.cityName());
                    weatherLocation.setLatitude(result.latitude());
                    weatherLocation.setLongitude(result.longitude());

                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                    if (onWeatherLocationChanged != null) {
                        onWeatherLocationChanged.run();
                    }
                    if (settingsPopup != null) {
                        settingsPopup.hide();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Błąd sieci"));
            }
        }).start();
    }
}
