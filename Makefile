JAVAC=javac
JAVA=java
SRC=src
BIN=bin
JAVA_FX_PATH=/home/ljacquem/javafx-sdk-17.0.16/lib

# JavaFX modules
MODULES=javafx.controls,javafx.fxml

# Source files
SOURCES=$(wildcard $(SRC)/*.java)
CLASSES=$(SOURCES:$(SRC)/%.java=$(BIN)/%.class)

all:
	javac --module-path /home/ljacquem/javafx-sdk-17.0.16/lib \
		--add-modules javafx.controls,javafx.fxml \
		-d bin src/*.java
	java --module-path /home/ljacquem/javafx-sdk-17.0.16/lib \
		--add-modules javafx.controls,javafx.fxml -cp bin GameApp

run: $(CLASSES)
	$(JAVA) --module-path $(JAVA_FX_PATH) --add-modules $(MODULES) -cp $(BIN) HelloFX

clean:
	rm -rf $(BIN)
