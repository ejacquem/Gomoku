JAVAC=javac
JAVA=java
SRC=src/main/java
BIN=bin

JAVA_FX_VERSION = 17.0.16
JAVA_FX_PATH = lib/javafx-sdk-$(JAVA_FX_VERSION)/lib
JAVA_FX_ZIP = openjfx-$(JAVA_FX_VERSION)_linux-x64_bin-sdk.zip
JAVA_FX_URL = https://download2.gluonhq.com/openjfx/$(JAVA_FX_VERSION)/$(JAVA_FX_ZIP)

MODULES=javafx.controls,javafx.fxml

SOURCES=$(SRC)/game/Board.java \
		$(SRC)/game/BoardAnalyser.java \
		$(SRC)/game/BoardListener.java \
		$(SRC)/game/BoardGame.java \
		$(SRC)/game/Coords.java \
		$(SRC)/game/GomokuAI.java \
		$(SRC)/game/GomokuBot.java \
		$(SRC)/ui/BoardRenderer.java \
		$(SRC)/ui/GameUI.java \
		$(SRC)/ui/GameUISettings.java \
		$(SRC)/ui/UtilsRenderer.java \
		$(SRC)/ui/MoveHistoryPanel.java \
		$(SRC)/app/GameApp.java \
		$(SRC)/app/GameSettings.java \
		$(SRC)/utils/GomokuUtils.java \
		$(SRC)/utils/FileHelper.java \
		$(SRC)/utils/ScoreBuckets.java \
		$(SRC)/utils/Zobrist.java \
		$(SRC)/utils/TranspositionTable.java \

CLASSES=$(SOURCES:$(SRC)/%.java=$(BIN)/%.class)

all: check_javafx
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

check_javafx:
	@if [ ! -d "$(JAVA_FX_PATH)" ]; then \
		echo "JavaFX not found at $(JAVA_FX_PATH)."; \
		echo "Either change the path in the Makefile or download it."; \
		read -p "Would you like to install it? [y/N] " ans; \
		if [ "$$ans" = "y" ]; then \
			echo "Downloading JavaFX..."; \
			mkdir -p lib; \
			curl -L -o lib/$(JAVA_FX_ZIP) $(JAVA_FX_URL); \
			if unzip -q lib/$(JAVA_FX_ZIP) -d lib; then \
				rm lib/$(JAVA_FX_ZIP); \
				echo "JavaFX installed successfully."; \
			else \
				echo "Error: 'unzip' failed or is not installed."; \
				echo "The archive is kept at lib/$(JAVA_FX_ZIP). Please install 'unzip' and extract manually."; \
				exit 1; \
			fi; \
		else \
			echo "Aborted. Please set JAVA_FX_PATH correctly."; \
			exit 1; \
		fi \
	else \
		echo "JavaFX already installed at $(JAVA_FX_PATH)"; \
	fi

clean:
	rm -rf $(BIN)
