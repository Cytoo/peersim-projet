JAVAC=javac
JAVA=java
JAR=jar

LIB_DIR=$(ROOT)/peersim-lib
LIBS=`find $(LIB_DIR) -name '*.jar'`
CLASSPATH=-cp $(C_DIR)

ROOT=$(shell pwd)
SRC_DIR=$(ROOT)/src/projet
MANIFEST_DIR=$(SRC_DIR)/META-INF
MANIFEST=$(MANIFEST_DIR)/MANIFEST.MF
SRCS=*.java
OBJS=$(SRCS:.java=.class)
C_DIR=$(ROOT)/output
C_DIR_USER=$(C_DIR)/projet

OUTPUT_JAR=ElectionLeader.jar

all: move-to-c-dir create-jar move-jar

move-jar:
	mv $(C_DIR)/$(OUTPUT_JAR) .

create-jar:
	(cd $(C_DIR) && $(JAR) cmvf META-INF/MANIFEST.MF $(OUTPUT_JAR) `find . -name '*.class'`)

extract-libs-jar:
	cp $(LIB_DIR)/*.jar $(C_DIR)
	(cd $(C_DIR) && find -name '*.jar' -exec $(JAR) xf {} \;)
	rm $(C_DIR)/*.jar

compile-class:
	(cd $(SRC_DIR) && $(JAVAC) $(CLASSPATH) $(SRCS))

move-to-c-dir: $(C_DIR) $(C_DIR_USER) extract-libs-jar compile-class
	mv $(SRC_DIR)/$(OBJS) $(C_DIR_USER)
	cp -r $(MANIFEST_DIR) $(C_DIR)

$(C_DIR):
	mkdir -p $(C_DIR)

$(C_DIR_USER):
	mkdir -p $(C_DIR_USER)
	
clean:
	rm -r $(C_DIR)
	@find . -name *.class -delete -print
	@find . -name $(OUTPUT_JAR) -delete -print

run:
	java -jar $(OUTPUT_JAR) src/projet/configuration
