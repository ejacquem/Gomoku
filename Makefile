JAVAC=javac
JAVA=java
SRC=src/main/java
BIN=bin
JAVA_FX_PATH=/home/ljacquem/javafx-sdk-17.0.16/lib

MODULES=javafx.controls,javafx.fxml

SOURCES=$(SRC)/game/Board.java \
		$(SRC)/game/BoardAnalyser.java \
		$(SRC)/game/BoardGame.java \
		$(SRC)/game/Cell.java \
		$(SRC)/game/CellInfo.java \
		$(SRC)/game/Coords.java \
		$(SRC)/game/Move.java \
		$(SRC)/game/GomokuAI.java \
		$(SRC)/game/GomokuBot.java \
		$(SRC)/ui/BoardRenderer.java \
		$(SRC)/ui/GameUI.java \
		$(SRC)/ui/GameUISettings.java \
		$(SRC)/ui/UtilsRenderer.java \
		$(SRC)/app/GameApp.java \
		$(SRC)/app/GameSettings.java \
		$(SRC)/utils/GomokuUtils.java \

CLASSES=$(SOURCES:$(SRC)/%.java=$(BIN)/%.class)

all:
	@mkdir -p $(BIN)
	$(JAVAC) --module-path $(JAVA_FX_PATH) \
	--add-modules $(MODULES) \
	-d $(BIN) $(SOURCES)
	$(JAVA) --module-path $(JAVA_FX_PATH) \
	--add-modules $(MODULES) -cp $(BIN) main.java.app.GameApp

$(CLASSES): $(SOURCES)
	@mkdir -p $(BIN)/game $(BIN)/ui
	$(JAVAC) --module-path $(JAVA_FX_PATH) \
	--add-modules $(MODULES) \
	-d $(BIN) $(SOURCES)

clean:
	rm -rf $(BIN)
