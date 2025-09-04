JAVAC=javac
JAVA=java
SRC=src/main/java
BIN=bin
JAVA_FX_PATH=/home/ljacquem/javafx-sdk-17.0.16/lib

# JavaFX modules
MODULES=javafx.controls,javafx.fxml

# Source files
SOURCES=$(wildcard $(SRC)/*.java)
CLASSES=$(SOURCES:$(SRC)/%.java=$(BIN)/%.class)

all:
	javac --module-path $(JAVA_FX_PATH) \
		--add-modules $(MODULES) \
		-d $(BIN) $(SRC)/*.java
	java --module-path $(JAVA_FX_PATH) \
		--add-modules $(MODULES) -cp $(BIN) main.java.GameApp



run: $(CLASSES)
	$(JAVA) --module-path $(JAVA_FX_PATH) --add-modules $(MODULES) -cp $(BIN) HelloFX

clean:
	rm -rf $(BIN)
