# Babel makefile

TARGET  = target/release/litenWebsite
STATIC  =	$(TARGET)/web/static
ASSETS  = $(STATIC)/assets
FTL     = $(TARGET)/web/freemarker
NBIN		= ./node_modules/.bin
PORT		= 8000

.PHONY: clean browserify libs test watch compile server static


# Targets

all: compile

node_modules:
	npm install --save-dev

target:
	mkdir -p $(FTL)
	mkdir -p $(ASSETS)
	mkdir -p $(ASSETS)/js

clean:
	rm -rf target

$(ASSETS)/libs/bootstrap:
	mkdir -p $(ASSETS)/libs/bootstrap

libs: target node_modules $(ASSETS)/libs/bootstrap
	cp -r ./node_modules/bootstrap/dist/* $(ASSETS)/libs/bootstrap

# Generated Code
target/generated: target
	mkdir target/generated

watch: node_modules libs target
	$(NBIN)/watchify js/main.js -o $(ASSETS)/js/bundle.js -v -d -t babelify


# $(NBIN)/browserify . -t [envify --NODE_ENV production] -t [babelify --stage 0] | $(NBIN)/uglifyjs -cm > $(ASSETS)/js/bundle.js

browserify: node_modules target
	$(NBIN)/browserify js/main.js -t babelify -o $(ASSETS)/js/bundle.js

static:
	mkdir -p $(ASSETS)/css
	cp -r css/* $(ASSETS)/css
	cp -r root/* $(STATIC)/
	cp -r freemarker/* $(FTL)
	cp -r assets/* $(ASSETS)

compile: libs browserify static

test: node_modules target
	npm test

server: node_modules target
	$(NBIN)/http-server $(STATIC) -p $(PORT) --utc
