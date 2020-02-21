.PHONY: prepare test coverage clean

node_modules/ws:
	npm install
prepare: node_modules/ws

test: prepare
	lein test-all

coverage:
	lein cloverage --codecov

clean:
	lein clean
	\rm -rf node_modules
